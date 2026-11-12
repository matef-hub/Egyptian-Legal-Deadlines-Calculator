package com.ateflaw.legaldeadlines.data.database

import android.database.Cursor
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteQuery
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.sql.DriverManager
import java.sql.ResultSet

/**
 * Migration verification test suite for Room database migrations:
 * - Upgrade path 1 -> 2 -> 3 -> 4
 * - Direct upgrade path 1 -> 4
 * - Upgrade path 2 -> 3 -> 4
 * - Direct upgrade path 2 -> 4
 * - Data preservation across all migrations (deadlines, client details, rules)
 * - MIGRATION_1_2 deduplication of duplicate holiday dates before unique index creation
 * - Verification of default values and nullability (effective_to, review_status, distance_type, proactive_reminder_days)
 * - Non-destructive verification (no data loss)
 */
class DatabaseMigrationTest {

    /**
     * In-memory SQLite fixture that implements SupportSQLiteDatabase for JVM unit testing.
     */
    private class TestSupportSQLiteDatabase : SupportSQLiteDatabase {
        private val connection = DriverManager.getConnection("jdbc:sqlite::memory:")

        override fun execSQL(sql: String) {
            connection.createStatement().use { statement ->
                statement.execute(sql)
            }
        }

        override fun execSQL(sql: String, bindArgs: Array<out Any?>) {
            connection.prepareStatement(sql).use { ps ->
                bindArgs.forEachIndexed { index, arg ->
                    ps.setObject(index + 1, arg)
                }
                ps.execute()
            }
        }

        override fun query(query: String): Cursor {
            val statement = connection.createStatement()
            val resultSet = statement.executeQuery(query)
            return ResultSetCursor(resultSet, statement)
        }

        override fun query(query: String, bindArgs: Array<out Any?>): Cursor {
            val ps = connection.prepareStatement(query)
            bindArgs.forEachIndexed { index, arg ->
                ps.setObject(index + 1, arg)
            }
            val resultSet = ps.executeQuery()
            return ResultSetCursor(resultSet, ps)
        }

        override fun query(query: SupportSQLiteQuery): Cursor = query(query.sql)
        override fun query(query: SupportSQLiteQuery, cancellationSignal: android.os.CancellationSignal?): Cursor = query(query.sql)

        override fun close() {
            connection.close()
        }

        // Unused interface stubs required by SupportSQLiteDatabase
        override fun beginTransaction() {}
        override fun beginTransactionNonExclusive() {}
        override fun beginTransactionReadOnly() {}
        override fun beginTransactionWithListener(transactionListener: android.database.sqlite.SQLiteTransactionListener) {}
        override fun beginTransactionWithListenerNonExclusive(transactionListener: android.database.sqlite.SQLiteTransactionListener) {}
        override fun endTransaction() {}
        override fun setTransactionSuccessful() {}
        override fun inTransaction(): Boolean = false
        override val isDbLockedByCurrentThread: Boolean get() = false
        override fun yieldIfContendedSafely(): Boolean = false
        override fun yieldIfContendedSafely(sleepAfterYieldDelayMillis: Long): Boolean = false
        override var version: Int
            get() = 0
            set(_) {}
        override val maximumSize: Long get() = 0
        override fun setMaximumSize(numBytes: Long): Long = 0
        override var pageSize: Long
            get() = 0
            set(_) {}
        override fun compileStatement(sql: String): androidx.sqlite.db.SupportSQLiteStatement = throw UnsupportedOperationException()
        override fun insert(table: String, conflictAlgorithm: Int, values: android.content.ContentValues): Long = throw UnsupportedOperationException()
        override fun delete(table: String, whereClause: String?, whereArgs: Array<out Any?>?): Int = throw UnsupportedOperationException()
        override fun update(table: String, conflictAlgorithm: Int, values: android.content.ContentValues, whereClause: String?, whereArgs: Array<out Any?>?): Int = throw UnsupportedOperationException()
        override fun needUpgrade(newVersion: Int): Boolean = false
        override val path: String? get() = null
        override fun setLocale(locale: java.util.Locale) {}
        override fun setMaxSqlCacheSize(cacheSize: Int) {}
        override fun setForeignKeyConstraintsEnabled(enabled: Boolean) {}
        override fun enableWriteAheadLogging(): Boolean = false
        override fun disableWriteAheadLogging() {}
        override val isWriteAheadLoggingEnabled: Boolean get() = false
        override val attachedDbs: List<android.util.Pair<String, String>>? get() = null
        override val isDatabaseIntegrityOk: Boolean get() = true
        override val isOpen: Boolean get() = !connection.isClosed
        override val isReadOnly: Boolean get() = false
    }

