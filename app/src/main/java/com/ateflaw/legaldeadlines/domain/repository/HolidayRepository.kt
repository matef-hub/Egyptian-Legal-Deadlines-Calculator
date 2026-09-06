package com.ateflaw.legaldeadlines.domain.repository

import com.ateflaw.legaldeadlines.domain.model.Holiday
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Domain repository abstraction for official Egyptian holidays.
 */
interface HolidayRepository {
    fun getAllHolidays(): Flow<List<Holiday>>
    suspend fun getHolidaysCount(): Int
    suspend fun getHolidayByDate(date: LocalDate): Holiday?
    suspend fun isHoliday(date: LocalDate): Boolean
    suspend fun getHolidayName(date: LocalDate): String?
    suspend fun insertHolidays(holidays: List<Holiday>)
    suspend fun syncHolidays(year: Int): Result<Int>
}
