package com.ateflaw.legaldeadlines.domain.repository

import com.ateflaw.legaldeadlines.domain.model.Holiday
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface HolidayRepository {
    fun getAllHolidays(): Flow<List<Holiday>>
    suspend fun getAllHolidaysList(): List<Holiday>
    suspend fun getHolidayByDate(date: LocalDate): Holiday?
    suspend fun syncHolidays(): Result<Int>
}
