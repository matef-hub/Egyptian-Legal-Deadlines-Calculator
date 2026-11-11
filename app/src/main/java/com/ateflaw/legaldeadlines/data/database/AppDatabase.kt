package com.ateflaw.legaldeadlines.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ateflaw.legaldeadlines.data.dao.HolidayDao
import com.ateflaw.legaldeadlines.data.dao.LegalRuleDao
import com.ateflaw.legaldeadlines.data.dao.SavedDeadlineDao
import com.ateflaw.legaldeadlines.data.entity.HolidayEntity
import com.ateflaw.legaldeadlines.data.entity.LegalRuleEntity
import com.ateflaw.legaldeadlines.data.entity.RuleEditHistoryEntity
import com.ateflaw.legaldeadlines.data.entity.SavedDeadlineEntity
import com.ateflaw.legaldeadlines.data.seed.LegalRuleSeedData
import com.ateflaw.legaldeadlines.data.review.ReviewedWorkbookImporter
import com.ateflaw.legaldeadlines.domain.review.LegalRuleReviewCatalog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        LegalRuleEntity::class,
        HolidayEntity::class,
        SavedDeadlineEntity::class,
        RuleEditHistoryEntity::class
    ],
    version = 10,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun legalRuleDao(): LegalRuleDao
    abstract fun holidayDao(): HolidayDao
    abstract fun savedDeadlineDao(): SavedDeadlineDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Checks whether a column exists in a given table.
         */
        fun columnExists(db: SupportSQLiteDatabase, tableName: String, columnName: String): Boolean {
            db.query("PRAGMA table_info(`$tableName`)").use { cursor ->
                val nameIndex = cursor.getColumnIndex("name")
                if (nameIndex != -1) {
                    while (cursor.moveToNext()) {
                        if (cursor.getString(nameIndex).equals(columnName, ignoreCase = true)) {
                            return true
                        }
                    }
                }
            }
            return false
        }

        /**
         * Migration 1 -> 2:
         * Ensure unique index on holidays.holiday_date exists to prevent duplicate dates.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Safeguard against duplicate dates in existing user databases before creating the unique index
                db.execSQL("DELETE FROM holidays WHERE id NOT IN (SELECT MIN(id) FROM holidays GROUP BY holiday_date)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_holidays_holiday_date ON holidays (holiday_date)")
            }
        }

        /**
         * Migration 2 -> 3:
         * Add proactive_reminder_days column to saved_deadlines.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                if (!columnExists(db, "saved_deadlines", "proactive_reminder_days")) {
                    db.execSQL("ALTER TABLE saved_deadlines ADD COLUMN proactive_reminder_days INTEGER DEFAULT NULL")
                }
            }
        }

        /**
         * Migration 3 -> 4:
         * Add full legal rule metadata columns and saved deadline audit trail fields.
         * Existing unreviewed records are labeled 'NEEDS_REVIEW' to maintain legal integrity.
         */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add columns to legal_rules atomically
                if (!columnExists(db, "legal_rules", "trigger_event_type")) {
                    db.execSQL("ALTER TABLE legal_rules ADD COLUMN trigger_event_type TEXT NOT NULL DEFAULT 'ANNOUNCEMENT'")
                }
                if (!columnExists(db, "legal_rules", "trigger_event_label")) {
                    db.execSQL("ALTER TABLE legal_rules ADD COLUMN trigger_event_label TEXT NOT NULL DEFAULT 'تاريخ الإعلان أو صدور الحكم'")
                }
                if (!columnExists(db, "legal_rules", "court_type")) {
                    db.execSQL("ALTER TABLE legal_rules ADD COLUMN court_type TEXT NOT NULL DEFAULT 'مدني وتجاري'")
                }
                if (!columnExists(db, "legal_rules", "category")) {
                    db.execSQL("ALTER TABLE legal_rules ADD COLUMN category TEXT NOT NULL DEFAULT 'طرق الطعن'")
                }
                if (!columnExists(db, "legal_rules", "distance_policy")) {
                    db.execSQL("ALTER TABLE legal_rules ADD COLUMN distance_policy TEXT NOT NULL DEFAULT 'APPLICABLE'")
                }
                if (!columnExists(db, "legal_rules", "extension_policy")) {
                    db.execSQL("ALTER TABLE legal_rules ADD COLUMN extension_policy TEXT NOT NULL DEFAULT 'ARTICLE_18_COURTS'")
                }
                if (!columnExists(db, "legal_rules", "law_name")) {
                    db.execSQL("ALTER TABLE legal_rules ADD COLUMN law_name TEXT NOT NULL DEFAULT 'قانون المرافعات المدنية والتجارية'")
                }
                if (!columnExists(db, "legal_rules", "article")) {
                    db.execSQL("ALTER TABLE legal_rules ADD COLUMN article TEXT NOT NULL DEFAULT ''")
                }
                if (!columnExists(db, "legal_rules", "official_source_url")) {
                    db.execSQL("ALTER TABLE legal_rules ADD COLUMN official_source_url TEXT NOT NULL DEFAULT ''")
                }
                if (!columnExists(db, "legal_rules", "effective_from")) {
                    db.execSQL("ALTER TABLE legal_rules ADD COLUMN effective_from TEXT NOT NULL DEFAULT '1968-11-01'")
                }
                if (!columnExists(db, "legal_rules", "effective_to")) {
                    db.execSQL("ALTER TABLE legal_rules ADD COLUMN effective_to TEXT DEFAULT NULL")
                }
                if (!columnExists(db, "legal_rules", "last_reviewed_at")) {
                    db.execSQL("ALTER TABLE legal_rules ADD COLUMN last_reviewed_at TEXT NOT NULL DEFAULT '2026-10-13'")
                }
                if (!columnExists(db, "legal_rules", "review_status")) {
                    // Do not automatically mark pre-existing records as VERIFIED_ACCURATE
                    db.execSQL("ALTER TABLE legal_rules ADD COLUMN review_status TEXT NOT NULL DEFAULT 'NEEDS_REVIEW'")
                }

                // Add columns to saved_deadlines
                if (!columnExists(db, "saved_deadlines", "rule_snapshot")) {
                    db.execSQL("ALTER TABLE saved_deadlines ADD COLUMN rule_snapshot TEXT NOT NULL DEFAULT ''")
                }
                if (!columnExists(db, "saved_deadlines", "distance_type")) {
                    db.execSQL("ALTER TABLE saved_deadlines ADD COLUMN distance_type TEXT NOT NULL DEFAULT 'NONE'")
                }
            }
        }

        /**
         * Direct Migration 1 -> 4 for upgrade paths skipping intermediate versions.
         */
        val MIGRATION_1_4 = object : Migration(1, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                MIGRATION_1_2.migrate(db)
                MIGRATION_2_3.migrate(db)
                MIGRATION_3_4.migrate(db)
            }
        }

        /**
         * Direct Migration 2 -> 4 for upgrade paths skipping version 3.
         */
        val MIGRATION_2_4 = object : Migration(2, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                MIGRATION_2_3.migrate(db)
                MIGRATION_3_4.migrate(db)
            }
        }

        /**
         * Preserve every saved deadline. Existing holiday rows have no official
         * publication provenance, so migration never silently marks them verified.
         */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE holidays ADD COLUMN verification_status TEXT NOT NULL DEFAULT 'UNVERIFIED'")
                db.execSQL("ALTER TABLE holidays ADD COLUMN source_url TEXT NOT NULL DEFAULT ''")
                db.execSQL("UPDATE legal_rules SET effective_from = '' WHERE effective_from = '1968-11-01'")
                db.execSQL("UPDATE legal_rules SET effective_to = '2025-08-31', review_status = 'NEEDS_REVIEW' WHERE id BETWEEN 147 AND 167 AND (effective_to IS NULL OR effective_to = '')")
                db.execSQL("UPDATE legal_rules SET effective_to = '2027-09-30', review_status = 'NEEDS_REVIEW' WHERE id BETWEEN 92 AND 128 AND (effective_to IS NULL OR effective_to = '')")
            }
        }

        /**
         * Revision/policy metadata is non-destructive and only enables explicitly
         * classified, independently reviewed rule variants.
         */
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE legal_rules ADD COLUMN revision INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE legal_rules ADD COLUMN calculation_kind TEXT NOT NULL DEFAULT 'UNREVIEWED'")
            }
        }

        /**
         * 6 -> 7: full article text and revision history. Existing saved deadlines
         * are not modified, and unreviewed legal rules stay unreviewed.
         */
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE legal_rules ADD COLUMN article_text TEXT NOT NULL DEFAULT ''")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS rule_edit_history (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        rule_id INTEGER NOT NULL,
                        previous_revision INTEGER NOT NULL,
                        action_name TEXT NOT NULL,
                        law_name TEXT NOT NULL,
                        law_article TEXT NOT NULL,
                        article_text TEXT NOT NULL,
                        duration INTEGER NOT NULL,
                        unit TEXT NOT NULL,
                        notes TEXT NOT NULL,
                        review_status TEXT NOT NULL,
                        official_source_url TEXT NOT NULL,
                        edited_at INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_rule_edit_history_rule_id ON rule_edit_history(rule_id)")
                // Historical seed rows were accidentally all classified as Civil Procedures.
                // Only correct known groups that still bear that legacy default category.
                db.execSQL("""
                    UPDATE legal_rules SET law_name = CASE
                        WHEN id = 3 THEN 'قانون الإثبات'
                        WHEN id BETWEEN 59 AND 91 THEN 'القانون المدني'
                        WHEN id BETWEEN 92 AND 127 THEN 'قانون الإجراءات الجنائية'
                        WHEN id = 128 THEN 'قانون حالات وإجراءات الطعن أمام محكمة النقض رقم 57 لسنة 1959'
                        WHEN id BETWEEN 129 AND 135 THEN 'قانون الأحوال الشخصية رقم 25 لسنة 1929'
                        WHEN id BETWEEN 136 AND 141 THEN 'قانون تنظيم إجراءات التقاضي في الأحوال الشخصية رقم 1 لسنة 2000'
                        WHEN id = 142 THEN 'قانون إنشاء محاكم الأسرة رقم 10 لسنة 2004'
                        WHEN id BETWEEN 143 AND 146 THEN 'قانون مجلس الدولة'
                        WHEN id BETWEEN 147 AND 167 THEN 'قانون العمل'
                        WHEN id BETWEEN 168 AND 169 THEN 'قانون البناء'
                        ELSE law_name
                    END
                    WHERE revision <= 1 AND law_name = 'قانون المرافعات المدنية والتجارية'
                      AND (id = 3 OR id BETWEEN 59 AND 169)
                """.trimIndent())
            }
        }

        /**
         * v7 -> v8: correct two law-family labels without overwriting any
         * user-edited rule or changing its duration, evidence or approval.
         */
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    UPDATE legal_rules SET law_name = 'قانون الإثبات'
                    WHERE id BETWEEN 21 AND 24
                      AND revision <= 1
                      AND law_name = 'قانون المرافعات المدنية والتجارية'
                """.trimIndent())
                db.execSQL("""
                    UPDATE legal_rules SET law_name = 'قانون الرسوم القضائية رقم 90 لسنة 1944'
                    WHERE id BETWEEN 57 AND 58
                      AND revision <= 1
                      AND law_name = 'قانون المرافعات المدنية والتجارية'
                """.trimIndent())
            }
        }

        /**
         * 8 -> 9: fill the previously missing official article 940 text only in
         * untouched legacy rule #82. Never replace a user-supplied text or
         * modified version. No client or saved-deadline data is changed.
         */
        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    UPDATE legal_rules SET
                        article_text = ?,
                        official_source_url = ?,
                        trigger_event_label = 'تاريخ الإنذار الرسمي بالبيع للشفيع'
                    WHERE id = 82
                      AND revision <= 1
                      AND article_text = ''
                      AND law_article LIKE '%940%'
                      AND duration = 15
                      AND unit = 'DAYS'
                """.trimIndent(), arrayOf(
                    LegalRuleReviewCatalog.ARTICLE_940_TEXT,
                    LegalRuleReviewCatalog.ARTICLE_940_SOURCE
                ))
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE legal_rules ADD COLUMN workbook_decision TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE legal_rules ADD COLUMN review_method TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE legal_rules ADD COLUMN reviewer_note TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE legal_rules ADD COLUMN review_version INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "egyptian_legal_deadlines.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_1_4, MIGRATION_2_4)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            seedIfEmpty(context)
                        }

                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            seedIfEmpty(context)
                        }

                        private fun seedIfEmpty(context: Context) {
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val database = getInstance(context)
                                    val ruleDao = database.legalRuleDao()
                                    val catalog = LegalRuleSeedData.getSeedRules()
                                    if (ruleDao.count() == 0) {
                                        ruleDao.insertAll(catalog.map { LegalRuleEntity.fromDomain(it) })
                                    } else {
                                        // Existing records are never overwritten. Only verified
                                        // legacy IDs/names with revision=0 receive safety metadata.
                                        for (rule in catalog) {
                                            ruleDao.adoptSeedMetadata(
                                                id = rule.id,
                                                expectedName = rule.actionName,
                                                expiresAt = rule.effectiveTo,
                                                revision = rule.revision,
                                                kind = rule.calculationKind
                                            )
                                        }
                                    }
                                    // Safe 169-rule review import (independent of Cassation cases).
                                    ReviewedWorkbookImporter.apply(context.applicationContext, database)
                                    // Merge sourced national dates into existing databases
                                    // without replacing another officially verified decision.
                                    val officialSeeds = LegalRuleSeedData.getInitialOfficialHolidays()
                                    val holidayDao = database.holidayDao()
                                    val missingOrUnverified = officialSeeds.filter { holiday ->
                                        val current = holidayDao.getHoliday(holiday.holidayDate.toString())
                                        current == null || current.verificationStatus != "VERIFIED_OFFICIAL"
                                    }
                                    if (missingOrUnverified.isNotEmpty()) {
                                        holidayDao.insertAll(missingOrUnverified.map { HolidayEntity.fromDomain(it) })
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("AppDatabase", "Database seeding failed", e)
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
