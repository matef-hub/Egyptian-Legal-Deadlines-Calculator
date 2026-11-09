package com.ateflaw.legaldeadlines.domain.usecase

import com.ateflaw.legaldeadlines.domain.model.SavedDeadline
import com.ateflaw.legaldeadlines.domain.repository.DeadlineRepository

class DeleteSavedDeadlineUseCase(
    private val repository: DeadlineRepository
) {
    suspend operator fun invoke(deadline: SavedDeadline) {
        repository.deleteDeadline(deadline)
    }

    suspend fun byId(id: Long) {
        repository.deleteDeadlineById(id)
    }
}
