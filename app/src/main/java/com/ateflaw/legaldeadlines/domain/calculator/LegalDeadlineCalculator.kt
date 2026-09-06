package com.ateflaw.legaldeadlines.domain.calculator

import com.ateflaw.legaldeadlines.domain.model.DeadlineResult
import com.ateflaw.legaldeadlines.domain.model.DurationUnit
import com.ateflaw.legaldeadlines.domain.model.ExcludedDay
import com.ateflaw.legaldeadlines.domain.model.LegalRule
import com.ateflaw.legaldeadlines.domain.model.StartRule
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Core Domain calculation engine for Egyptian procedural legal deadlines.
 *
 * Adheres strictly to Egyptian Civil and Commercial Procedures Law:
 * - Friday is always a weekly holiday and must be excluded.
 * - Saturday is a normal working day in Egyptian courts and must NEVER be excluded as a weekend.
 * - Official Egyptian holidays are excluded.
 * - If the final deadline falls on an excluded day (Friday or official holiday), it rolls forward
 *   consecutively until the first valid working day.
 * - Distance days (legal + additional) are explicitly computed and tracked.
 * - Handles both SAME_DAY and NEXT_DAY start counting rules.
 */
class LegalDeadlineCalculator {

    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)

    /**
     * Calculates the procedural deadline based on the announcement date, legal rule,
     * optional additional distance days, and an official holiday lookup provider.
     */
    fun calculate(
        announcementDate: LocalDate,
        rule: LegalRule,
        additionalDistanceDays: Int = 0,
        isHolidayProvider: (LocalDate) -> Pair<Boolean, String?> = { Pair(false, null) }
    ): DeadlineResult {
        require(additionalDistanceDays >= 0) { "أيام المسافة الإضافية لا يمكن أن تكون قيمة سالبة" }

        val totalDistanceDays = rule.distanceDays + additionalDistanceDays

        // 1. Determine Start Date based on rule.startRule
        val startDate = when (rule.startRule) {
            StartRule.SAME_DAY -> announcementDate
            StartRule.NEXT_DAY -> announcementDate.plusDays(1)
        }

        // 2. Compute Provisional Deadline
        val provisionalDate = when (rule.unit) {
            DurationUnit.DAYS -> {
                val effectiveDays = (rule.duration - 1).coerceAtLeast(0)
                startDate.plusDays(effectiveDays).plusDays(totalDistanceDays.toLong())
            }
            DurationUnit.MONTHS -> {
                startDate.plusMonths(rule.duration).plusDays(totalDistanceDays.toLong())
            }
            DurationUnit.YEARS -> {
                startDate.plusYears(rule.duration).plusDays(totalDistanceDays.toLong())
            }
            DurationUnit.HOURS -> {
                val equivalentDays = ((rule.duration + 23) / 24).coerceAtLeast(1)
                startDate.plusDays(equivalentDays - 1).plusDays(totalDistanceDays.toLong())
            }
        }

        // 3. Final Deadline Adjustment Loop
        var currentDate = provisionalDate
        val excludedDays = mutableListOf<ExcludedDay>()

        while (true) {
            val dayOfWeek = currentDate.dayOfWeek
            val (isOfficialHoliday, holidayName) = isHolidayProvider(currentDate)

            if (dayOfWeek == DayOfWeek.FRIDAY) {
                val dayNameArabic = getArabicDayName(dayOfWeek)
                val formattedDate = currentDate.format(dateFormatter)
                excludedDays.add(ExcludedDay(currentDate, "$formattedDate — $dayNameArabic — عطلة أسبوعية"))
                currentDate = currentDate.plusDays(1)
            } else if (isOfficialHoliday) {
                val holidayDesc = holidayName ?: "عطلة رسمية معتمدة"
                val formattedDate = currentDate.format(dateFormatter)
                excludedDays.add(ExcludedDay(currentDate, "$formattedDate — عطلة رسمية — $holidayDesc"))
                currentDate = currentDate.plusDays(1)
            } else {
                // Saturday is a valid working day in Egyptian procedural law; loop breaks.
                break
            }
        }

        val finalDeadline = currentDate

        // 4. Generate Comprehensive Arabic Explanation
        val explanation = generateArabicExplanation(
            announcementDate = announcementDate,
            startDate = startDate,
            rule = rule,
            additionalDistanceDays = additionalDistanceDays,
            totalDistanceDays = totalDistanceDays,
            provisionalDate = provisionalDate,
            excludedDays = excludedDays,
            finalDeadline = finalDeadline
        )

        return DeadlineResult(
            startDate = startDate,
            provisionalDate = provisionalDate,
            duration = rule.duration,
            unit = rule.unit,
            ruleDistanceDays = rule.distanceDays,
            additionalDistanceDays = additionalDistanceDays,
            totalDistanceDays = totalDistanceDays,
            excludedDays = excludedDays,
            finalDeadline = finalDeadline,
            explanation = explanation,
            lawArticle = rule.lawArticle,
            notes = rule.notes
        )
    }

    private fun generateArabicExplanation(
        announcementDate: LocalDate,
        startDate: LocalDate,
        rule: LegalRule,
        additionalDistanceDays: Int,
        totalDistanceDays: Int,
        provisionalDate: LocalDate,
        excludedDays: List<ExcludedDay>,
        finalDeadline: LocalDate
    ): String {
        val sb = StringBuilder()

        sb.append("١. تاريخ الإعلان / الإجراء: ")
            .append(announcementDate.format(dateFormatter))
            .append(" (").append(getArabicDayName(announcementDate.dayOfWeek)).append(")\n")

        sb.append("٢. تاريخ بدء احتساب الميعاد: ")
            .append(startDate.format(dateFormatter))
            .append(" (").append(getArabicDayName(startDate.dayOfWeek)).append(")")
        if (rule.startRule == StartRule.NEXT_DAY) {
            sb.append(" — تطبيقاً للقاعدة الإجرائية: يبدأ احتساب الميعاد من اليوم التالي للإعلان ولا يُحسب يوم الإجراء نفسه.\n")
        } else {
            sb.append(" — يبدأ احتساب الميعاد من نفس يوم الإعلان وفقاً لنص القاعدة الإجرائية.\n")
        }

        sb.append("٣. مدة الميعاد القانوني: ")
            .append(rule.duration)
            .append(" ")
            .append(rule.unit.arabicDisplayName)
            .append("\n")

        sb.append("٤. وحدة الاحتساب: ")
            .append(rule.unit.arabicDisplayName)
            .append("\n")

        sb.append("٥. ميعاد المسافة القانوني الأصلي: ")
            .append(rule.distanceDays)
            .append(" يوم\n")

        sb.append("٦. ميعاد المسافة الإضافي: ")
            .append(additionalDistanceDays)
            .append(" يوم\n")

        sb.append("٧. إجمالي مواعيد المسافة المضافة: ")
            .append(totalDistanceDays)
            .append(" يوم\n")

        sb.append("٨. الميعاد المبدئي (قبل استبعاد العطلات): ")
            .append(provisionalDate.format(dateFormatter))
            .append(" (").append(getArabicDayName(provisionalDate.dayOfWeek)).append(")\n")

        if (excludedDays.isNotEmpty()) {
            sb.append("٩. الأيام المستبعدة لمصادفتها عطلة رسمية أو أسبوعية:\n")
            excludedDays.forEachIndexed { index, excluded ->
                sb.append("   • [").append(index + 1).append("] ")
                    .append(excluded.reason)
                    .append("\n")
            }
        } else {
            sb.append("٩. الأيام المستبعدة: لا يوجد أيام مستبعدة (صادف الميعاد المبدئي يوم عمل رسمي مباشرة).\n")
        }

        sb.append("١٠. الميعاد النهائي واجب الالتزام به: ")
            .append(finalDeadline.format(dateFormatter))
            .append(" (").append(getArabicDayName(finalDeadline.dayOfWeek)).append(")")

        if (finalDeadline.dayOfWeek == DayOfWeek.SATURDAY) {
            sb.append(" [ملاحظة قانونية: يوم السبت يُعد يوم عمل كامل في المحاكم المصرية وفقاً للقانون].")
        }

        return sb.toString()
    }

    companion object {
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
    }
}
