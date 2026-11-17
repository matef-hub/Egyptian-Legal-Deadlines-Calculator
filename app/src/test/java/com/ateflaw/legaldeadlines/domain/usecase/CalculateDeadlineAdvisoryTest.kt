package com.ateflaw.legaldeadlines.domain.usecase

import com.ateflaw.legaldeadlines.domain.calculator.LegalDeadlineCalculator
import com.ateflaw.legaldeadlines.data.seed.LegalRuleSeedData
import com.ateflaw.legaldeadlines.data.seed.ReleaseRuleCatalog
import com.ateflaw.legaldeadlines.domain.model.DistanceType
import com.ateflaw.legaldeadlines.domain.model.DurationUnit
import com.ateflaw.legaldeadlines.domain.model.Holiday
import com.ateflaw.legaldeadlines.domain.model.LegalRule
import com.ateflaw.legaldeadlines.domain.model.MilestoneType
import com.ateflaw.legaldeadlines.domain.model.StartRule
import com.ateflaw.legaldeadlines.domain.repository.HolidayRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/** Regression tests for the error users saw selecting rule #10. */
class CalculateDeadlineAdvisoryTest {
    private fun calculator(holidays: List<Holiday> = emptyList()) =
        CalculateDeadlineUseCase(LegalDeadlineCalculator(), object : HolidayRepository {
            override fun getAllHolidays(): Flow<List<Holiday>> = flowOf(holidays)
            override suspend fun getAllHolidaysList() = holidays
            override suspend fun getHolidayByDate(date: LocalDate) = holidays.firstOrNull { it.holidayDate == date }
            override suspend fun syncHolidays() = Result.success(holidays.size)
        })

    private val useCase = calculator()

    private fun shatb() = LegalRule(
        id = 10,
        actionName = "تجديد الدعوى من الشطب وإعلان صحيفة التجديد",
        duration = 60,
        unit = DurationUnit.DAYS,
        startRule = StartRule.NEXT_DAY,
        lawName = "قانون المرافعات المدنية والتجارية",
        lawArticle = "المادة 82 من قانون المرافعات المدنية والتجارية",
        reviewStatus = "NEEDS_REVIEW",
        calculationKind = "SPECIAL_UNSUPPORTED"
    )

    @Test fun shatbSixtyDaysIsShownAsUnverifiedPreviewNotAnError() = runBlocking {
        val result = useCase(LocalDate.of(2026, 11, 10), shatb())
        assertEquals(LocalDate.of(2027, 1, 9), result.finalDeadline)
        assertTrue(result.milestones.any {
            it.type == MilestoneType.TRIGGER_EVENT && it.title == "تاريخ قرار شطب الدعوى"
        })
        assertTrue(result.isProvisional)
        assertTrue(result.warnings.any { it.contains("المادة 82") && it.contains("إعلان") })
        assertTrue(result.warnings.any { it.contains("استرشادي") })
        assertTrue(result.milestones.any {
            it.type == MilestoneType.FINAL_DEADLINE && it.title.contains("نهاية المدة المحسوبة")
        })
        assertFalse(result.explanation.contains("الميعاد النهائي واجب الالتزام به"))
    }

    @Test fun ordinaryUnreviewedForwardRuleStillReturnsExplicitAdvisoryDate() = runBlocking {
        val rule = LegalRule(
            id = 33, actionName = "استئناف مدني",
            duration = 40, unit = DurationUnit.DAYS,
            startRule = StartRule.NEXT_DAY, reviewStatus = "NEEDS_REVIEW"
        )
        val result = useCase(LocalDate.of(2026, 11, 10), rule)
        assertTrue(result.isProvisional)
        assertTrue(result.warnings.any { it.contains("قيد") || it.contains("المراجعة") })
    }

