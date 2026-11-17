package com.ateflaw.legaldeadlines.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ateflaw.legaldeadlines.data.entity.SavedDeadlineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedDeadlineDao {

    @Query("""
        SELECT * FROM (
            SELECT * FROM saved_deadlines WHERE final_deadline >= DATE('now', 'localtime')
            ORDER BY final_deadline ASC, created_at DESC, id DESC
        )
        UNION ALL SELECT * FROM (
            SELECT * FROM saved_deadlines WHERE final_deadline < DATE('now', 'localtime')
            ORDER BY final_deadline DESC, created_at DESC, id DESC
        )
    """)
    fun observeByDeadline(): Flow<List<SavedDeadlineEntity>>

    @Query("""
        SELECT * FROM saved_deadlines WHERE final_deadline >= :today
        AND proactive_reminder_days > 0
        AND DATE(final_deadline, '-' || proactive_reminder_days || ' days') <= :today
        AND (reminder_sent_for IS NULL OR reminder_sent_for != final_deadline || '/' || proactive_reminder_days)
        ORDER BY final_deadline, id
    """)
    suspend fun dueReminders(today: String): List<SavedDeadlineEntity>

    @Query("""
        UPDATE saved_deadlines SET reminder_sent_for = final_deadline || '/' || proactive_reminder_days
        WHERE id = :id AND final_deadline = :date AND proactive_reminder_days = :days
    """)
    suspend fun markReminderSent(id: Long, date: String, days: Int)

    @Query("SELECT * FROM saved_deadlines WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): SavedDeadlineEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(deadline: SavedDeadlineEntity): Long

    @Update
    suspend fun update(deadline: SavedDeadlineEntity)

    @Delete
    suspend fun delete(deadline: SavedDeadlineEntity)

    @Query("DELETE FROM saved_deadlines WHERE id = :id")
    suspend fun deleteById(id: Long)
}
