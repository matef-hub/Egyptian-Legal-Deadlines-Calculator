package com.ateflaw.legaldeadlines.domain.usecase

import com.ateflaw.legaldeadlines.domain.calculator.LegalDeadlineCalculator
import com.ateflaw.legaldeadlines.domain.calculator.LegalRuleSafety
import com.ateflaw.legaldeadlines.domain.model.DeadlineResult
import com.ateflaw.legaldeadlines.domain.model.DistanceType
import com.ateflaw.legaldeadlines.domain.model.LegalRule
import com.ateflaw.legaldeadlines.domain.repository.HolidayRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

/**
 * UseCase to execute procedural deadline calculations leveraging the repository holiday data.
 * Highly optimized with in-memory lookup for instantaneous zero-latency calculations.
 */
class CalculateDeadlineUseCase(
    private val calculator: LegalDeadlineCalculator = LegalDeadlineCalculator(),
    private val holidayRepository: HolidayRepository
) {
    suspend operator fun invoke(
        announcementDate: LocalDate,
        rule: LegalRule,
        distanceType: DistanceType = DistanceType.NONE,
        additionalDistanceDays: Int = 0,
        judicialOrderDetails: String? = null
    ): DeadlineResult = withContext(Dispatchers.Default) {
        LegalRuleSafety.validate(rule, announcementDate)
        val holidaysList = withContext(Dispatchers.IO) {
            holidayRepository.getAllHolidaysList()
        }
        // Bundled dates and third-party dates are NOT official published decisions.
        val officialHolidays = holidaysList.filter {
            it.verificationStatus == "VERIFIED_OFFICIAL" &&
                it.sourceUrl.startsWith("https://")
        }
        val holidayMap: Map<LocalDate, String> = officialHolidays.associate { it.holidayDate to it.name }

        val result = calculator.calculate(
            announcementDate = announcementDate,
            rule = rule,
            distanceType = distanceType,
            additionalDistanceDays = additionalDistanceDays,
            judicialOrderDetails = judicialOrderDetails,
            isHolidayProvider = { date ->
                val name = holidayMap[date]
                Pair(name != null, name)
            }
        )

        // The holiday cache is not a substitute for published government decisions.
        // Particularly for an empty year, the calculator must never suggest that
        // no holidays existed merely because none were stored locally.
        val coveredYears = officialHolidays.map { it.holidayDate.year }.toSet()
        val yearRange = minOf(announcementDate.year, result.finalDeadline.year)..
            maxOf(announcementDate.year, result.finalDeadline.year)
        val missingYears = yearRange.filterNot { it in coveredYears }
        val holidayWarnings = buildList {
            if (missingYears.isNotEmpty()) {
                add("تحذير: قاعدة العطلات المحلية لا تحتوي بيانات لسنوات ${missingYears.joinToString("، ")}؛ وقد يتغير الميعاد النهائي بعد التحقق من الإجازات الرسمية.")
            }
            add("تنبيه: اكتمال القرارات الرسمية للعطلات في جهة الإجراء غير مثبت حتى لو وجدت بعض التواريخ الموثقة. يجب مراجعة القرارات الفعلية قبل الاعتماد على النتيجة.")
        }
        result.copy(warnings = result.warnings + holidayWarnings)
    }

    suspend operator fun invoke(
        announcementDate: LocalDate,
        rule: LegalRule,
        additionalDistanceDays: Int
    ): DeadlineResult {
        val distanceType = when {
            additionalDistanceDays <= 0 -> DistanceType.NONE
            additionalDistanceDays <= 4 -> DistanceType.LOCAL
            additionalDistanceDays == 15 -> DistanceType.BORDER_AREA
            additionalDistanceDays == 60 -> DistanceType.ABROAD
            else -> DistanceType.LOCAL
        }
        return invoke(
            announcementDate = announcementDate,
            rule = rule,
            distanceType = distanceType,
            additionalDistanceDays = additionalDistanceDays
        )
    }
}
