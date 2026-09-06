package com.ateflaw.legaldeadlines.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateFormatterUtils {

    private val standardFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)

    fun formatDisplayDate(date: LocalDate): String {
        return date.format(standardFormatter)
    }

    fun parseIsoDate(isoString: String): LocalDate {
        return LocalDate.parse(isoString)
    }

    fun toIsoDate(date: LocalDate): String {
        return date.toString()
    }
}
