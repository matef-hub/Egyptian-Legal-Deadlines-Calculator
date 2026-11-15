package com.ateflaw.legaldeadlines.domain.usecase

import com.ateflaw.legaldeadlines.domain.repository.HolidayRepository
import java.time.LocalDate

class GetHolidaysUseCase(private val repository: HolidayRepository) {
    operator fun invoke() = repository.getAllHolidays()

    suspend fun officialHolidayOn(date: LocalDate) = repository.getHolidayByDate(date)
}
