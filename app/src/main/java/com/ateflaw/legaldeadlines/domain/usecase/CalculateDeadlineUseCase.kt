package com.ateflaw.legaldeadlines.domain.usecase

import com.ateflaw.legaldeadlines.domain.calculator.LegalDeadlineCalculator
import com.ateflaw.legaldeadlines.domain.calculator.LegalRuleSafety
import com.ateflaw.legaldeadlines.domain.model.DeadlineResult
import com.ateflaw.legaldeadlines.domain.model.DistanceType
import com.ateflaw.legaldeadlines.domain.model.LegalRule
import com.ateflaw.legaldeadlines.domain.model.MilestoneType
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
        val reviewWarnings = LegalRuleSafety.previewWarnings(rule, announcementDate)
        // No unverified route-distance extension for the article 82 shatb preview.
        if (rule.id == 10L) {
            require(distanceType == DistanceType.NONE && additionalDistanceDays == 0) {
                "لا تُضاف أيام مسافة آليًا لتجديد الدعوى من الشطب؛ راجع أحكام الإعلان."
            }
        }
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
            rule = if (rule.id == 10L) rule.copy(triggerEventLabel = "تاريخ قرار شطب الدعوى") else rule,
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
        // A user-reviewed rule is finalized by the editor. An incomplete
        // official-holiday year still makes the computed date provisional.
        val isProvisional = rule.reviewStatus != "VERIFIED_ACCURATE" ||
            missingYears.isNotEmpty()
        val outputMilestones = if (isProvisional) result.milestones.map { item ->
            if (item.type == MilestoneType.FINAL_DEADLINE) item.copy(
                title = "تاريخ الحساب (يحتاج استكمال بيانات)",
                description = "تاريخ ناتج من المدة المسجلة؛ تحقق من العطلات والإجراءات حسب المادة."
            ) else item
        } else result.milestones

        result.copy(
            milestones = outputMilestones,
            explanation = if (isProvisional) {
                "تاريخ حسابي مع وجود بيانات تستلزم الاستكمال.\\n" +
                    result.explanation.replace(
                        "الميعاد النهائي واجب الالتزام به",
                        "تاريخ نهاية المدة الحسابية"
                    )
            } else result.explanation,
            warnings = (reviewWarnings + result.warnings + holidayWarnings).distinct(),
            isProvisional = isProvisional
        )
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
