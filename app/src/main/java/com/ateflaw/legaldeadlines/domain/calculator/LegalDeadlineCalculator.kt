package com.ateflaw.legaldeadlines.domain.calculator

import com.ateflaw.legaldeadlines.domain.model.CalculationMilestone
import com.ateflaw.legaldeadlines.domain.model.DeadlineResult
import com.ateflaw.legaldeadlines.domain.model.DistanceType
import com.ateflaw.legaldeadlines.domain.model.DurationUnit
import com.ateflaw.legaldeadlines.domain.model.ExcludedDay
import com.ateflaw.legaldeadlines.domain.model.LegalRule
import com.ateflaw.legaldeadlines.domain.model.MilestoneType
import com.ateflaw.legaldeadlines.domain.model.StartRule
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Core Domain calculation engine for Egyptian procedural legal deadlines.
 *
 * Adheres strictly to Egyptian Civil and Commercial Procedures Law (Law 13 of 1968):
 * - Article 15: Day of announcement/event is excluded if NEXT_DAY; deadlines in months/years
 *   expire on the corresponding day in the following month/year. If the corresponding day does
 *   not exist, it expires on the last day of that month/year (Court of Cassation).
 * - Article 16: Distance deadline for local travel (1 day per 50 km up to 4 days max).
 * - Article 17: Distance deadline for persons domiciled abroad (up to 60/90 days).
 * - Article 18: If the deadline expires on an official holiday or Friday, it extends to the first subsequent working day.
 * - Friday is always a weekly holiday and must be excluded.
 * - Saturday is a normal working day in Egyptian courts and must NEVER be excluded as a weekend.
 * - Official Egyptian national/cabinet holidays are excluded.
 */
class LegalDeadlineCalculator {

    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd", Locale.ENGLISH)

