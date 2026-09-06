package com.ateflaw.legaldeadlines.data.repository

import com.ateflaw.legaldeadlines.data.dao.SavedDeadlineDao
import com.ateflaw.legaldeadlines.data.entity.SavedDeadlineEntity
import com.ateflaw.legaldeadlines.domain.model.SavedDeadline
import com.ateflaw.legaldeadlines.domain.repository.DeadlineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DeadlineRepositoryImpl(
    private val savedDeadlineDao: SavedDeadlineDao
) : DeadlineRepository {

    override fun getAllSavedDeadlines(): Flow<List<SavedDeadline>> {
        return savedDeadlineDao.getAllSortedByCreatedAt().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getDeadlineById(id: Long): SavedDeadline? {
        return savedDeadlineDao.getById(id)?.toDomain()
    }

    override suspend fun insertDeadline(deadline: SavedDeadline): Long {
        return savedDeadlineDao.insert(SavedDeadlineEntity.fromDomain(deadline))
    }

    override suspend fun updateDeadline(deadline: SavedDeadline) {
        savedDeadlineDao.update(SavedDeadlineEntity.fromDomain(deadline))
    }

    override suspend fun deleteDeadline(deadline: SavedDeadline) {
        savedDeadlineDao.delete(SavedDeadlineEntity.fromDomain(deadline))
    }

    override suspend fun deleteDeadlineById(id: Long) {
        savedDeadlineDao.deleteById(id)
    }
}