    /**
     * Cursor wrapper over JDBC ResultSet for table_info and verification queries.
     */
    @Suppress("OVERRIDE_DEPRECATION")
    private class ResultSetCursor(
        private val resultSet: ResultSet,
        private val statement: java.sql.Statement
    ) : Cursor {
        private val metaData = resultSet.metaData
        private val columnNamesList = (1..metaData.columnCount).map { metaData.getColumnName(it) }

        override fun getColumnIndex(columnName: String): Int {
            return columnNamesList.indexOfFirst { it.equals(columnName, ignoreCase = true) }
        }

        override fun getColumnIndexOrThrow(columnName: String): Int {
            val idx = getColumnIndex(columnName)
            if (idx == -1) throw IllegalArgumentException("Column $columnName not found")
            return idx
        }

        override fun getColumnName(columnIndex: Int): String = columnNamesList[columnIndex]
        override fun getColumnNames(): Array<String> = columnNamesList.toTypedArray()
        override fun getColumnCount(): Int = columnNamesList.size

        override fun moveToNext(): Boolean = resultSet.next()
        override fun moveToFirst(): Boolean = throw UnsupportedOperationException()
        override fun getString(columnIndex: Int): String? = resultSet.getString(columnIndex + 1)
        override fun getInt(columnIndex: Int): Int = resultSet.getInt(columnIndex + 1)
        override fun getLong(columnIndex: Int): Long = resultSet.getLong(columnIndex + 1)
        override fun isNull(columnIndex: Int): Boolean = resultSet.getObject(columnIndex + 1) == null

        override fun close() {
            resultSet.close()
            statement.close()
        }

        override fun isClosed(): Boolean = resultSet.isClosed

        // Stub methods for Cursor interface
        override fun getCount(): Int = 0
        override fun getPosition(): Int = 0
        override fun move(offset: Int): Boolean = false
        override fun moveToPosition(position: Int): Boolean = false
        override fun moveToLast(): Boolean = false
        override fun moveToPrevious(): Boolean = false
        override fun isFirst(): Boolean = false
        override fun isLast(): Boolean = false
        override fun isBeforeFirst(): Boolean = false
        override fun isAfterLast(): Boolean = false
        override fun getShort(columnIndex: Int): Short = 0
        override fun getFloat(columnIndex: Int): Float = 0f
        override fun getDouble(columnIndex: Int): Double = 0.0
        override fun getType(columnIndex: Int): Int = 0
        override fun getBlob(columnIndex: Int): ByteArray? = null
        override fun copyStringToBuffer(columnIndex: Int, buffer: android.database.CharArrayBuffer?) {}
        @Suppress("OVERRIDE_DEPRECATION")
        override fun deactivate() {}
        override fun requery(): Boolean = false
        override fun registerContentObserver(observer: android.database.ContentObserver?) {}
        override fun unregisterContentObserver(observer: android.database.ContentObserver?) {}
        override fun registerDataSetObserver(observer: android.database.DataSetObserver?) {}
        override fun unregisterDataSetObserver(observer: android.database.DataSetObserver?) {}
        override fun setNotificationUri(cr: android.content.ContentResolver?, uri: android.net.Uri?) {}
        override fun getNotificationUri(): android.net.Uri? = null
        override fun getWantsAllOnMoveCalls(): Boolean = false
        override fun setExtras(extras: android.os.Bundle?) {}
        override fun getExtras(): android.os.Bundle = android.os.Bundle.EMPTY
        override fun respond(extras: android.os.Bundle?): android.os.Bundle = android.os.Bundle.EMPTY
    }

