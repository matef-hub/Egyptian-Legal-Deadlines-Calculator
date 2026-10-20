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
 * Egyptian Procedural Legal Deadline Calculator.
 *
 * Implements authoritative procedural rules under Egyptian Law:
 * - Articles 15, 16, 17, and 18 of the Civil and Commercial Procedures Law No. 13 of 1968.
 * - Friday is an official weekly holiday and must be excluded.
 * - Saturday is a valid judicial working day in Egyptian courts and is NOT excluded.
 * - Consecutive official holidays roll over to the first subsequent working day.
 * - Clamps months and leap years according to procedural jurisprudence.
 * - Enforces effective dates and transitional provisions.
 */
class LegalDeadlineCalculator {

    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd", Locale.ENGLISH)

    /**
     * Primary strongly typed calculation contract.
     */
    fun calculate(
        announcementDate: LocalDate,
        rule: LegalRule,
        distanceType: DistanceType = DistanceType.NONE,
        additionalDistanceDays: Int = 0,
        judicialOrderDetails: String? = null,
        isHolidayProvider: (LocalDate) -> Pair<Boolean, String?> = { Pair(false, null) }
    ): DeadlineResult {
        // 1. Effective Date Validation & Transitional Provisions
        val eventIso = announcementDate.toString()
        if (rule.effectiveFrom.isNotBlank() && eventIso < rule.effectiveFrom) {
            throw IllegalArgumentException(
                "القاعدة القانونية المختارة (${rule.actionName}) غير سارية في تاريخ الواقعة ($eventIso)؛ حيث يبدأ سريانها في ${rule.effectiveFrom}."
            )
        }
        if (!rule.effectiveTo.isNullOrBlank() && eventIso > rule.effectiveTo) {
            throw IllegalArgumentException(
                "القاعدة القانونية المختارة (${rule.actionName}) منتهية السريان في تاريخ الواقعة ($eventIso)؛ حيث انتهى سريانها في ${rule.effectiveTo}."
            )
        }

        // 2. Distance Policy Check
        if (rule.distancePolicy.equals("NOT_APPLICABLE", ignoreCase = true)) {
            require(distanceType == DistanceType.NONE || distanceType == DistanceType.ABROAD_SERVED_IN_EGYPT) {
                "هذا الإجراء القانوني (${rule.actionName}) لا يقبل تطبيق مواعيد المسافة وفقاً للقواعد الإجرائية المقررة."
            }
            require(additionalDistanceDays == 0) {
                "لا يجوز إضافة ميعاد مسافة لهذا الإجراء (${rule.actionName}) لأن ميعاد المسافة غير منطبق عليه قانوناً."
            }
        }

        // 3. Distance Days Statutory Validation
        val validatedDistanceDays: Int = when (distanceType) {
            DistanceType.NONE -> {
                require(additionalDistanceDays == 0) { "لا يجوز إضافة أيام مسافة مع خيار (بدون مسافة)." }
                0
            }
            DistanceType.LOCAL -> {
                require(additionalDistanceDays in 0..4) {
                    "ميعاد المسافة المحلي لا يجوز أن يجاوز 4 أيام إعمالاً للمادة 16 من قانون المرافعات (يوم لكل 50 كم، وما زاد على 30 كم يعتبر 50 كم، بحد أقصى 4 أيام)."
                }
                additionalDistanceDays
            }
            DistanceType.BORDER_AREA -> {
                require(additionalDistanceDays == 0 || additionalDistanceDays == 15) {
                    "ميعاد المسافة للمناطق الحدودية النائية محدد بنص القانون بـ 15 يوماً وفقاً للمادة 16 فقرة 2 مرافعات."
                }
                15
            }
            DistanceType.ABROAD -> {
                require(additionalDistanceDays == 0 || additionalDistanceDays == 60) {
                    "ميعاد المسافة العادي لمن موطنه بالخارج محدد بنص المادة 17 مرافعات بـ 60 يوماً ولا يجوز زيادته إلى 90 يوماً."
                }
                60
            }
            DistanceType.ABROAD_JUDICIAL_REDUCTION -> {
                require(additionalDistanceDays in 1..59) {
                    "ميعاد المسافة المنقوص بأمر قضائي لمن موطنه بالخارج يجب أن يكون بين 1 و 59 يوماً وفقاً للمادة 17 فقرة 2 مرافعات."
                }
                require(!judicialOrderDetails.isNullOrBlank()) {
                    "يلزم إثبات بيانات وسند الأمر القضائي الصادر بإنقاص ميعاد المسافة وفقاً للمادة 17 فقرة 2 مرافعات."
                }
                additionalDistanceDays
            }
            DistanceType.ABROAD_SERVED_IN_EGYPT -> {
                require(additionalDistanceDays == 0) {
                    "من أُعلن لشخصه أثناء وجوده في مصر لا يسري عليه ميعاد مسافة وفقاً للمادة 17 فقرة 3 مرافعات."
                }
                0
            }
        }

        val totalDistanceDays = rule.distanceDays + validatedDistanceDays

        // 4. Start Date Calculation (Article 15 Procedures Law)
        val startDate = when (rule.startRule) {
            StartRule.SAME_DAY -> announcementDate
            StartRule.NEXT_DAY -> announcementDate.plusDays(1)
        }

        // 5. Base End Date
        val baseEndDate = calculateBaseEndDate(announcementDate, startDate, rule)

        // 6. Provisional Date with Distance
        val provisionalDate = baseEndDate.plusDays(totalDistanceDays.toLong())

        // 7. Final Deadline Adjustment Loop (Article 18 Procedures Law)
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

        // 8. Milestones
        val milestones = mutableListOf<CalculationMilestone>()

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

        milestones.add(
            CalculationMilestone(
                type = MilestoneType.BASE_DEADLINE,
                date = baseEndDate,
                title = "نهاية المدة القانونية الأصلية",
                description = "انقضاء مدة الميعاد الأصلية (${rule.duration} ${rule.unit.arabicDisplayName}) في (${baseEndDate.format(dateFormatter)}) (${getArabicDayName(baseEndDate.dayOfWeek)}).",
                legalBasis = rule.lawArticle.ifBlank { rule.article }
            )
        )

        if (totalDistanceDays > 0 && distanceType != DistanceType.NONE) {
            val distDesc = when (distanceType) {
                DistanceType.LOCAL -> "إضافة $totalDistanceDays أيام مسافة محلية (المادة 16 مرافعات)."
                DistanceType.BORDER_AREA -> "إضافة 15 يوماً مسافة للمناطق الحدودية النائية (المادة 16 فقرة 2 مرافعات)."
                DistanceType.ABROAD -> "إضافة 60 يوماً ميعاد مسافة لمن موطنه بالخارج (المادة 17 مرافعات)."
                DistanceType.ABROAD_JUDICIAL_REDUCTION -> "إضافة $totalDistanceDays يوماً مسافة منقوصة بأمر قضائي: $judicialOrderDetails (المادة 17 فقرة 2)."
                else -> "إضافة $totalDistanceDays أيام مسافة طريق."
            }
            milestones.add(
                CalculationMilestone(
                    type = MilestoneType.DISTANCE_EXTENSION,
                    date = provisionalDate,
                    title = "إضافة ميعاد مسافة الطريق",
                    description = "$distDesc ليمتد الميعاد المبدئي إلى (${provisionalDate.format(dateFormatter)}).",
                    legalBasis = distanceType.statutoryBasis
                )
            )
        }

        excludedDays.forEachIndexed { index, excluded ->
            milestones.add(
                CalculationMilestone(
                    type = MilestoneType.HOLIDAY_EXTENSION,
                    date = excluded.date,
                    title = "امتداد بسبب عطلة [${index + 1}]",
                    description = "${excluded.reason}، ويمتد الميعاد لليوم التالي وفقاً للمادة 18 مرافعات.",
                    legalBasis = "المادة 18 من قانون المرافعات المدنية والتجارية",
                    isExcludedDay = true
                )
            )
        }

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

        val explanation = generateArabicExplanation(
            announcementDate = announcementDate,
            startDate = startDate,
            rule = rule,
            distanceType = distanceType,
            additionalDistanceDays = validatedDistanceDays,
            totalDistanceDays = totalDistanceDays,
            provisionalDate = provisionalDate,
            excludedDays = excludedDays,
            finalDeadline = finalDeadline,
            judicialOrderDetails = judicialOrderDetails
        )

        val warnings = mutableListOf<String>()
        if (rule.reviewStatus.equals("NEEDS_REVIEW", ignoreCase = true)) {
            warnings.add("تنبيه تدقيق: هذه القاعدة القانونية قيد المراجعة والتدقيق التشريعي، ولم تُعتمد بعد كميعاد قطعي موثق رسمياً.")
        }

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
            distanceType = distanceType,
            judicialOrderDetails = judicialOrderDetails,
            warnings = warnings
        )
    }

    /**
     * Backward-compatible overload.
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
            additionalDistanceDays == 15 -> DistanceType.BORDER_AREA
            additionalDistanceDays == 60 -> DistanceType.ABROAD
            else -> DistanceType.LOCAL
        }
        return calculate(
            announcementDate = announcementDate,
            rule = rule,
            distanceType = distanceType,
            additionalDistanceDays = additionalDistanceDays,
            judicialOrderDetails = null,
            isHolidayProvider = isHolidayProvider
        )
    }

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
        finalDeadline: LocalDate,
        judicialOrderDetails: String?
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
        } else if (distanceType == DistanceType.ABROAD_SERVED_IN_EGYPT) {
            sb.append("لا يسري ميعاد المسافة لمن أعلن لشخصه أثناء وجوده في مصر (المادة 17 فقرة 3 مرافعات).\n")
        } else {
            sb.append(distanceType.arabicDisplayName)
                .append(" — إجمالي: ").append(totalDistanceDays).append(" يوم")
                .append(" (").append(distanceType.statutoryBasis).append(")")
            if (!judicialOrderDetails.isNullOrBlank()) {
                sb.append(" [سند الأمر القضائي: ").append(judicialOrderDetails).append("]")
            }
            sb.append("\n")
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
                DayOfWeek.THURSDAY -> "الخميس"
                DayOfWeek.WEDNESDAY -> "الأربعاء"
                DayOfWeek.TUESDAY -> "الثلاثاء"
                DayOfWeek.FRIDAY -> "الجمعة"
            }
        }
    }
}
