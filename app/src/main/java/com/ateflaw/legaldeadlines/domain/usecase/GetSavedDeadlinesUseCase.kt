package com.ateflaw.legaldeadlines.domain.usecase

import com.ateflaw.legaldeadlines.domain.model.SavedDeadline
import com.ateflaw.legaldeadlines.domain.repository.DeadlineRepository
import kotlinx.coroutines.flow.Flow

class GetSavedDeadlinesUseCase(
    private val repository: DeadlineRepository
) {
    operator fun invoke(): Flow<List<SavedDeadline>> = repository.getAllSavedDeadlines()
}