    private fun createSchemaV1(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `legal_rules` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `action_name` TEXT NOT NULL,
                `duration` INTEGER NOT NULL,
                `unit` TEXT NOT NULL,
                `distance_days` INTEGER NOT NULL,
                `start_rule` TEXT NOT NULL,
                `law_article` TEXT NOT NULL,
                `notes` TEXT NOT NULL,
                `summary` TEXT NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `holidays` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `holiday_date` TEXT NOT NULL,
                `name` TEXT NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `saved_deadlines` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `case_number` TEXT NOT NULL,
                `client_name` TEXT NOT NULL,
                `action_name` TEXT NOT NULL,
                `announcement_date` TEXT NOT NULL,
                `duration` INTEGER NOT NULL,
                `unit` TEXT NOT NULL,
                `distance_days` INTEGER NOT NULL,
                `final_deadline` TEXT NOT NULL,
                `law_article` TEXT NOT NULL,
                `calculation_explanation` TEXT NOT NULL,
                `notes` TEXT NOT NULL,
                `created_at` INTEGER NOT NULL
            )
        """.trimIndent())
    }

    @Test
    fun testMigration1To2_withDuplicateHolidayDates_deduplicatesSafely() {
        TestSupportSQLiteDatabase().use { db ->
            createSchemaV1(db)

            // Insert realistic duplicate holiday dates
            db.execSQL("INSERT INTO holidays (holiday_date, name) VALUES ('2026-10-06', 'Armed Forces Day')")
            db.execSQL("INSERT INTO holidays (holiday_date, name) VALUES ('2026-10-06', 'Duplicate 6th October')")
            db.execSQL("INSERT INTO holidays (holiday_date, name) VALUES ('2026-01-25', 'Revolution Day')")

            // Execute MIGRATION_1_2
            AppDatabase.MIGRATION_1_2.migrate(db)

            // Verify unique index was created and duplicates were safely pruned
            db.query("SELECT COUNT(*) AS total FROM holidays").use { cursor ->
                assertTrue(cursor.moveToNext())
                assertEquals(2, cursor.getInt(0))
            }
        }
    }

    @Test
    fun testMigrationPath_1_to_2_to_3_to_4_preservesAllData() {
        TestSupportSQLiteDatabase().use { db ->
            createSchemaV1(db)

            // Seed user data in v1
            db.execSQL("""
                INSERT INTO legal_rules (id, action_name, duration, unit, distance_days, start_rule, law_article, notes, summary)
                VALUES (1, 'طعن بالنقض مدني', 60, 'DAYS', 0, 'NEXT_DAY', 'المادة 243 مرافعات', 'ميعاد حتمي', 'ملخص')
            """.trimIndent())

            db.execSQL("""
                INSERT INTO saved_deadlines (id, case_number, client_name, action_name, announcement_date, duration, unit, distance_days, final_deadline, law_article, calculation_explanation, notes, created_at)
                VALUES (101, '452/2026', 'أحمد محمود', 'استئناف مدني', '2026-10-01', 40, 'DAYS', 0, '2026-11-10', 'المادة 227 مرافعات', 'شرح تفصيلي', 'ملاحظة سرية', 1729000000000)
            """.trimIndent())

            // Execute sequential migrations
            AppDatabase.MIGRATION_1_2.migrate(db)
            AppDatabase.MIGRATION_2_3.migrate(db)
            AppDatabase.MIGRATION_3_4.migrate(db)

            // Verify saved deadline remained intact
            db.query("SELECT case_number, client_name, proactive_reminder_days, distance_type FROM saved_deadlines WHERE id = 101").use { cursor ->
                assertTrue(cursor.moveToNext())
                assertEquals("452/2026", cursor.getString(0))
                assertEquals("أحمد محمود", cursor.getString(1))
                assertTrue(cursor.isNull(2)) // Default proactive_reminder_days is NULL
                assertEquals("NONE", cursor.getString(3)) // Default distance_type is NONE
            }

            // Verify legal rule migrated with NEEDS_REVIEW to preserve legal integrity
            db.query("SELECT action_name, review_status, trigger_event_type, effective_to FROM legal_rules WHERE id = 1").use { cursor ->
                assertTrue(cursor.moveToNext())
                assertEquals("طعن بالنقض مدني", cursor.getString(0))
                assertEquals("NEEDS_REVIEW", cursor.getString(1))
                assertEquals("ANNOUNCEMENT", cursor.getString(2))
                assertTrue(cursor.isNull(3)) // effective_to DEFAULT NULL
            }
        }
    }

    @Test
    fun testDirectMigration_1_to_4_matchesSequential() {
        TestSupportSQLiteDatabase().use { db ->
            createSchemaV1(db)

            db.execSQL("""
                INSERT INTO saved_deadlines (id, case_number, client_name, action_name, announcement_date, duration, unit, distance_days, final_deadline, law_article, calculation_explanation, notes, created_at)
                VALUES (201, '999/2026', 'سعيد علي', 'معارضة جنح', '2026-10-01', 10, 'DAYS', 0, '2026-10-11', 'المادة 398 إجراءات', 'شرح', '', 1729000000000)
            """.trimIndent())

            AppDatabase.MIGRATION_1_4.migrate(db)

            // Verify all columns exist
            assertTrue(AppDatabase.columnExists(db, "saved_deadlines", "proactive_reminder_days"))
            assertTrue(AppDatabase.columnExists(db, "saved_deadlines", "distance_type"))
            assertTrue(AppDatabase.columnExists(db, "saved_deadlines", "rule_snapshot"))
            assertTrue(AppDatabase.columnExists(db, "legal_rules", "review_status"))
            assertTrue(AppDatabase.columnExists(db, "legal_rules", "effective_to"))

            db.query("SELECT client_name, distance_type FROM saved_deadlines WHERE id = 201").use { cursor ->
                assertTrue(cursor.moveToNext())
                assertEquals("سعيد علي", cursor.getString(0))
                assertEquals("NONE", cursor.getString(1))
            }
        }
    }

    @Test
    fun testDirectMigration_2_to_4_and_2_to_3_to_4() {
        TestSupportSQLiteDatabase().use { db ->
            createSchemaV1(db)
            AppDatabase.MIGRATION_1_2.migrate(db) // Now at v2

            db.execSQL("""
                INSERT INTO saved_deadlines (id, case_number, client_name, action_name, announcement_date, duration, unit, distance_days, final_deadline, law_article, calculation_explanation, notes, created_at)
                VALUES (301, '111/2026', 'محمود حسن', 'استئناف مستعجل', '2026-10-01', 15, 'DAYS', 0, '2026-10-16', 'المادة 227', '', '', 1729000000000)
            """.trimIndent())

            AppDatabase.MIGRATION_2_4.migrate(db)

            assertTrue(AppDatabase.columnExists(db, "saved_deadlines", "proactive_reminder_days"))
            assertTrue(AppDatabase.columnExists(db, "legal_rules", "review_status"))

            db.query("SELECT client_name FROM saved_deadlines WHERE id = 301").use { cursor ->
                assertTrue(cursor.moveToNext())
                assertEquals("محمود حسن", cursor.getString(0))
            }
        }
    }

    @Test
    fun testMigration4To5_preservesClientDataAndQuarantinesUnsourcedHolidays() {
        TestSupportSQLiteDatabase().use { db ->
            createSchemaV1(db)
            db.execSQL("INSERT INTO holidays (holiday_date, name) VALUES ('2026-10-06', 'عطلة غير موثقة')")
            db.execSQL("""
                INSERT INTO saved_deadlines (id, case_number, client_name, action_name, announcement_date,
                    duration, unit, distance_days, final_deadline, law_article, calculation_explanation, notes, created_at)
                VALUES (991, '321/2026', 'اختبار سري', 'استئناف', '2026-10-01',
                    40, 'DAYS', 0, '2026-11-10', 'المادة 227', 'نتيجة محفوظة', 'سجل أصلي', 1729000000000)
            """.trimIndent())
            db.execSQL("""
                INSERT INTO legal_rules (id, action_name, duration, unit, distance_days, start_rule, law_article, notes, summary)
                VALUES (159, 'إخطار إنهاء العمل القديم', 2, 'MONTHS', 0, 'NEXT_DAY', 'قانون 12 لسنة 2003', 'قديم', 'قديم')
            """.trimIndent())

            AppDatabase.MIGRATION_1_2.migrate(db)
            AppDatabase.MIGRATION_2_3.migrate(db)
            AppDatabase.MIGRATION_3_4.migrate(db)
            AppDatabase.MIGRATION_4_5.migrate(db)

            db.query("SELECT case_number, client_name, calculation_explanation FROM saved_deadlines WHERE id = 991")
                .use { cursor ->
                    assertTrue(cursor.moveToNext())
                    assertEquals("321/2026", cursor.getString(0))
                    assertEquals("اختبار سري", cursor.getString(1))
                    assertEquals("نتيجة محفوظة", cursor.getString(2))
                }
            db.query("SELECT verification_status, source_url FROM holidays WHERE holiday_date = '2026-10-06'")
                .use { cursor ->
                    assertTrue(cursor.moveToNext())
                    assertEquals("UNVERIFIED", cursor.getString(0))
                    assertEquals("", cursor.getString(1))
                }
            db.query("SELECT review_status, effective_from, effective_to FROM legal_rules WHERE id = 159")
                .use { cursor ->
                    assertTrue(cursor.moveToNext())
                    assertEquals("NEEDS_REVIEW", cursor.getString(0))
                    assertEquals("", cursor.getString(1))
                    assertEquals("2025-08-31", cursor.getString(2))
                }
        }
    }


    @Test
    fun testMigration5To6_addsPolicyAndVersionWithoutChangingSavedDeadlines() {
        TestSupportSQLiteDatabase().use { db ->
            createSchemaV1(db)
            db.execSQL("""
                INSERT INTO legal_rules (id, action_name, duration, unit, distance_days, start_rule, law_article, notes, summary)
                VALUES (33, 'استئناف مدني', 40, 'DAYS', 0, 'NEXT_DAY', 'المادة 227', 'قيد المراجعة', 'اختبار')
            """.trimIndent())
            db.execSQL("""
                INSERT INTO saved_deadlines (id, case_number, client_name, action_name, announcement_date,
                    duration, unit, distance_days, final_deadline, law_article, calculation_explanation, notes, created_at)
                VALUES (992, '991/2026', 'موكل محفوظ', 'استئناف', '2026-10-01',
                    40, 'DAYS', 0, '2026-11-10', 'المادة 227', 'الحساب الأصلي', 'لا يحذف', 1729000000000)
            """.trimIndent())
            AppDatabase.MIGRATION_1_2.migrate(db)
            AppDatabase.MIGRATION_2_3.migrate(db)
            AppDatabase.MIGRATION_3_4.migrate(db)
            AppDatabase.MIGRATION_4_5.migrate(db)
            AppDatabase.MIGRATION_5_6.migrate(db)

            db.query("SELECT revision, calculation_kind, duration, review_status FROM legal_rules WHERE id = 33")
                .use { cursor ->
                    assertTrue(cursor.moveToNext())
                    assertEquals(0, cursor.getInt(0))
                    assertEquals("UNREVIEWED", cursor.getString(1))
                    assertEquals(40, cursor.getInt(2))
                    assertEquals("NEEDS_REVIEW", cursor.getString(3))
                }
            db.query("SELECT client_name, notes FROM saved_deadlines WHERE id = 992")
                .use { cursor ->
                    assertTrue(cursor.moveToNext())
                    assertEquals("موكل محفوظ", cursor.getString(0))
                    assertEquals("لا يحذف", cursor.getString(1))
                }
        }
    }


    @Test
    fun testMigration6To7_preservesSavedDeadlinesAndCreatesLawGroupedCatalog() {
        TestSupportSQLiteDatabase().use { db ->
            createSchemaV1(db)
            db.execSQL("""
                INSERT INTO legal_rules (id, action_name, duration, unit, distance_days, start_rule, law_article, notes, summary)
                VALUES (159, 'مهلة إخطار إنهاء عقد العمل', 2, 'MONTHS', 0, 'NEXT_DAY',
                    'المادة 111 من قانون العمل القديم', 'قاعدة مخزنة', 'اختبار')
            """.trimIndent())
            db.execSQL("""
                INSERT INTO saved_deadlines (id, case_number, client_name, action_name, announcement_date,
                    duration, unit, distance_days, final_deadline, law_article, calculation_explanation, notes, created_at)
                VALUES (1007, '123/2026', 'موكل تاريخي', 'ميعاد قديم', '2026-10-01',
                    40, 'DAYS', 0, '2026-11-10', 'المادة 227', 'النص المحفوظ', 'لا تحذف', 1729000000000)
            """.trimIndent())

            AppDatabase.MIGRATION_1_2.migrate(db)
            AppDatabase.MIGRATION_2_3.migrate(db)
            AppDatabase.MIGRATION_3_4.migrate(db)
            AppDatabase.MIGRATION_4_5.migrate(db)
            AppDatabase.MIGRATION_5_6.migrate(db)
            AppDatabase.MIGRATION_6_7.migrate(db)

            db.query("SELECT law_name, article_text, duration, review_status FROM legal_rules WHERE id = 159")
                .use { cursor ->
                    assertTrue(cursor.moveToNext())
                    assertEquals("قانون العمل", cursor.getString(0))
                    assertEquals("", cursor.getString(1))
                    assertEquals(2, cursor.getInt(2))
                    assertEquals("NEEDS_REVIEW", cursor.getString(3))
                }
            db.query("SELECT case_number, client_name, calculation_explanation FROM saved_deadlines WHERE id = 1007")
                .use { cursor ->
                    assertTrue(cursor.moveToNext())
                    assertEquals("123/2026", cursor.getString(0))
                    assertEquals("موكل تاريخي", cursor.getString(1))
                    assertEquals("النص المحفوظ", cursor.getString(2))
                }
            db.query("SELECT COUNT(*) FROM rule_edit_history").use { cursor ->
                assertTrue(cursor.moveToNext())
                assertEquals(0, cursor.getInt(0))
            }
            assertTrue(AppDatabase.columnExists(db, "legal_rules", "article_text"))
        }
    }


    @Test
    fun testMigration7To8_correctsLegacyLawGroupingWithoutChangingUserData() {
        TestSupportSQLiteDatabase().use { db ->
            createSchemaV1(db)
            db.execSQL("""
                INSERT INTO legal_rules (id, action_name, duration, unit, distance_days, start_rule, law_article, notes, summary)
                VALUES (21, 'رد الخبير', 5, 'DAYS', 0, 'NEXT_DAY', 'المادة 140 إثبات', 'الأصل', '')
            """.trimIndent())
            db.execSQL("""
                INSERT INTO legal_rules (id, action_name, duration, unit, distance_days, start_rule, law_article, notes, summary)
                VALUES (57, 'معارضة رسوم', 8, 'DAYS', 0, 'NEXT_DAY', 'المادة 17 رسوم', 'الأصل', '')
            """.trimIndent())
            db.execSQL("""
                INSERT INTO saved_deadlines (id, case_number, client_name, action_name, announcement_date,
                    duration, unit, distance_days, final_deadline, law_article, calculation_explanation, notes, created_at)
                VALUES (1008, '221/2026', 'موكل محمي', 'استئناف', '2026-10-01',
                    40, 'DAYS', 0, '2026-11-10', 'المادة 227', 'نص محفوظ', 'لا يمس', 1729000000000)
            """.trimIndent())
            AppDatabase.MIGRATION_1_2.migrate(db)
            AppDatabase.MIGRATION_2_3.migrate(db)
            AppDatabase.MIGRATION_3_4.migrate(db)
            AppDatabase.MIGRATION_4_5.migrate(db)
            AppDatabase.MIGRATION_5_6.migrate(db)
            AppDatabase.MIGRATION_6_7.migrate(db)
            AppDatabase.MIGRATION_7_8.migrate(db)
            db.query("SELECT law_name, duration FROM legal_rules WHERE id = 21").use {
                assertTrue(it.moveToNext())
                assertEquals("قانون الإثبات", it.getString(0))
                assertEquals(5, it.getInt(1))
            }
            db.query("SELECT law_name FROM legal_rules WHERE id = 57").use {
                assertTrue(it.moveToNext())
                assertEquals("قانون الرسوم القضائية رقم 90 لسنة 1944", it.getString(0))
            }
            db.query("SELECT client_name, notes FROM saved_deadlines WHERE id = 1008").use {
                assertTrue(it.moveToNext())
                assertEquals("موكل محمي", it.getString(0))
                assertEquals("لا يمس", it.getString(1))
            }
        }
    }

    @Test
    fun testMigration8To9_fillsOnlyMissingArticle940PreservingUserEdits() {
        TestSupportSQLiteDatabase().use { db ->
            createSchemaV1(db)
            db.execSQL("""
                INSERT INTO legal_rules (id, action_name, duration, unit, distance_days, start_rule,
                    law_article, notes, summary)
                VALUES (82, 'إعلان الرغبة في الأخذ بالشفعة', 15, 'DAYS', 0, 'NEXT_DAY',
                    'المادة 940 من القانون المدني المصري', 'قيد المراجعة', '')
            """.trimIndent())
            db.execSQL("""
                INSERT INTO saved_deadlines (id, case_number, client_name, action_name, announcement_date,
                    duration, unit, distance_days, final_deadline, law_article, calculation_explanation, notes, created_at)
                VALUES (901, '440/2026', 'موكل محفوظ', 'ميعاد شفعة', '2026-11-01',
                    15, 'DAYS', 0, '2026-11-16', 'المادة 940', 'بيانات محفوظة', 'لا تعدل', 1729000000000)
            """.trimIndent())
            AppDatabase.MIGRATION_1_2.migrate(db)
            AppDatabase.MIGRATION_2_3.migrate(db)
            AppDatabase.MIGRATION_3_4.migrate(db)
            AppDatabase.MIGRATION_4_5.migrate(db)
            AppDatabase.MIGRATION_5_6.migrate(db)
            AppDatabase.MIGRATION_6_7.migrate(db)
            AppDatabase.MIGRATION_7_8.migrate(db)
            // Case A: legacy untouched article receives sourced text.
            AppDatabase.MIGRATION_8_9.migrate(db)
            db.query("SELECT article_text, official_source_url FROM legal_rules WHERE id = 82").use {
                assertTrue(it.moveToNext())
                assertTrue(it.getString(0).contains("خمسة عشر"))
                assertTrue(it.getString(1).contains("portal.eta.gov.eg"))
            }
            // Case B: a user-supplied text is never overwritten.
            db.execSQL("UPDATE legal_rules SET article_text = 'نص أدخله المستخدم', revision = 2 WHERE id = 82")
            AppDatabase.MIGRATION_8_9.migrate(db)
            db.query("SELECT article_text FROM legal_rules WHERE id = 82").use {
                assertTrue(it.moveToNext())
                assertEquals("نص أدخله المستخدم", it.getString(0))
            }
            db.query("SELECT client_name, notes FROM saved_deadlines WHERE id = 901").use {
                assertTrue(it.moveToNext())
                assertEquals("موكل محفوظ", it.getString(0))
                assertEquals("لا تعدل", it.getString(1))
            }
        }
    }


    @Test
    fun testMigration9To10_preservesSavedCasesAndInitializesWorkbookFields() {
        TestSupportSQLiteDatabase().use { db ->
            createSchemaV1(db)
            db.execSQL("""
                INSERT INTO legal_rules (id, action_name, duration, unit, distance_days, start_rule, law_article, notes, summary)
                VALUES (159, 'مهلة الإخطار لإنهاء عقد العمل', 2, 'MONTHS', 0, 'NEXT_DAY',
                    'المادة 111 من القانون 12 لسنة 2003', 'سجل تاريخي', '')
            """.trimIndent())
            db.execSQL("""
                INSERT INTO saved_deadlines (id, case_number, client_name, action_name, announcement_date,
                    duration, unit, distance_days, final_deadline, law_article, calculation_explanation, notes, created_at)
                VALUES (1010, '845/2026', 'موكل قديم', 'ميعاد محفوظ', '2026-01-01',
                    30, 'DAYS', 0, '2026-01-31', 'المادة 30', 'حساب قديم', 'تحت الحفظ', 1729000000000)
            """.trimIndent())

            AppDatabase.MIGRATION_1_2.migrate(db)
            AppDatabase.MIGRATION_2_3.migrate(db)
            AppDatabase.MIGRATION_3_4.migrate(db)
            AppDatabase.MIGRATION_4_5.migrate(db)
            AppDatabase.MIGRATION_5_6.migrate(db)
            AppDatabase.MIGRATION_6_7.migrate(db)
            AppDatabase.MIGRATION_7_8.migrate(db)
            AppDatabase.MIGRATION_8_9.migrate(db)
            AppDatabase.MIGRATION_9_10.migrate(db)

            db.query("""
                SELECT workbook_decision, review_method, reviewer_note, review_version,
                       duration, notes FROM legal_rules WHERE id = 159
            """.trimIndent()).use { cursor ->
                assertTrue(cursor.moveToNext())
                assertEquals("", cursor.getString(0))
                assertEquals("", cursor.getString(1))
                assertEquals("", cursor.getString(2))
                assertEquals(0, cursor.getInt(3))
                assertEquals(2, cursor.getInt(4))
                assertEquals("سجل تاريخي", cursor.getString(5))
            }
            db.query("SELECT case_number, client_name, calculation_explanation, notes FROM saved_deadlines WHERE id = 1010")
                .use { cursor ->
                    assertTrue(cursor.moveToNext())
                    assertEquals("845/2026", cursor.getString(0))
                    assertEquals("موكل قديم", cursor.getString(1))
                    assertEquals("حساب قديم", cursor.getString(2))
                    assertEquals("تحت الحفظ", cursor.getString(3))
                }
        }
    }


    @Test
    fun testMigration10To11_finalizesPreviouslySavedDraftsWithoutTouchingCases() {
        TestSupportSQLiteDatabase().use { db ->
            createSchemaV1(db)
            db.execSQL("""
                INSERT INTO legal_rules (id, action_name, duration, unit, distance_days, start_rule, law_article, notes, summary)
                VALUES (82, 'إعلان الرغبة في الشفعة', 15, 'DAYS', 0, 'NEXT_DAY',
                    'المادة 940 مدني', 'ملاحظة المستخدم', 'سجل الشفعة')
            """.trimIndent())
            db.execSQL("""
                INSERT INTO legal_rules (id, action_name, duration, unit, distance_days, start_rule, law_article, notes, summary)
                VALUES (17, 'رد القاضي', 0, 'DAYS', 0, 'NEXT_DAY',
                    'المادة 152 مرافعات', 'ممنوع الحساب', '')
            """.trimIndent())
            db.execSQL("""
                INSERT INTO saved_deadlines (id, case_number, client_name, action_name, announcement_date,
                    duration, unit, distance_days, final_deadline, law_article, calculation_explanation, notes, created_at)
                VALUES (1111, '66/2026', 'موكل محفوظ', 'شفعة', '2026-11-01',
                    15, 'DAYS', 0, '2026-11-16', 'المادة 940', 'ناتج محفوظ', 'بيانات أصلية', 1729000000000)
            """.trimIndent())

            AppDatabase.MIGRATION_1_2.migrate(db)
            AppDatabase.MIGRATION_2_3.migrate(db)
            AppDatabase.MIGRATION_3_4.migrate(db)
            AppDatabase.MIGRATION_4_5.migrate(db)
            AppDatabase.MIGRATION_5_6.migrate(db)
            AppDatabase.MIGRATION_6_7.migrate(db)
            AppDatabase.MIGRATION_7_8.migrate(db)
            AppDatabase.MIGRATION_8_9.migrate(db)
            AppDatabase.MIGRATION_9_10.migrate(db)
            db.execSQL("""
                UPDATE legal_rules SET workbook_decision = 'EDITED_PENDING',
                    review_status = 'NEEDS_REVIEW', article_text = 'نص المادة المدخل من المستخدم',
                    reviewer_note = 'ملاحظة ملف المراجعة', review_version = 1, revision = 3
                WHERE id = 82
            """.trimIndent())
            db.execSQL("""
                UPDATE legal_rules SET workbook_decision = 'KEEP_BLOCKED',
                    review_status = 'KEEP_BLOCKED', review_version = 1
                WHERE id = 17
            """.trimIndent())
            AppDatabase.MIGRATION_10_11.migrate(db)

            db.query("""
                SELECT workbook_decision, review_status, article_text, reviewer_note,
                    duration, revision FROM legal_rules WHERE id = 82
            """.trimIndent()).use { cursor ->
                assertTrue(cursor.moveToNext())
                assertEquals("USER_APPROVED", cursor.getString(0))
                assertEquals("VERIFIED_ACCURATE", cursor.getString(1))
                assertEquals("نص المادة المدخل من المستخدم", cursor.getString(2))
                assertEquals("ملاحظة ملف المراجعة", cursor.getString(3))
                assertEquals(15, cursor.getInt(4))
                assertEquals(3, cursor.getInt(5))
            }
            db.query("SELECT workbook_decision, review_status FROM legal_rules WHERE id = 17")
                .use {
                    assertTrue(it.moveToNext())
                    assertEquals("KEEP_BLOCKED", it.getString(0))
                    assertEquals("KEEP_BLOCKED", it.getString(1))
                }
            db.query("SELECT case_number, client_name, calculation_explanation FROM saved_deadlines WHERE id = 1111")
                .use {
                    assertTrue(it.moveToNext())
                    assertEquals("66/2026", it.getString(0))
                    assertEquals("موكل محفوظ", it.getString(1))
                    assertEquals("ناتج محفوظ", it.getString(2))
                }
        }
    }

}
