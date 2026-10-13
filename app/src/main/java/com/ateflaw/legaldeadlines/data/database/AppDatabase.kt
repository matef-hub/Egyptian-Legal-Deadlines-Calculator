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
    version = 3,
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

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "egyptian_legal_deadlines.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
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
