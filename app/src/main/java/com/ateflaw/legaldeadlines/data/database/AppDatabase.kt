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
import com.ateflaw.legaldeadlines.data.entity.SavedDeadlineEntity
import com.ateflaw.legaldeadlines.data.seed.LegalRuleSeedData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        LegalRuleEntity::class,
        HolidayEntity::class,
        SavedDeadlineEntity::class
    ],
    version = 4,
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

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "egyptian_legal_deadlines.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_1_4, MIGRATION_2_4)
                    // Destructive fallback intentionally omitted to prevent accidental loss of saved client deadlines
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
                                    if (database.legalRuleDao().count() == 0) {
                                        database.legalRuleDao().insertAll(
                                            LegalRuleSeedData.getSeedRules().map { LegalRuleEntity.fromDomain(it) }
                                        )
                                    }
                                    if (database.holidayDao().count() == 0) {
                                        database.holidayDao().insertAll(
                                            LegalRuleSeedData.getInitialOfficialHolidays().map { HolidayEntity.fromDomain(it) }
                                        )
                                    }
                                } catch (_: Exception) {}
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
