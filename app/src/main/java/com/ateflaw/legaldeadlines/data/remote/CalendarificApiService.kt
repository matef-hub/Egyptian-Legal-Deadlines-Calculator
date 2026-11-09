package com.ateflaw.legaldeadlines.data.remote

import com.ateflaw.legaldeadlines.data.dto.CalendarificResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit REST interface for Calendarific Holidays API.
 */
interface CalendarificApiService {

    @GET("api/v2/holidays")
    suspend fun getHolidays(
        @Query("api_key") apiKey: String,
        @Query("country") country: String = "EG",
        @Query("year") year: Int,
        @Query("type") type: String = "national"
    ): Response<CalendarificResponseDto>
}
