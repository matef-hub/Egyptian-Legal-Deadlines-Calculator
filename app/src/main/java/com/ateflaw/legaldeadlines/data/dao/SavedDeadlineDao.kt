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

    @Query("SELECT * FROM saved_deadlines ORDER BY created_at DESC")
    fun getAllSortedByCreatedAt(): Flow<List<SavedDeadlineEntity>>

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
