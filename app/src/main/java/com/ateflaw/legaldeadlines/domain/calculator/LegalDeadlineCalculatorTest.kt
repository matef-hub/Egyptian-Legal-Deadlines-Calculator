package com.ateflaw.legaldeadlines.domain.calculator

import com.ateflaw.legaldeadlines.domain.model.DistanceType
import com.ateflaw.legaldeadlines.domain.model.DurationUnit
import com.ateflaw.legaldeadlines.domain.model.LegalRule
import com.ateflaw.legaldeadlines.domain.model.StartRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Real unit tests for [LegalDeadlineCalculator] verifying all mandatory procedural edge cases:
 * - Friday is always excluded (Article 18).
 * - Saturday is a working day and must remain.
 * - Official holidays are excluded.
 * - Consecutive exclusions roll over properly.
 * - SAME_DAY vs NEXT_DAY counting (Article 15).
 * - Months and years arithmetic with leap years and short months (Article 15 Para 4).
 * - Distance rules under Articles 16 & 17.
 * - Accurate Arabic explanation and milestones generation.
 */
class LegalDeadlineCalculatorTest {

    private lateinit var calculator: LegalDeadlineCalculator

    @Before
    fun setUp() {
        calculator = LegalDeadlineCalculator()
    }

    /**
     * Test A:
     * When provisional deadline is Friday: Result must move to Saturday.
     */
    @Test
    fun testA_whenProvisionalDeadlineIsFriday_resultMustMoveToSaturday() {
        // Thursday 2026-10-01 + 1 day with SAME_DAY = Friday 2026-10-02 (Provisional)
        // Friday must be excluded and moved to Saturday 2026-10-03.
        val announcementDate = LocalDate.of(2026, 10, 1) // Thursday
        val rule = LegalRule(
            id = 1,
            actionName = "اختبار ميعاد يصادف الجمعة",
            duration = 2,
            unit = DurationUnit.DAYS,
            startRule = StartRule.SAME_DAY
        )

        val result = calculator.calculate(announcementDate, rule)

        assertEquals(LocalDate.of(2026, 10, 2), result.provisionalDate)
        assertEquals(DayOfWeek.FRIDAY, result.provisionalDate.dayOfWeek)
        assertEquals(LocalDate.of(2026, 10, 3), result.finalDeadline)
        assertEquals(DayOfWeek.SATURDAY, result.finalDeadline.dayOfWeek)
        assertEquals(1, result.excludedDays.size)
        assertTrue(result.excludedDays.first().reason.contains("الجمعة"))
    }

    /**
     * Test B:
     * When provisional deadline is Saturday: Result must remain Saturday.
     * Saturday IS A WORKING DAY in Egyptian courts.
     */
    @Test
    fun testB_whenProvisionalDeadlineIsSaturday_resultMustRemainSaturday() {
        val announcementDate = LocalDate.of(2026, 10, 1) // Thursday
        val rule = LegalRule(
            id = 2,
            actionName = "اختبار ميعاد يصادف السبت",
            duration = 3,
            unit = DurationUnit.DAYS,
            startRule = StartRule.SAME_DAY
        )

        val result = calculator.calculate(announcementDate, rule)

        assertEquals(LocalDate.of(2026, 10, 3), result.provisionalDate)
        assertEquals(DayOfWeek.SATURDAY, result.provisionalDate.dayOfWeek)
        assertEquals(LocalDate.of(2026, 10, 3), result.finalDeadline)
        assertEquals(DayOfWeek.SATURDAY, result.finalDeadline.dayOfWeek)
        assertTrue(result.excludedDays.isEmpty())
    }

