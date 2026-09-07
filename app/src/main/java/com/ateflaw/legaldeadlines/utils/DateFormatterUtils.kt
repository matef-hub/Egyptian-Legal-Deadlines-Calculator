package com.ateflaw.legaldeadlines.utils

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateFormatterUtils {

    private val standardFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)

    private val arabicMonthNames = arrayOf(
        "", // 1-based index
        "يناير",
        "فبراير",
        "مارس",
        "أبريل",
        "مايو",
        "يونيو",
        "يوليو",
        "أغسطس",
        "سبتمبر",
        "أكتوبر",
        "نوفمبر",
        "ديسمبر"
    )

    fun getArabicMonthName(month: Int): String {
        return if (month in 1..12) arabicMonthNames[month] else ""
    }

    fun getArabicDayName(dayOfWeek: DayOfWeek): String {
        return when (dayOfWeek) {
            DayOfWeek.SATURDAY -> "السبت"
            DayOfWeek.SUNDAY -> "الأحد"
            DayOfWeek.MONDAY -> "الاثنين"
            DayOfWeek.TUESDAY -> "الثلاثاء"
            DayOfWeek.WEDNESDAY -> "الأربعاء"
            DayOfWeek.THURSDAY -> "الخميس"
            DayOfWeek.FRIDAY -> "الجمعة"
        }
    }

    /**
     * Formats date in standard Egyptian legal practice:
     * e.g. "21 سبتمبر 2026"
     * Completely immune to BiDi number inversion issues.
     */
    fun formatArabicFullDate(date: LocalDate): String {
        val monthName = getArabicMonthName(date.monthValue)
        return "${date.dayOfMonth} $monthName ${date.year}"
    }

    /**
     * Formats date with day name:
     * e.g. "الأربعاء، 21 سبتمبر 2026"
     */
    fun formatArabicDateWithDay(date: LocalDate): String {
        val dayName = getArabicDayName(date.dayOfWeek)
        val fullDate = formatArabicFullDate(date)
        return "$dayName، $fullDate"
    }

    /**
     * Formats date numerically with explicit LTR isolate marks to prevent slash inversion:
     * e.g. "\u200E21/09/2026\u200E"
     */
    fun formatDisplayDate(date: LocalDate): String {
        val d = if (date.dayOfMonth < 10) "0${date.dayOfMonth}" else "${date.dayOfMonth}"
        val m = if (date.monthValue < 10) "0${date.monthValue}" else "${date.monthValue}"
        return "\u200E$d/$m/${date.year}\u200E"
    }

    fun parseIsoDate(isoString: String): LocalDate {
        return LocalDate.parse(isoString)
    }

    fun toIsoDate(date: LocalDate): String {
        return date.toString()
    }
}