    /**
     * Calculates the procedural deadline based on the announcement date, legal rule,
     * explicit distance type, optional distance days, and an official holiday lookup provider.
     */
    fun calculate(
        announcementDate: LocalDate,
        rule: LegalRule,
        distanceType: DistanceType = DistanceType.NONE,
        additionalDistanceDays: Int = 0,
        isHolidayProvider: (LocalDate) -> Pair<Boolean, String?> = { Pair(false, null) }
    ): DeadlineResult {
        // 1. Distance Days Validation according to Articles 16 & 17 of Procedures Law
        val validatedDistanceDays: Int = when (distanceType) {
            DistanceType.NONE -> {
                require(additionalDistanceDays == 0) { "لا يجوز إضافة أيام مسافة مع خيار (بدون مسافة)" }
                0
            }
            DistanceType.LOCAL -> {
                require(additionalDistanceDays in 0..4) {
                    "ميعاد المسافة المحلي لا يجوز أن يجاوز 4 أيام إعمالاً للمادة 16 من قانون المرافعات"
                }
                additionalDistanceDays
            }
            DistanceType.ABROAD -> {
                require(additionalDistanceDays in 0..90) {
                    "ميعاد المسافة لمن موطنه بالخارج لا يجوز أن يجاوز 90 يوماً إعمالاً للمادة 17 من قانون المرافعات"
                }
                additionalDistanceDays
            }
        }

        val totalDistanceDays = rule.distanceDays + validatedDistanceDays

        // 2. Determine Start Date based on rule.startRule (Article 15 Procedures Law)
        val startDate = when (rule.startRule) {
            StartRule.SAME_DAY -> announcementDate
            StartRule.NEXT_DAY -> announcementDate.plusDays(1)
        }

        // 3. Compute Base End Date according to Article 15 Paragraph 4 and Court of Cassation
        val baseEndDate = calculateBaseEndDate(announcementDate, startDate, rule)

        // 4. Add Distance Days (Article 16 or 17 Procedures Law)
        val provisionalDate = baseEndDate.plusDays(totalDistanceDays.toLong())

        // 5. Final Deadline Adjustment Loop (Article 18 Procedures Law)
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

        // 6. Build Structured Milestones for Interactive Timeline
        val milestones = mutableListOf<CalculationMilestone>()

        // Milestone 1: Trigger Event
        val triggerTitle = rule.triggerEventLabel.ifBlank { "الواقعة المنشئة للميعاد" }
        milestones.add(
            CalculationMilestone(
                type = MilestoneType.TRIGGER_EVENT,
                date = announcementDate,
                title = triggerTitle,
                description = "$triggerTitle (${announcementDate.format(dateFormatter)}) (${getArabicDayName(announcementDate.dayOfWeek)}).",
                legalBasis = rule.lawArticle.ifBlank { rule.lawName }
            )
        )

        // Milestone 2: Counting Start
        milestones.add(
            CalculationMilestone(
                type = MilestoneType.COUNTING_START,
                date = startDate,
                title = "بدء سريان الميعاد الإجرائي",
                description = if (rule.startRule == StartRule.NEXT_DAY) {
                    "يبدأ احتساب الميعاد من اليوم التالي للواقعة (${startDate.format(dateFormatter)}) ولا يُحسب يوم الواقعة نفسه إعمالاً للمادة 15 مرافعات."
                } else {
                    "يبدأ احتساب الميعاد من نفس يوم الواقعة (${startDate.format(dateFormatter)}) وفقاً للقاعدة المقررة."
                },
                legalBasis = "المادة 15 من قانون المرافعات المدنية والتجارية"
            )
        )

        // Milestone 3: Base Deadline End
        milestones.add(
            CalculationMilestone(
                type = MilestoneType.BASE_DEADLINE,
                date = baseEndDate,
                title = "نهاية المدة القانونية الأصلية",
                description = "انقضاء مدة الميعاد الأصلية (${rule.duration} ${rule.unit.arabicDisplayName}) في (${baseEndDate.format(dateFormatter)}) (${getArabicDayName(baseEndDate.dayOfWeek)}).",
                legalBasis = rule.lawArticle.ifBlank { rule.article }
            )
        )

        // Milestone 4: Distance extension if applicable
        if (totalDistanceDays > 0 && distanceType != DistanceType.NONE) {
            milestones.add(
                CalculationMilestone(
                    type = MilestoneType.DISTANCE_EXTENSION,
                    date = provisionalDate,
                    title = "إضافة ميعاد مسافة الطريق",
                    description = "إضافة $totalDistanceDays أيام مسافة (${distanceType.arabicDisplayName}) ليمتد الميعاد المبدئي إلى (${provisionalDate.format(dateFormatter)}).",
                    legalBasis = distanceType.statutoryBasis
                )
            )
        }

        // Milestone 5: Excluded Days & Holiday Extensions
        excludedDays.forEach { excluded ->
            milestones.add(
                CalculationMilestone(
                    type = MilestoneType.HOLIDAY_EXTENSION,
                    date = excluded.date,
                    title = "امتداد الميعاد لمصادفة عطلة",
                    description = "مصادفة تاريخ ${excluded.date.format(dateFormatter)} (${getArabicDayName(excluded.date.dayOfWeek)}) [${excluded.reason.substringAfterLast("—").trim()}]، ويمتد الميعاد إعمالاً للمادة 18 مرافعات إلى أول يوم عمل.",
                    legalBasis = "المادة 18 من قانون المرافعات (امتداد العطلات)",
                    isExcludedDay = true
                )
            )
        }

        // Milestone 6: Final Binding Deadline
        milestones.add(
            CalculationMilestone(
                type = MilestoneType.FINAL_DEADLINE,
                date = finalDeadline,
                title = "الميعاد النهائي واجب الالتزام به",
                description = "آخر يوم عمل لإيداع صحيفة الطعن أو اتخاذ الإجراء بقلم الكتاب (${finalDeadline.format(dateFormatter)}) (${getArabicDayName(finalDeadline.dayOfWeek)})" +
                    if (finalDeadline.dayOfWeek == DayOfWeek.SATURDAY) " [يوم السبت يوم عمل رسمي بالمحاكم المصرية]." else " [يوم عمل رسمي].",
                legalBasis = "المادتان 15 و 18 من قانون المرافعات"
            )
        )

        // 7. Generate Comprehensive Arabic Explanation
        val explanation = generateArabicExplanation(
            announcementDate = announcementDate,
            startDate = startDate,
            rule = rule,
            distanceType = distanceType,
            additionalDistanceDays = validatedDistanceDays,
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
            additionalDistanceDays = validatedDistanceDays,
            totalDistanceDays = totalDistanceDays,
            excludedDays = excludedDays,
            finalDeadline = finalDeadline,
            explanation = explanation,
            lawArticle = rule.lawArticle.ifBlank { rule.article },
            notes = rule.notes,
            actionSummary = rule.summary.ifBlank { rule.actionName },
            milestones = milestones,
            distanceType = distanceType
        )
    }

    /**
     * Backward-compatible overload accepting additionalDistanceDays directly.
     */
    fun calculate(
        announcementDate: LocalDate,
        rule: LegalRule,
        additionalDistanceDays: Int,
        isHolidayProvider: (LocalDate) -> Pair<Boolean, String?> = { Pair(false, null) }
    ): DeadlineResult {
        val distanceType = when {
            additionalDistanceDays <= 0 -> DistanceType.NONE
            additionalDistanceDays <= 4 -> DistanceType.LOCAL
            else -> DistanceType.ABROAD
        }
        return calculate(
            announcementDate = announcementDate,
            rule = rule,
            distanceType = distanceType,
            additionalDistanceDays = additionalDistanceDays,
            isHolidayProvider = isHolidayProvider
        )
    }