    @Test fun shatbCannotGetUnverifiedRouteDistanceAdded() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                useCase(LocalDate.of(2026, 11, 10), shatb(), DistanceType.LOCAL, 2)
            }
        }
    }

    @Test fun backwardHourAndLimitationStillFailClosed() {
        for (rule in listOf(
            LegalRule(id = 4, actionName = "قبل الجلسة", duration = 3, unit = DurationUnit.DAYS),
            LegalRule(id = 7, actionName = "ساعة قديمة غير مدعومة", duration = 24, unit = DurationUnit.HOURS),
            LegalRule(id = 93, actionName = "تقادم", duration = 10, unit = DurationUnit.YEARS),
            LegalRule(id = 0, actionName = "مسودة جديدة", duration = 4, unit = DurationUnit.DAYS,
                calculationKind = "SPECIAL_UNSUPPORTED")
        )) {
            assertThrows(IllegalArgumentException::class.java) {
                runBlocking { useCase(LocalDate.of(2026, 11, 10), rule) }
            }
        }
    }

    @Test fun oneDayDisplayDoesNotRoundClockSensitiveDeadlineIntoSaturday() {
        val rule = LegalRule(
            id = 96,
            actionName = "إرسال المقبوض عليه إلى النيابة",
            duration = 1,
            unit = DurationUnit.DAYS,
            calculationKind = "FORWARD"
        )
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { useCase(LocalDate.of(2026, 11, 12), rule) }
        }
    }

    @Test fun cabinetHolidayAndFridayAreAppliedWithoutPromisingCompleteCourtCoverage() = runBlocking {
        val holiday = Holiday(
            holidayDate = LocalDate.parse("2026-10-08"),
            name = "عيد القوات المسلحة",
            verificationStatus = "VERIFIED_OFFICIAL",
            sourceUrl = "https://www.cabinet.gov.eg/News/Details/86219"
        )
        val candidate = holiday.copy(
            holidayDate = LocalDate.parse("2026-10-10"),
            verificationStatus = "UNVERIFIED"
        )
        val seed = LegalRuleSeedData.getSeedRules().single { it.id == 33L }
        val rule = seed.copy(reviewStatus = "VERIFIED_ACCURATE", effectiveFrom = "1968-11-10",
            officialSourceUrl = "https://www.alamiria.com/")
        val result = calculator(listOf(holiday, candidate))(LocalDate.parse("2026-08-29"), rule)
        assertEquals(LocalDate.parse("2026-10-10"), result.finalDeadline)
        assertTrue(result.isProvisional)
    }

    @Test fun settlementUsesVerifiedEditionsOnBothSidesOfThe2025Transition() = runBlocking {
        val editions = ReleaseRuleCatalog.editions(LegalRuleSeedData.getSeedRules()).associateBy { it.id }
        val modern = editions.getValue(ReleaseRuleCatalog.SETTLEMENT_CURRENT_ID)
        val legacy = editions.getValue(156L)
        val original = editions.getValue(ReleaseRuleCatalog.SETTLEMENT_PRE_2008_ID)
        val result = useCase(LocalDate.parse("2026-11-15"), modern)
        assertEquals(LocalDate.parse("2026-11-25"), result.finalDeadline)
        assertTrue(result.lawArticle.contains("149"))
        assertEquals(LocalDate.parse("2025-08-30"), useCase(LocalDate.parse("2025-08-20"), legacy).finalDeadline)
        assertEquals(LocalDate.parse("2008-06-27"), useCase(LocalDate.parse("2008-06-20"), original).finalDeadline)
        assertEquals(listOf(40L, 60L), listOf(125L, ReleaseRuleCatalog.PROSECUTOR_APPEAL_ID).map {
            editions.getValue(it).duration
        })
    }

    @Test fun verifiedRuleRemainsAdvisoryWhenHolidayCoverageIsNotConfirmedComplete() = runBlocking {
        val calculate = calculator(listOf(
                Holiday(
                    holidayDate = LocalDate.of(2026, 1, 7),
                    name = "عطلة موثقة",
                    verificationStatus = "VERIFIED_OFFICIAL",
                    sourceUrl = "https://example.gov.eg"
                )
        ))
        val rule = LegalRule(
            id = 2, actionName = "إجراء", duration = 15, unit = DurationUnit.DAYS,
            reviewStatus = "VERIFIED_ACCURATE"
        )
        val result = calculate(
            LocalDate.of(2026, 11, 1), rule
        )
        assertTrue(result.warnings.any { it.contains("اكتمال") })
        assertTrue(result.isProvisional)
    }

    @Test fun obsoleteLaborRuleIsNotPreviewedAsCurrentLaw() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                useCase(LocalDate.of(2026, 11, 10), LegalRule(
                    id = 159, actionName = "قانون العمل القديم",
                    duration = 2, unit = DurationUnit.MONTHS
                ))
            }
        }
    }
}
