package com.ateflaw.legaldeadlines.domain.calculator

import com.ateflaw.legaldeadlines.domain.model.DurationUnit
import com.ateflaw.legaldeadlines.domain.model.LegalRule
import com.ateflaw.legaldeadlines.domain.model.StartRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Real unit tests for [LegalDeadlineCalculator] verifying all mandatory edge cases:
 * - Friday is always excluded.
 * - Saturday is a working day and must remain.
 * - Official holidays are excluded.
 * - Consecutive exclusions roll over properly.
 * - SAME_DAY vs NEXT_DAY counting.
 * - Months and years arithmetic with leap years and short months.
 * - Accurate Arabic explanation generation.
 */
class LegalDeadlineCalculatorTest {

    private lateinit var calculator: LegalDeadlineCalculator

    @Before
    fun setUp() {
        calculator = LegalDeadlineCalculator()
    }

    /**
     * Test A (Section 41):
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
     * Test B (Section 41):
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
     * Test C (Section 41):
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
     * Test D (Section 41):
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
        // Friday excluded (weekly), Saturday excluded (official holiday) -> Sunday 2026-10-04!
        assertEquals(LocalDate.of(2026, 10, 4), result.finalDeadline)
        assertEquals(DayOfWeek.SUNDAY, result.finalDeadline.dayOfWeek)
        assertEquals(2, result.excludedDays.size)
    }

    /**
     * Test E (Section 41):
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
     * Test F (Section 41):
     * When month addition reaches a shorter month: java.time handles end-of-month correctly.
     */
    @Test
    fun testF_whenMonthAdditionReachesShorterMonth_handlesEndOfMonthCorrectly() {
        // Jan 31 + 1 month -> Feb 28 in non-leap year (2025/2026/2027)
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
     * Test G (Section 41):
     * When year addition crosses a leap-year boundary: Handled correctly via java.time.
     */
    @Test
    fun testG_whenYearAdditionCrossesLeapYear_handlesCorrectly() {
        // Feb 29 2024 (leap year) + 1 year -> Feb 28 2025
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
     * Distance Days Test:
     * Calculates rule.distanceDays + additionalDistanceDays and appends to provisional date.
     */
    @Test
    fun testDistanceDays_legalAndAdditionalCombined() {
        val announcementDate = LocalDate.of(2026, 4, 1) // Wednesday
        val rule = LegalRule(
            id = 8,
            actionName = "اختبار مواعيد المسافة",
            duration = 10,
            unit = DurationUnit.DAYS,
            distanceDays = 3,
            startRule = StartRule.SAME_DAY
        )

        val additionalDistanceDays = 2 // Total distance = 3 + 2 = 5 days
        val result = calculator.calculate(announcementDate, rule, additionalDistanceDays)

        assertEquals(3, result.ruleDistanceDays)
        assertEquals(2, result.additionalDistanceDays)
        assertEquals(5, result.totalDistanceDays)
        // Day 1: Apr 1. Duration 10 days = Apr 10. Plus 5 distance days = Apr 15.
        assertEquals(LocalDate.of(2026, 4, 15), result.provisionalDate)
        assertEquals(LocalDate.of(2026, 4, 15), result.finalDeadline)
    }

    /**
     * Arabic explanation contains all required elements (Section 12).
     */
    @Test
    fun testArabicExplanation_containsAllMandatoryElements() {
        val announcementDate = LocalDate.of(2026, 10, 1)
        val rule = LegalRule(
            id = 9,
            actionName = "الطعن بالاستئناف في المواد المدنية",
            duration = 40,
            unit = DurationUnit.DAYS,
            distanceDays = 1,
            startRule = StartRule.NEXT_DAY,
            lawArticle = "المادة 227 مرافعات"
        )

        val result = calculator.calculate(announcementDate, rule, additionalDistanceDays = 2)

        assertTrue(result.explanation.contains("١. "))
        assertTrue(result.explanation.contains("٢. تاريخ بدء احتساب الميعاد"))
        assertTrue(result.explanation.contains("٣. مدة الميعاد القانوني"))
        assertTrue(result.explanation.contains("٤. وحدة الاحتساب"))
        assertTrue(result.explanation.contains("٥. ميعاد المسافة"))
        assertTrue(result.explanation.contains("٦. الميعاد المبدئي"))
        assertTrue(result.explanation.contains("٧. الأيام المستبعدة"))
        assertTrue(result.explanation.contains("٨. الميعاد النهائي واجب الالتزام به"))
    }

    /**
     * Test H (Audit P0 Requirement):
     * Event on 31/01/2026 with 1 Month and NEXT_DAY:
     * Validates Article 15 of Egyptian Procedures Law:
     * Event date 31/01/2026, counting starts 01/02/2026.
     * Completes on corresponding day of next month (28/02/2026 since Feb has no 31).
     * 28/02/2026 is Saturday (valid working day in Egyptian courts).
     */
    @Test
    fun testH_whenMonthAdditionWithNextDay_onJan31_handlesEndOfMonthAndSaturday() {
        val announcementDate = LocalDate.of(2026, 1, 31)
        val rule = LegalRule(
            id = 10,
            actionName = "التماس إعادة النظر في المواد المدنية",
            duration = 1,
            unit = DurationUnit.MONTHS,
            startRule = StartRule.NEXT_DAY,
            lawArticle = "المادة 242 من قانون المرافعات"
        )

        val result = calculator.calculate(announcementDate, rule)

        assertEquals(LocalDate.of(2026, 2, 1), result.startDate)
        assertEquals(LocalDate.of(2026, 2, 28), result.provisionalDate)
        assertEquals(LocalDate.of(2026, 2, 28), result.finalDeadline)
        assertEquals(DayOfWeek.SATURDAY, result.finalDeadline.dayOfWeek)
        assertTrue(result.excludedDays.isEmpty())
    }

    /**
     * Test I:
     * Validates that structured Milestones list is correctly constructed for timeline rendering.
     */
    @Test
    fun testI_structuredMilestones_containsAllLifecycleStages() {
        val announcementDate = LocalDate.of(2026, 10, 1) // Thursday
        val rule = LegalRule(
            id = 11,
            actionName = "طعن بالاستئناف",
            duration = 40,
            unit = DurationUnit.DAYS,
            distanceDays = 2,
            startRule = StartRule.NEXT_DAY,
            lawArticle = "المادة 227 مرافعات"
        )

        val result = calculator.calculate(announcementDate, rule, additionalDistanceDays = 1)

        val milestones = result.milestones
        assertTrue(milestones.isNotEmpty())
        assertTrue(milestones[0].title.contains("الواقعة") || milestones[0].title.contains("الإعلان"))
        assertEquals(announcementDate, milestones[0].date)
        assertEquals("بدء سريان الميعاد الإجرائي", milestones[1].title)
        assertEquals(announcementDate.plusDays(1), milestones[1].date)
        assertTrue(milestones.any { it.title.contains("نهاية المدة") })
        assertTrue(milestones.any { it.title.contains("مسافة") })
        assertEquals("الميعاد النهائي واجب الالتزام به", milestones.last().title)
    }

    /**
     * Test J:
     * Distance validation according to Article 16 of Procedures Law:
     * Local distance cannot exceed 4 days.
     */
    @Test(expected = IllegalArgumentException::class)
    fun testJ_distanceValidation_localExceeds4Days_throws() {
        val announcementDate = LocalDate.of(2026, 5, 1)
        val rule = LegalRule(
            id = 12,
            actionName = "اختبار مسافة محلية غير صالحة",
            duration = 15,
            unit = DurationUnit.DAYS
        )

        calculator.calculate(
            announcementDate = announcementDate,
            rule = rule,
            distanceType = com.ateflaw.legaldeadlines.domain.model.DistanceType.LOCAL,
            additionalDistanceDays = 5 // Maximum is 4 days!
        )
    }

    /**
     * Test K:
     * Distance validation: NONE cannot have additional distance days.
     */
    @Test(expected = IllegalArgumentException::class)
    fun testK_distanceValidation_noneWithAdditionalDays_throws() {
        val announcementDate = LocalDate.of(2026, 5, 1)
        val rule = LegalRule(
            id = 13,
            actionName = "اختبار بدون مسافة مع أيام مضافة",
            duration = 15,
            unit = DurationUnit.DAYS
        )

        calculator.calculate(
            announcementDate = announcementDate,
            rule = rule,
            distanceType = com.ateflaw.legaldeadlines.domain.model.DistanceType.NONE,
            additionalDistanceDays = 2 // Cannot add days with NONE
        )
    }

    /**
     * Test L:
     * Domiciled abroad distance under Article 17 of Procedures Law:
     * 60 days standard distance applied properly.
     */
    @Test
    fun testL_distanceAbroad_article17_appliesAccurately() {
        val announcementDate = LocalDate.of(2026, 3, 1) // Sunday
        val rule = LegalRule(
            id = 14,
            actionName = "إعلان لمن موطنه بالخارج",
            duration = 30,
            unit = DurationUnit.DAYS,
            startRule = StartRule.NEXT_DAY
        )

        val result = calculator.calculate(
            announcementDate = announcementDate,
            rule = rule,
            distanceType = com.ateflaw.legaldeadlines.domain.model.DistanceType.ABROAD,
            additionalDistanceDays = 60
        )

        assertEquals(60, result.additionalDistanceDays)
        assertEquals(com.ateflaw.legaldeadlines.domain.model.DistanceType.ABROAD, result.distanceType)
        assertTrue(result.explanation.contains("المادة 17 من قانون المرافعات"))
    }

    /**
     * Test M:
     * Leap year February 29th arithmetic in 2024 (leap year) + 1 month = March 29th.
     */
    @Test
    fun testM_leapYearFeb29_oneMonth_reachesMarch29() {
        val announcementDate = LocalDate.of(2024, 2, 29)
        val rule = LegalRule(
            id = 15,
            actionName = "اختبار شهر بعد 29 فبراير",
            duration = 1,
            unit = DurationUnit.MONTHS,
            startRule = StartRule.SAME_DAY
        )

        val result = calculator.calculate(announcementDate, rule)
        assertEquals(LocalDate.of(2024, 3, 29), result.provisionalDate)
    }
}