    /**
     * Strictly computes base end date according to Article 15 of Egyptian Procedures Law:
     * - Days: (duration - 1) added to startDate.
     * - Months: Clamps to last day of target month if corresponding day does not exist.
     * - Years: Clamps Feb 29 to Feb 28 on non-leap years.
     * - Hours: Converts hours to days safely.
     */
    fun calculateBaseEndDate(
        announcementDate: LocalDate,
        startDate: LocalDate,
        rule: LegalRule
    ): LocalDate {
        return when (rule.unit) {
            DurationUnit.DAYS -> {
                val effectiveDays = (rule.duration - 1).coerceAtLeast(0)
                startDate.plusDays(effectiveDays)
            }
            DurationUnit.MONTHS -> {
                val refDate = if (rule.startRule == StartRule.NEXT_DAY) announcementDate else startDate
                val targetYearMonth = YearMonth.from(refDate).plusMonths(rule.duration)
                val targetDay = refDate.dayOfMonth.coerceAtMost(targetYearMonth.lengthOfMonth())
                targetYearMonth.atDay(targetDay)
            }
            DurationUnit.YEARS -> {
                val refDate = if (rule.startRule == StartRule.NEXT_DAY) announcementDate else startDate
                val targetYear = refDate.year + rule.duration
                val isLeapTarget = java.time.Year.isLeap(targetYear)
                if (refDate.monthValue == 2 && refDate.dayOfMonth == 29 && !isLeapTarget) {
                    LocalDate.of(targetYear.toInt(), 2, 28)
                } else {
                    LocalDate.of(targetYear.toInt(), refDate.monthValue, refDate.dayOfMonth)
                }
            }
            DurationUnit.HOURS -> {
                val equivalentDays = ((rule.duration + 23) / 24).coerceAtLeast(1)
                startDate.plusDays(equivalentDays - 1)
            }
        }
    }

    private fun generateArabicExplanation(
        announcementDate: LocalDate,
        startDate: LocalDate,
        rule: LegalRule,
        distanceType: DistanceType,
        additionalDistanceDays: Int,
        totalDistanceDays: Int,
        provisionalDate: LocalDate,
        excludedDays: List<ExcludedDay>,
        finalDeadline: LocalDate
    ): String {
        val sb = StringBuilder()

        val triggerLabel = rule.triggerEventLabel.ifBlank { "تاريخ الإعلان / الإجراء" }
        sb.append("١. ").append(triggerLabel).append(": ")
            .append(announcementDate.format(dateFormatter))
            .append(" (").append(getArabicDayName(announcementDate.dayOfWeek)).append(")\n")

        sb.append("٢. تاريخ بدء احتساب الميعاد: ")
            .append(startDate.format(dateFormatter))
            .append(" (").append(getArabicDayName(startDate.dayOfWeek)).append(")")
        if (rule.startRule == StartRule.NEXT_DAY) {
            sb.append(" — تطبيقاً لنص المادة 15 مرافعات: يبدأ احتساب الميعاد من اليوم التالي ولا يُحسب يوم الواقعة نفسه.\n")
        } else {
            sb.append(" — يبدأ احتساب الميعاد من نفس يوم الواقعة وفقاً لنص القاعدة الإجرائية.\n")
        }

        sb.append("٣. مدة الميعاد القانوني: ")
            .append(rule.duration)
            .append(" ")
            .append(rule.unit.arabicDisplayName)
            .append("\n")

        sb.append("٤. وحدة الاحتساب: ")
            .append(rule.unit.arabicDisplayName)
            .append("\n")

        sb.append("٥. ميعاد المسافة: ")
        if (distanceType == DistanceType.NONE && totalDistanceDays == 0) {
            sb.append("لا يوجد ميعاد مسافة (محل النزاع بنفس الدائرة القضائية).\n")
        } else {
            sb.append(distanceType.arabicDisplayName)
                .append(" — إجمالي: ").append(totalDistanceDays).append(" يوم")
                .append(" (").append(distanceType.statutoryBasis).append(")\n")
        }

        sb.append("٦. الميعاد المبدئي (قبل تطبيق امتداد العطلات): ")
            .append(provisionalDate.format(dateFormatter))
            .append(" (").append(getArabicDayName(provisionalDate.dayOfWeek)).append(")\n")

        if (excludedDays.isNotEmpty()) {
            sb.append("٧. الأيام المستبعدة والامتداد (المادة 18 مرافعات):\n")
            excludedDays.forEachIndexed { index, excluded ->
                sb.append("   • [").append(index + 1).append("] ")
                    .append(excluded.reason)
                    .append("\n")
            }
        } else {
            sb.append("٧. الأيام المستبعدة: لا يوجد أيام مستبعدة (صادف الميعاد المبدئي يوم عمل رسمي مباشرة).\n")
        }

        sb.append("٨. الميعاد النهائي واجب الالتزام به: ")
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