    /**
     * Test C:
     * When provisional deadline is an official holiday: Result must move to the next valid working day.
     */
    @Test
    fun testC_whenProvisionalDeadlineIsOfficialHoliday_resultMovesToNextWorkingDay() {
        // Tuesday 2026-10-06 is Armed Forces Day (Official Holiday)
        val announcementDate = LocalDate.of(2026, 10, 6) // Tuesday
        val rule = LegalRule(
            id = 3,
            actionName = "اختبار عطلة رسمية",
            duration = 1,
            unit = DurationUnit.DAYS,
            startRule = StartRule.SAME_DAY
        )

        val holidays = mapOf(LocalDate.of(2026, 10, 6) to "عيد القوات المسلحة")

        val result = calculator.calculate(
            announcementDate = announcementDate,
            rule = rule,
            isHolidayProvider = { date -> Pair(holidays.containsKey(date), holidays[date]) }
        )

        assertEquals(LocalDate.of(2026, 10, 6), result.provisionalDate)
        // Next day is Wednesday 2026-10-07 (a working day)
        assertEquals(LocalDate.of(2026, 10, 7), result.finalDeadline)
        assertEquals(DayOfWeek.WEDNESDAY, result.finalDeadline.dayOfWeek)
        assertEquals(1, result.excludedDays.size)
        assertTrue(result.excludedDays.first().reason.contains("عيد القوات المسلحة"))
    }

    /**
     * Test D:
     * When excluded dates are consecutive (e.g. Friday + Official Holiday on Saturday):
     * Continue forward until the first valid working day (Sunday).
     */
    @Test
    fun testD_whenExcludedDatesAreConsecutive_continueUntilFirstWorkingDay() {
        val announcementDate = LocalDate.of(2026, 10, 2) // Friday
        val rule = LegalRule(
            id = 4,
            actionName = "اختبار عطلات متتالية",
            duration = 1,
            unit = DurationUnit.DAYS,
            startRule = StartRule.SAME_DAY
        )

        // Saturday Oct 3 is made an official holiday
        val holidays = mapOf(LocalDate.of(2026, 10, 3) to "عطلة رسمية استثنائية")

        val result = calculator.calculate(
            announcementDate = announcementDate,
            rule = rule,
            isHolidayProvider = { date -> Pair(holidays.containsKey(date), holidays[date]) }
        )

        assertEquals(LocalDate.of(2026, 10, 2), result.provisionalDate)
        assertEquals(DayOfWeek.FRIDAY, result.provisionalDate.dayOfWeek)
        // Friday excluded (weekly), Saturday excluded (official holiday) -> Sunday 2026-10-04
        assertEquals(LocalDate.of(2026, 10, 4), result.finalDeadline)
        assertEquals(DayOfWeek.SUNDAY, result.finalDeadline.dayOfWeek)
        assertEquals(2, result.excludedDays.size)
    }

    /**
     * Test E:
     * When StartRule = NEXT_DAY: The event date must NOT be used as the starting counting date.
     */
    @Test
    fun testE_whenStartRuleIsNextDay_eventDateMustNotBeStartCountingDate() {
        val announcementDate = LocalDate.of(2026, 5, 10) // Sunday
        val rule = LegalRule(
            id = 5,
            actionName = "اختبار بدء الميعاد من اليوم التالي",
            duration = 10,
            unit = DurationUnit.DAYS,
            startRule = StartRule.NEXT_DAY
        )

        val result = calculator.calculate(announcementDate, rule)

        // Event date is 10/05/2026, start counting date must be 11/05/2026
        assertEquals(LocalDate.of(2026, 5, 11), result.startDate)
        // 10 days starting 11/05: 11, 12, 13, 14, 15, 16, 17, 18, 19, 20
        assertEquals(LocalDate.of(2026, 5, 20), result.provisionalDate)
        assertEquals(LocalDate.of(2026, 5, 20), result.finalDeadline)
    }

    /**
     * Test F:
     * When month addition reaches a shorter month: clamps to end of month.
     * Jan 31 + 1 month -> Feb 28 in non-leap year (2026).
     */
    @Test
    fun testF_whenMonthAdditionReachesShorterMonth_handlesEndOfMonthCorrectly() {
        val announcementDate = LocalDate.of(2026, 1, 31)
        val rule = LegalRule(
            id = 6,
            actionName = "اختبار إضافة شهر مع شهر أقصر",
            duration = 1,
            unit = DurationUnit.MONTHS,
            startRule = StartRule.SAME_DAY
        )

        val result = calculator.calculate(announcementDate, rule)

        assertEquals(LocalDate.of(2026, 2, 28), result.provisionalDate)
        // 2026-02-28 is Saturday (a working day)
        assertEquals(LocalDate.of(2026, 2, 28), result.finalDeadline)
        assertEquals(DayOfWeek.SATURDAY, result.finalDeadline.dayOfWeek)
    }

