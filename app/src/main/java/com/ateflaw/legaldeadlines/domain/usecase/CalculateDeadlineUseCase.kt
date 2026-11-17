package com.ateflaw.legaldeadlines.domain.usecase

import com.ateflaw.legaldeadlines.domain.calculator.LegalDeadlineCalculator
import com.ateflaw.legaldeadlines.domain.calculator.LegalRuleSafety
import com.ateflaw.legaldeadlines.domain.model.DeadlineResult
import com.ateflaw.legaldeadlines.domain.model.DistanceType
import com.ateflaw.legaldeadlines.domain.model.LegalRule
import com.ateflaw.legaldeadlines.domain.model.Holiday
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
        // Only sourced, verified decisions participate in court-day extensions.
        val officialHolidays = holidaysList.filter(Holiday::isVerifiedOfficial)
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

        // Published holiday dates do not establish the full calendar for each court.
        val holidayWarnings = if (rule.extensionPolicy == "ARTICLE_18_COURTS") {
            val recordedYears = officialHolidays.map { it.holidayDate.year }.toSet()
            val missingYears = (announcementDate.year..result.finalDeadline.year)
                .filterNot(recordedYears::contains)
            buildList {
                if (missingYears.isNotEmpty()) {
                    add("لا توجد قرارات عطلات مسجلة لسنوات ${missingYears.joinToString("، ")}.")
                }
                add("تحقّق من اكتمال قرارات العطلات والإغلاقات الخاصة بالجهة المختصة قبل الاعتماد على التاريخ.")
            }
        } else {
            listOf("يُعرض انتهاء المدة دون ترحيل تلقائي. راجع قواعد الجهة المختصة.")
        }

        result.copy(
            explanation = "تاريخ حسابي للمراجعة.\n" + result.explanation,
            warnings = (reviewWarnings + result.warnings + holidayWarnings).distinct(),
            isProvisional = true
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
