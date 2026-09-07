package com.ateflaw.legaldeadlines.data.service

import com.ateflaw.legaldeadlines.BuildConfig
import com.ateflaw.legaldeadlines.data.dao.HolidayDao
import com.ateflaw.legaldeadlines.data.entity.HolidayEntity
import com.ateflaw.legaldeadlines.data.remote.CalendarificApiService
import com.ateflaw.legaldeadlines.domain.model.Holiday
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate

/**
 * Service managing official Egyptian holidays:
 * - Reads holidays from Room
 * - Checks whether a date is an official holiday
 * - Returns holiday name
 * - Synchronizes holidays from Calendarific
 * - Updates Room idempotently
 * - Operates offline using cached Room data
 */
class OfficialHolidayService(
    private val holidayDao: HolidayDao,
    private val apiService: CalendarificApiService
) {

    fun getAllHolidays(): Flow<List<Holiday>> {
        return holidayDao.getAll().map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun getAllHolidaysList(): List<Holiday> = withContext(Dispatchers.IO) {
        holidayDao.getAllList().map { it.toDomain() }
    }

    suspend fun getHolidaysCount(): Int = withContext(Dispatchers.IO) {
        holidayDao.count()
    }

    suspend fun isHoliday(date: LocalDate): Boolean = withContext(Dispatchers.IO) {
        holidayDao.isHoliday(date.toString())
    }

    suspend fun getHolidayName(date: LocalDate): String? = withContext(Dispatchers.IO) {
        holidayDao.getHoliday(date.toString())?.name
    }

    suspend fun insertHolidays(holidays: List<Holiday>) = withContext(Dispatchers.IO) {
        holidayDao.insertAll(holidays.map { HolidayEntity.fromDomain(it) })
    }

    suspend fun syncHolidays(year: Int): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val apiKey = BuildConfig.CALENDARIFIC_API_KEY
            if (apiKey.isBlank() || apiKey == "YOUR_API_KEY") {
                // If API key is not configured, silently succeed with cached records without crashing
                return@withContext Result.success(holidayDao.count())
            }

            val response = apiService.getHolidays(
                apiKey = apiKey,
                country = "EG",
                year = year,
                type = "national"
            )

            if (response.isSuccessful) {
                val holidayDtos = response.body()?.response?.holidays ?: emptyList()
                val parsedHolidays = holidayDtos.mapNotNull { dto ->
                    val iso = dto.date?.iso
                    val name = dto.name ?: "عطلة رسمية"
                    if (iso != null) {
                        try {
                            val parsedDate = if (iso.length >= 10) {
                                LocalDate.parse(iso.substring(0, 10))
                            } else {
                                LocalDate.of(dto.date.datetime!!.year, dto.date.datetime.month, dto.date.datetime.day)
                            }
                            Holiday(
                                id = 0,
                                holidayDate = parsedDate,
                                name = name
                            )
                        } catch (_: Exception) {
                            null
                        }
                    } else null
                }

                if (parsedHolidays.isNotEmpty()) {
                    // Safe idempotent upsert: insert all
                    holidayDao.insertAll(parsedHolidays.map { HolidayEntity.fromDomain(it) })
                }
                Result.success(parsedHolidays.size)
            } else {
                Result.failure(Exception("Calendarific API returned error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            // Offline or network error: return failure result gracefully without crashing
            Result.failure(e)
        }
    }
}
