package com.ateflaw.legaldeadlines.data.backup

import android.content.Context
import android.content.ContentValues
import android.net.Uri
import androidx.room.withTransaction
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ateflaw.legaldeadlines.data.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.time.Instant

/**
 * Offline import/export through user-selected Storage Access Framework documents.
 * No broad storage permission, no automatic cloud backup and no raw SQLite/WAL file export.
 *
 * Merge imports only missing appointments. Full replacement is explicitly selected in the UI.
 */
class BackupTransferService(
    private val database: AppDatabase,
    private val context: Context
) {
    private val resolver get() = context.contentResolver

    enum class RestoreMode { MERGE_DEADLINES, REPLACE_ALL }

    data class Totals(
        val savedDeadlines: Int,
        val editedRules: Int,
        val historyEntries: Int,
        val holidays: Int
    )

    private val tables = listOf("legal_rules", "saved_deadlines", "holidays", "rule_edit_history")

    suspend fun exportTo(destination: Uri, password: CharArray): Totals = withContext(Dispatchers.IO) {
        database.prepareRuleCatalog(context)
        val document = database.withTransaction {
            val db = database.openHelper.writableDatabase
            val tableObject = JSONObject()
            for (table in tables) {
                tableObject.put(table, exportRows(db, table))
            }
            JSONObject()
                .put("format", "meead-transfer")
                .put("format_version", 1)
                .put("source_database_version", AppDatabase.VERSION)
                .put("exported_at", Instant.now().toString())
                .put("tables", tableObject)
        }
        val plaintext = document.toString().toByteArray(StandardCharsets.UTF_8)
        try {
            val encrypted = EncryptedBackupCodec.encrypt(plaintext, password)
            resolver.openOutputStream(destination, "w")?.use { it.write(encrypted) }
                ?: error("تعذر فتح الملف المحدد لحفظ النسخة الاحتياطية.")
            val rows = document.getJSONObject("tables")
            Totals(
                savedDeadlines = rows.getJSONArray("saved_deadlines").length(),
                editedRules = rows.getJSONArray("legal_rules").length(),
                historyEntries = rows.getJSONArray("rule_edit_history").length(),
                holidays = rows.getJSONArray("holidays").length()
            )
        } finally {
            plaintext.fill(0)
        }
    }

    suspend fun importFrom(source: Uri, password: CharArray, mode: RestoreMode): Totals = withContext(Dispatchers.IO) {
        val encrypted = resolver.openInputStream(source)?.use { input ->
            val buffer = ByteArrayOutputStream()
            val chunk = ByteArray(8192)
            while (true) {
                val n = input.read(chunk)
                if (n < 0) break
                check(buffer.size() + n <= EncryptedBackupCodec.maxEncryptedBytes) {
                    "ملف النسخة الاحتياطية أكبر من الحجم المسموح."
                }
                buffer.write(chunk, 0, n)
            }
            buffer.toByteArray()
        } ?: error("تعذر قراءة ملف النسخة الاحتياطية.")
        val clear = EncryptedBackupCodec.decrypt(encrypted, password)
        val document = try {
            JSONObject(String(clear, StandardCharsets.UTF_8))
        } finally {
            clear.fill(0)
            encrypted.fill(0)
        }
        // The original desktop transfer tool used -1 for an unknown Room version.
        val sourceVersion = document.getInt("source_database_version")
        require(document.optString("format") == "meead-transfer" &&
            document.optInt("format_version") == 1 &&
            (sourceVersion == -1 || sourceVersion in 1..AppDatabase.VERSION)) {
            "إصدار النسخة الاحتياطية غير مدعوم."
        }
        val rows = document.getJSONObject("tables")
        require(tables.all { rows.has(it) && rows.optJSONArray(it) != null }) {
            "النسخة الاحتياطية ناقصة أو تالفة."
        }
        require(tables.sumOf { rows.getJSONArray(it).length() } <= 30_000) {
            "عدد السجلات في النسخة الاحتياطية يتجاوز الحد المسموح."
        }

        database.prepareRuleCatalog(context)
        database.withTransaction {
            val db = database.openHelper.writableDatabase
            val columns = tables.associateWith { tableColumns(db, it) }
            // Prevalidate every row before the first write, including column names/types.
            val records = tables.associateWith { table ->
                val array = rows.getJSONArray(table)
                (0 until array.length()).map { index ->
                    toContentValues(array.getJSONObject(index), columns.getValue(table))
                }
            }

            val appointments = records.getValue("saved_deadlines")
            for (row in appointments) {
                java.time.LocalDate.parse(row.getAsString("final_deadline"))
                java.time.LocalDate.parse(row.getAsString("announcement_date"))
                com.ateflaw.legaldeadlines.domain.model.DurationUnit.valueOf(row.getAsString("unit"))
            }
            if (mode == RestoreMode.MERGE_DEADLINES) {
                val inserted = appointments.count { insertMissingDeadline(db, it) }
                return@withTransaction Totals(inserted, 0, 0, 0)
            }

            // Any malformed row rolls back the entire replacement, including the deletes.
            for (table in listOf("rule_edit_history", "saved_deadlines", "holidays", "legal_rules")) {
                db.execSQL("DELETE FROM `$table`")
            }
            database.loadBuiltInCatalog(context)
            val seedById = com.ateflaw.legaldeadlines.data.seed.LegalRuleSeedData.getSeedRules().associateBy { it.id }
            val historicallyEditedIds = records.getValue("rule_edit_history")
                .mapNotNull { it.getAsLong("rule_id") }
                .toSet()
            var importedRules = 0
            for (row in records.getValue("legal_rules")) {
                val id = row.getAsLong("id") ?: error("قاعدة قانونية دون معرف.")
                val decision = row.getAsString("workbook_decision").orEmpty()
                val ownerDecision = decision == "USER_APPROVED" || decision == "EDITED_PENDING"
                val revision = row.getAsInteger("revision") ?: 0
                if (id > 169 || ownerDecision || id in historicallyEditedIds || revision >= 3) {
                    // Equivalent to the v10->v11 on-device migration for owner-saved
                    // drafts in old preview packages. Never approve untouched seeds.
                    if (decision == "EDITED_PENDING") {
                        row.put("workbook_decision", "USER_APPROVED")
                        row.put("review_status", "VERIFIED_ACCURATE")
                        row.put("review_version", maxOf(1, row.getAsInteger("review_version") ?: 0))
                    }
                    com.ateflaw.legaldeadlines.domain.model.DurationUnit.valueOf(row.getAsString("unit"))
                    com.ateflaw.legaldeadlines.domain.model.StartRule.valueOf(row.getAsString("start_rule"))
                    db.insert("legal_rules", android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE, row)
                    importedRules++
                } else {
                    // Preserve untracked annotations from old installs without reinstating obsolete
                    // bundled rule text over a current statutory edition.
                    val seed = seedById[id]
                    val installed = database.legalRuleDao().getById(id)
                    if (seed != null && installed != null) {
                        val workbook = com.ateflaw.legaldeadlines.data.review.ReviewedWorkbookRecord(id,
                            decision, "", "", "", "")
                        val notes = row.getAsString("notes")
                        val article = row.getAsString("article_text")
                        val summary = row.getAsString("summary")
                        val annotated = installed.copy(
                            notes = notes?.takeUnless { it == seed.notes || it == workbook.correctedNotes } ?: installed.notes,
                            summary = summary?.takeUnless { it == seed.summary || it == workbook.correctedSummary } ?: installed.summary,
                            articleText = article?.takeUnless { it.isBlank() || it == seed.articleText } ?: installed.articleText
                        )
                        if (annotated != installed) {
                            database.legalRuleDao().updateBuiltIn(annotated)
                            importedRules++
                        }
                    }
                }
            }
            for (row in records.getValue("rule_edit_history")) {
                val parent = row.getAsLong("rule_id") ?: error("مراجعة بلا قاعدة.")
                require(database.legalRuleDao().getById(parent) != null) { "مراجعة لقاعدة غير موجودة." }
                db.insert("rule_edit_history", android.database.sqlite.SQLiteDatabase.CONFLICT_ABORT, row)
            }
            for (row in appointments) {
                row.remove("reminder_sent_for")
                db.insert("saved_deadlines", android.database.sqlite.SQLiteDatabase.CONFLICT_ABORT, row)
            }
            var importedHolidays = 0
            for (sourceRow in records.getValue("holidays")) {
                val date = sourceRow.getAsString("holiday_date")
                    ?: error("سجل عطلة دون تاريخ.")
                val existing = db.query(
                    "SELECT verification_status FROM holidays WHERE holiday_date = ?",
                    arrayOf(date)
                ).use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
                if (existing == "VERIFIED_OFFICIAL") continue
                val row = ContentValues(sourceRow)
                row.remove("id")
                if (row.getAsString("verification_status") == "VERIFIED_OFFICIAL" &&
                    !row.getAsString("source_url").orEmpty().startsWith("https://")) {
                    row.put("verification_status", "UNVERIFIED")
                }
                db.insert("holidays", android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE, row)
                importedHolidays++
            }
            database.loadBuiltInCatalog(context)
            Totals(
                savedDeadlines = records.getValue("saved_deadlines").size,
                editedRules = importedRules,
                historyEntries = records.getValue("rule_edit_history").size,
                holidays = importedHolidays
            )
        }
    }

    private fun insertMissingDeadline(db: SupportSQLiteDatabase, source: ContentValues): Boolean {
        val row = ContentValues(source)
        row.remove("id")
        row.remove("reminder_sent_for")
        if (!row.containsKey("is_provisional")) row.put("is_provisional", 1)
        val keys = row.keySet().sorted()
        // Column names were checked against the Room schema before entering this method.
        val predicate = keys.joinToString(" AND ") { "`$it` IS ?" }
        val args = keys.map { row.get(it) }.toTypedArray()
        val exists = db.query("SELECT 1 FROM saved_deadlines WHERE $predicate LIMIT 1", args)
            .use { it.moveToFirst() }
        if (exists) return false
        db.insert("saved_deadlines", android.database.sqlite.SQLiteDatabase.CONFLICT_ABORT, row)
        return true
    }

    private fun exportRows(db: SupportSQLiteDatabase, table: String): JSONArray {
        val result = JSONArray()
        db.query("SELECT * FROM `$table`").use { cursor ->
            while (cursor.moveToNext()) {
                require(result.length() < 30_000) { "عدد السجلات كبير جدًا لتصدير محلي." }
                val row = JSONObject()
                for (i in 0 until cursor.columnCount) {
                    val name = cursor.getColumnName(i)
                    val value: Any = when (cursor.getType(i)) {
                        android.database.Cursor.FIELD_TYPE_NULL -> JSONObject.NULL
                        android.database.Cursor.FIELD_TYPE_INTEGER -> cursor.getLong(i)
                        android.database.Cursor.FIELD_TYPE_FLOAT -> cursor.getDouble(i)
                        android.database.Cursor.FIELD_TYPE_STRING -> cursor.getString(i)
                        else -> error("نوع بيانات غير مدعوم في $table.")
                    }
                    row.put(name, value)
                }
                result.put(row)
            }
        }
        return result
    }

    private fun tableColumns(db: SupportSQLiteDatabase, table: String): Set<String> {
        val fields = mutableSetOf<String>()
        db.query("PRAGMA table_info(`$table`)").use { cursor ->
            val i = cursor.getColumnIndex("name")
            check(i >= 0) { "تعذر قراءة بنية الجدول." }
            while (cursor.moveToNext()) fields.add(cursor.getString(i))
        }
        return fields
    }

    private fun toContentValues(obj: JSONObject, allowed: Set<String>): ContentValues {
        val values = ContentValues()
        val keys = obj.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            require(key in allowed) { "النسخة تحتوي حقلًا غير معروف: $key." }
            when (val value = obj.get(key)) {
                JSONObject.NULL -> values.putNull(key)
                is String -> {
                    require(value.length <= 100_000) { "حجم الحقل غير صالح: $key." }
                    values.put(key, value)
                }
                is Int -> values.put(key, value)
                is Long -> values.put(key, value)
                is Double -> values.put(key, value)
                else -> error("نوع حقل غير صالح في ملف النسخة: $key.")
            }
        }
        return values
    }
}
