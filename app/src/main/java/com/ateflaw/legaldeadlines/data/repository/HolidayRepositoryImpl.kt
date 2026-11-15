package com.ateflaw.legaldeadlines.data.repository

import com.ateflaw.legaldeadlines.data.service.OfficialHolidayService
import com.ateflaw.legaldeadlines.domain.repository.HolidayRepository
import java.time.LocalDate

class HolidayRepositoryImpl(
    private val holidayService: OfficialHolidayService
) : HolidayRepository {
    override fun getAllHolidays() = holidayService.getAllHolidays()
    override suspend fun getAllHolidaysList() = holidayService.getAllHolidaysList()
    override suspend fun getHolidayByDate(date: LocalDate) = holidayService.getHoliday(date)
    override suspend fun syncHolidays() = holidayService.syncHolidays()
}
