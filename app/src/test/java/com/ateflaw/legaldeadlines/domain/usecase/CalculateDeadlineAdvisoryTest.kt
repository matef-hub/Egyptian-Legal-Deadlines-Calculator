package com.ateflaw.legaldeadlines.domain.usecase

import com.ateflaw.legaldeadlines.domain.calculator.LegalDeadlineCalculator
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
    private val source = object : HolidayRepository {
        override fun getAllHolidays(): Flow<List<Holiday>> = flowOf(emptyList())
        override suspend fun getAllHolidaysList(): List<Holiday> = emptyList()
        override suspend fun getHolidaysCount(): Int = 0
        override suspend fun getHolidayByDate(date: LocalDate): Holiday? = null
        override suspend fun isHoliday(date: LocalDate): Boolean = false
        override suspend fun getHolidayName(date: LocalDate): String? = null
        override suspend fun insertHolidays(holidays: List<Holiday>) {}
        override suspend fun syncHolidays(year: Int, forceRefresh: Boolean): Result<Int> = Result.success(0)
    }

    private val useCase = CalculateDeadlineUseCase(LegalDeadlineCalculator(), source)

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
        assertTrue(result.isProvisional)
        assertTrue(result.warnings.any { it.contains("المادة 82") && it.contains("إعلان") })
        assertTrue(result.warnings.any { it.contains("استرشادي") })
        assertTrue(result.milestones.any {
            it.type == MilestoneType.FINAL_DEADLINE && it.title.contains("غير معتمد")
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
            LegalRule(id = 7, actionName = "24 ساعة", duration = 1, unit = DurationUnit.DAYS),
            LegalRule(id = 93, actionName = "تقادم", duration = 10, unit = DurationUnit.YEARS),
            LegalRule(id = 0, actionName = "مسودة جديدة", duration = 4, unit = DurationUnit.DAYS,
                calculationKind = "SPECIAL_UNSUPPORTED")
        )) {
            assertThrows(IllegalArgumentException::class.java) {
                runBlocking { useCase(LocalDate.of(2026, 11, 10), rule) }
            }
        }
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
