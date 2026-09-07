package com.ateflaw.legaldeadlines.data.repository

import com.ateflaw.legaldeadlines.data.service.OfficialHolidayService
import com.ateflaw.legaldeadlines.domain.model.Holiday
import com.ateflaw.legaldeadlines.domain.repository.HolidayRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class HolidayRepositoryImpl(
    private val holidayService: OfficialHolidayService
) : HolidayRepository {

    override fun getAllHolidays(): Flow<List<Holiday>> {
        return holidayService.getAllHolidays()
    }

    override suspend fun getAllHolidaysList(): List<Holiday> {
        return holidayService.getAllHolidaysList()
    }

    override suspend fun getHolidaysCount(): Int {
        return holidayService.getHolidaysCount()
    }

    override suspend fun getHolidayByDate(date: LocalDate): Holiday? {
        val name = holidayService.getHolidayName(date)
        return if (name != null) Holiday(holidayDate = date, name = name) else null
    }

    override suspend fun isHoliday(date: LocalDate): Boolean {
        return holidayService.isHoliday(date)
    }

    override suspend fun getHolidayName(date: LocalDate): String? {
        return holidayService.getHolidayName(date)
    }

    override suspend fun insertHolidays(holidays: List<Holiday>) {
        holidayService.insertHolidays(holidays)
    }

    override suspend fun syncHolidays(year: Int): Result<Int> {
        return holidayService.syncHolidays(year)
    }
}
