package com.ateflaw.legaldeadlines.domain.usecase

import com.ateflaw.legaldeadlines.domain.repository.HolidayRepository

class SyncHolidaysUseCase(
    private val repository: HolidayRepository
) {
    suspend operator fun invoke(year: Int, forceRefresh: Boolean = false): Result<Int> {
        return repository.syncHolidays(year, forceRefresh)
    }
}