    /**
     * Test G:
     * When year addition crosses a leap-year boundary:
     * Feb 29 2024 (leap year) + 1 year -> Feb 28 2025.
     */
    @Test
    fun testG_whenYearAdditionCrossesLeapYear_handlesCorrectly() {
        val announcementDate = LocalDate.of(2024, 2, 29)
        val rule = LegalRule(
            id = 7,
            actionName = "اختبار سنة كبيسة",
            duration = 1,
            unit = DurationUnit.YEARS,
            startRule = StartRule.SAME_DAY
        )

        val result = calculator.calculate(announcementDate, rule)

        assertEquals(LocalDate.of(2025, 2, 28), result.provisionalDate)
        // 2025-02-28 is Friday -> rolls forward to Saturday 2025-03-01
        assertEquals(LocalDate.of(2025, 3, 1), result.finalDeadline)
        assertEquals(DayOfWeek.SATURDAY, result.finalDeadline.dayOfWeek)
    }

    /**
     * Test H:
     * Leap year to Leap year (Feb 29 2024 + 4 years -> Feb 29 2028).
     */
    @Test
    fun testH_whenLeapYearToLeapYear_preservesFeb29() {
        val announcementDate = LocalDate.of(2024, 2, 29)
        val rule = LegalRule(
            id = 8,
            actionName = "اختبار 4 سنوات كبيسة",
            duration = 4,
            unit = DurationUnit.YEARS,
            startRule = StartRule.SAME_DAY
        )

        val result = calculator.calculate(announcementDate, rule)

        assertEquals(LocalDate.of(2028, 2, 29), result.provisionalDate)
        assertEquals(LocalDate.of(2028, 2, 29), result.finalDeadline)
    }

    /**
     * Test I:
     * August 31 + 1 month -> September 30 (since September has 30 days).
     */
    @Test
    fun testI_august31PlusOneMonth_clampsToSeptember30() {
        val announcementDate = LocalDate.of(2026, 8, 31)
        val rule = LegalRule(
            id = 9,
            actionName = "اختبار 31 أغسطس",
            duration = 1,
            unit = DurationUnit.MONTHS,
            startRule = StartRule.NEXT_DAY
        )

        val result = calculator.calculate(announcementDate, rule)

        assertEquals(LocalDate.of(2026, 9, 30), result.provisionalDate)
    }

    /**
     * Test J:
     * Local Distance test (Article 16 Procedures Law):
     * 1 to 4 days allowed, added to provisional date.
     */
    @Test
    fun testJ_localDistance_addsUpToFourDays() {
        val announcementDate = LocalDate.of(2026, 4, 1) // Wednesday
        val rule = LegalRule(
            id = 10,
            actionName = "اختبار مواعيد المسافة المحلية",
            duration = 10,
            unit = DurationUnit.DAYS,
            distanceDays = 0,
            startRule = StartRule.SAME_DAY
        )

        val result = calculator.calculate(
            announcementDate = announcementDate,
            rule = rule,
            distanceType = DistanceType.LOCAL,
            additionalDistanceDays = 4
        )

        assertEquals(4, result.additionalDistanceDays)
        assertEquals(DistanceType.LOCAL, result.distanceType)
        // Day 1: Apr 1. Duration 10 days = Apr 10. Plus 4 distance days = Apr 14.
        assertEquals(LocalDate.of(2026, 4, 14), result.provisionalDate)
        assertEquals(LocalDate.of(2026, 4, 14), result.finalDeadline)
        assertTrue(result.milestones.any { it.legalBasis?.contains("المادة 16") == true })
    }

