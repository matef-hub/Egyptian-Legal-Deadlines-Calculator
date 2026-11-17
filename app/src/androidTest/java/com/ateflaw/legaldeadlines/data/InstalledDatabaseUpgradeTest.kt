package com.ateflaw.legaldeadlines.data

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ateflaw.legaldeadlines.data.database.AppDatabase
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InstalledDatabaseUpgradeTest {
    @Test fun openingEveryArchivedSchemaPreservesAnExistingDeadline() = runBlocking {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        for (version in listOf(1, 2, 3, 4, 11, 12)) {
            val name = "upgrade-test-$version.db"
            context.deleteDatabase(name)
            try {
                val dbFile = context.getDatabasePath(name)
                dbFile.parentFile?.mkdirs()
                SQLiteDatabase.openOrCreateDatabase(dbFile, null).use { db ->
                    val json = instrumentation.context.assets.open("$version.json").bufferedReader().use { it.readText() }
                    val entities = JSONObject(json).getJSONObject("database").getJSONArray("entities")
                    for (index in 0 until entities.length()) {
                        val entity = entities.getJSONObject(index)
                        val table = entity.getString("tableName")
                        fun execute(sql: String) = db.execSQL(sql.replace("\${TABLE_NAME}", table))
                        execute(entity.getString("createSql"))
                        val indices = entity.optJSONArray("indices")
                        for (i in 0 until (indices?.length() ?: 0)) execute(indices!!.getJSONObject(i).getString("createSql"))
                    }
                    db.execSQL("""
                        INSERT INTO saved_deadlines(id, case_number, client_name, action_name,
                            announcement_date, duration, unit, distance_days, final_deadline,
                            law_article, calculation_explanation, notes, created_at)
                        VALUES(42,'٦٦/٢٠٢٦','موكل','ميعاد','2026-10-01',7,'DAYS',0,
                            '2026-10-08','مادة','حساب','ملاحظة',1)
                    """.trimIndent())
                    db.version = version
                }
                val upgraded = Room.databaseBuilder(context, AppDatabase::class.java, name)
                    .addMigrations(*AppDatabase.migrations).build()
                try {
                    val saved = checkNotNull(upgraded.savedDeadlineDao().getById(42))
                    assertEquals("٦٦/٢٠٢٦", saved.caseNumber)
                    assertEquals("2026-10-08", saved.finalDeadline)
                    assertEquals("ملاحظة", saved.notes)
                    assertEquals(AppDatabase.VERSION, upgraded.openHelper.readableDatabase.version)
                } finally {
                    upgraded.close()
                }
            } finally {
                context.deleteDatabase(name)
            }
        }
    }
}
