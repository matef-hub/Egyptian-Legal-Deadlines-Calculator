package com.ateflaw.legaldeadlines.data.dto

import com.google.gson.annotations.SerializedName

/**
 * Data Transfer Objects for the Calendarific Holidays REST API.
 */
data class CalendarificResponseDto(
    @SerializedName("meta")
    val meta: CalendarificMetaDto?,
    @SerializedName("response")
    val response: CalendarificDataDto?
)

data class CalendarificMetaDto(
    @SerializedName("code")
    val code: Int,
    @SerializedName("error_type")
    val errorType: String?,
    @SerializedName("error_detail")
    val errorDetail: String?
)

data class CalendarificDataDto(
    @SerializedName("holidays")
    val holidays: List<CalendarificHolidayDto>?
)

data class CalendarificHolidayDto(
    @SerializedName("name")
    val name: String?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("date")
    val date: CalendarificDateDto?,
    @SerializedName("type")
    val type: List<String>?,
    @SerializedName("primary_type")
    val primaryType: String?
)

data class CalendarificDateDto(
    @SerializedName("iso")
    val iso: String?, // Can be YYYY-MM-DD or full timestamp
    @SerializedName("datetime")
    val datetime: CalendarificDateTimeDto?
)

data class CalendarificDateTimeDto(
    @SerializedName("year")
    val year: Int,
    @SerializedName("month")
    val month: Int,
    @SerializedName("day")
    val day: Int
)
