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
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun legalRuleDao(): LegalRuleDao
    abstract fun holidayDao(): HolidayDao
    abstract fun savedDeadlineDao(): SavedDeadlineDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Version 1 to 2 schema migration
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add proactive_reminder_days column without wiping existing client deadline data
                try {
                    db.execSQL("ALTER TABLE saved_deadlines ADD COLUMN proactive_reminder_days INTEGER DEFAULT NULL")
                } catch (_: Exception) {
                    // Column might already exist
                }
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE legal_rules ADD COLUMN trigger_event_type TEXT NOT NULL DEFAULT 'ANNOUNCEMENT'")
                    db.execSQL("ALTER TABLE legal_rules ADD COLUMN trigger_event_label TEXT NOT NULL DEFAULT 'تاريخ الإعلان أو صدور الحكم'")
                    db.execSQL("ALTER TABLE legal_rules ADD COLUMN court_type TEXT NOT NULL DEFAULT 'مدني وتجاري'")
                    db.execSQL("ALTER TABLE legal_rules ADD COLUMN category TEXT NOT NULL DEFAULT 'طرق الطعن'")
                    db.execSQL("ALTER TABLE legal_rules ADD COLUMN distance_policy TEXT NOT NULL DEFAULT 'APPLICABLE'")
                    db.execSQL("ALTER TABLE legal_rules ADD COLUMN extension_policy TEXT NOT NULL DEFAULT 'ARTICLE_18_COURTS'")
                    db.execSQL("ALTER TABLE legal_rules ADD COLUMN law_name TEXT NOT NULL DEFAULT 'قانون المرافعات المدنية والتجارية'")
                    db.execSQL("ALTER TABLE legal_rules ADD COLUMN article TEXT NOT NULL DEFAULT ''")
                    db.execSQL("ALTER TABLE legal_rules ADD COLUMN official_source_url TEXT NOT NULL DEFAULT ''")
                    db.execSQL("ALTER TABLE legal_rules ADD COLUMN effective_from TEXT NOT NULL DEFAULT '1968-11-01'")
                    db.execSQL("ALTER TABLE legal_rules ADD COLUMN effective_to TEXT DEFAULT NULL")
                    db.execSQL("ALTER TABLE legal_rules ADD COLUMN last_reviewed_at TEXT NOT NULL DEFAULT '2026-10-13'")
                    db.execSQL("ALTER TABLE legal_rules ADD COLUMN review_status TEXT NOT NULL DEFAULT 'VERIFIED_ACCURATE'")

                    db.execSQL("ALTER TABLE saved_deadlines ADD COLUMN rule_snapshot TEXT NOT NULL DEFAULT ''")
                    db.execSQL("ALTER TABLE saved_deadlines ADD COLUMN distance_type TEXT NOT NULL DEFAULT 'NONE'")
                } catch (_: Exception) {}
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "egyptian_legal_deadlines.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigrationOnDowngrade()
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
