package com.ateflaw.legaldeadlines.domain.usecase

import com.ateflaw.legaldeadlines.domain.model.SavedDeadline
import com.ateflaw.legaldeadlines.domain.repository.DeadlineRepository

class SaveDeadlineUseCase(
    private val repository: DeadlineRepository
) {
    suspend operator fun invoke(deadline: SavedDeadline): Long {
        return repository.insertDeadline(deadline)
    }
}
