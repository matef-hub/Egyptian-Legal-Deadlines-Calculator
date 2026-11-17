package com.ateflaw.legaldeadlines.presentation.components

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import androidx.core.net.toUri
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

internal fun openAllDayCalendarEvent(
    context: Context,
    date: LocalDate,
    title: String,
    description: String,
    webTitle: String = "تاريخ قانوني للمراجعة"
): Boolean {
    val endDate = date.plusDays(1)
    val event = Intent(Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI).apply {
        putExtra(CalendarContract.Events.TITLE, title)
        putExtra(CalendarContract.Events.DESCRIPTION, description)
        putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true)
        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
        putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
    }
    if (runCatching { context.startActivity(event) }.isSuccess) return true

    // A browser opens the URL immediately. Keep case data out of that request.
    val format = DateTimeFormatter.BASIC_ISO_DATE
    val web = "https://calendar.google.com/calendar/render".toUri().buildUpon()
        .appendQueryParameter("action", "TEMPLATE")
        .appendQueryParameter("text", webTitle)
        .appendQueryParameter("dates", "${date.format(format)}/${endDate.format(format)}")
        .build()
    return runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, web)) }.isSuccess
}
