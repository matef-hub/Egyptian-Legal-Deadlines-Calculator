package com.ateflaw.legaldeadlines.domain.usecase

import com.ateflaw.legaldeadlines.domain.model.Holiday
import com.ateflaw.legaldeadlines.domain.repository.HolidayRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class GetHolidaysUseCase(
    private val repository: HolidayRepository
) {
    operator fun invoke(): Flow<List<Holiday>> = repository.getAllHolidays()

    suspend fun isHoliday(date: LocalDate): Boolean = repository.isHoliday(date)

    suspend fun getHolidayName(date: LocalDate): String? = repository.getHolidayName(date)

    suspend fun getCount(): Int = repository.getHolidaysCount()

    suspend fun insertHolidays(holidays: List<Holiday>) = repository.insertHolidays(holidays)
}
