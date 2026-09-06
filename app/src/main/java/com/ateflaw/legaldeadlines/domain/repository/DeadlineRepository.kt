package com.ateflaw.legaldeadlines.domain.repository

import com.ateflaw.legaldeadlines.domain.model.SavedDeadline
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository abstraction for saved legal deadlines.
 */
interface DeadlineRepository {
    fun getAllSavedDeadlines(): Flow<List<SavedDeadline>>
    suspend fun getDeadlineById(id: Long): SavedDeadline?
    suspend fun insertDeadline(deadline: SavedDeadline): Long
    suspend fun updateDeadline(deadline: SavedDeadline)
    suspend fun deleteDeadline(deadline: SavedDeadline)
    suspend fun deleteDeadlineById(id: Long)
}