    /**
     * Test K:
     * Local Distance validation: > 4 days must throw IllegalArgumentException.
     */
    @Test(expected = IllegalArgumentException::class)
    fun testK_localDistance_exceedingFourDaysThrowsException() {
        val announcementDate = LocalDate.of(2026, 4, 1)
        val rule = LegalRule(
            id = 11,
            actionName = "اختبار تجاوز حد المسافة المحلي",
            duration = 10,
            unit = DurationUnit.DAYS
        )

        calculator.calculate(
            announcementDate = announcementDate,
            rule = rule,
            distanceType = DistanceType.LOCAL,
            additionalDistanceDays = 5 // Invalid for Article 16!
        )
    }

    /**
     * Test L:
     * Domiciled Abroad Distance test (Article 17 Procedures Law):
     * 60 days distance correctly applied.
     */
    @Test
    fun testL_abroadDistance_appliesArticle17() {
        val announcementDate = LocalDate.of(2026, 1, 1)
        val rule = LegalRule(
            id = 12,
            actionName = "اختبار ميعاد مسافة الخارج",
            duration = 10,
            unit = DurationUnit.DAYS,
            startRule = StartRule.SAME_DAY
        )

        val result = calculator.calculate(
            announcementDate = announcementDate,
            rule = rule,
            distanceType = DistanceType.ABROAD,
            additionalDistanceDays = 60
        )

        assertEquals(60, result.additionalDistanceDays)
        assertEquals(DistanceType.ABROAD, result.distanceType)
        assertTrue(result.milestones.any { it.legalBasis?.contains("المادة 17") == true })
    }

    /**
     * Test M:
     * Structured Milestones contains all lifecycle stages in correct order.
     */
    @Test
    fun testM_structuredMilestones_containsAllLifecycleStages() {
        val announcementDate = LocalDate.of(2026, 10, 1) // Thursday
        val rule = LegalRule(
            id = 13,
            actionName = "طعن بالاستئناف",
            duration = 40,
            unit = DurationUnit.DAYS,
            distanceDays = 0,
            startRule = StartRule.NEXT_DAY,
            lawArticle = "المادة 227 مرافعات",
            triggerEventLabel = "تاريخ صدور الحكم الحضوري أو إعلانه"
        )

        val result = calculator.calculate(
            announcementDate = announcementDate,
            rule = rule,
            distanceType = DistanceType.LOCAL,
            additionalDistanceDays = 2
        )

        val milestones = result.milestones
        assertTrue(milestones.isNotEmpty())
        assertEquals(rule.triggerEventLabel, milestones[0].title)
        assertEquals(announcementDate, milestones[0].date)
        assertEquals("بدء سريان الميعاد الإجرائي", milestones[1].title)
        assertEquals(announcementDate.plusDays(1), milestones[1].date)
        assertTrue(milestones.any { it.title.contains("نهاية المدة") })
        assertTrue(milestones.any { it.title.contains("مسافة") })
        assertEquals("الميعاد النهائي واجب الالتزام به", milestones.last().title)
    }

    /**
     * Test N:
     * When there are no distance days and no holiday extension,
     * no phantom distance or holiday extension milestones should appear.
     */
    @Test
    fun testN_noDistanceOrHolidays_noPhantomMilestones() {
        val announcementDate = LocalDate.of(2026, 5, 2) // Saturday
        val rule = LegalRule(
            id = 14,
            actionName = "اختبار بدون امتدادات",
            duration = 3,
            unit = DurationUnit.DAYS,
            startRule = StartRule.SAME_DAY
        )

        val result = calculator.calculate(
            announcementDate = announcementDate,
            rule = rule,
            distanceType = DistanceType.NONE,
            additionalDistanceDays = 0
        )

        val milestones = result.milestones
        assertFalse(milestones.any { it.title.contains("مسافة") })
        assertFalse(milestones.any { it.title.contains("عطلة") })
        assertEquals(LocalDate.of(2026, 5, 4), result.finalDeadline)
    }
}
