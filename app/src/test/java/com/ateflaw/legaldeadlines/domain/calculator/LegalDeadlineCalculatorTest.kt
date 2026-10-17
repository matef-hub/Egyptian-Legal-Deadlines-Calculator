package com.ateflaw.legaldeadlines.domain.calculator

import com.ateflaw.legaldeadlines.domain.model.DistanceType
import com.ateflaw.legaldeadlines.domain.model.DurationUnit
import com.ateflaw.legaldeadlines.domain.model.LegalRule
import com.ateflaw.legaldeadlines.domain.model.MilestoneType
import com.ateflaw.legaldeadlines.domain.model.StartRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Comprehensive unit test suite for [LegalDeadlineCalculator] verifying all Egyptian procedural rules:
 * - Articles 15, 16, 17, and 18 of Egyptian Civil and Commercial Procedures Law No. 13 of 1968.
 * - Friday is always excluded as a weekly rest day.
 * - Saturday is a valid court working day in Egypt and is NEVER excluded.
 * - Official holidays roll over to the next working day.
 * - Consecutive exclusions roll forward until a valid working day.
 * - SAME_DAY vs NEXT_DAY start counting convention.
 * - Month and leap year arithmetic (end-of-month clamping, leap year Feb 29).
 * - Article 16 Domestic distance (1 day per 50 km, fraction > 30 km adds 1 day, cap 4 days).
 * - Article 16 Border areas (statutory 15 days).
 * - Article 17 Ordinary abroad distance (statutory 60 days, rejecting 90 days).
 * - Article 17 Judicial reduction (1..59 days with mandatory order documentation).
 * - Article 17 Served in person in Egypt (0 distance days).
 * - Distance rejected when rule distancePolicy is NOT_APPLICABLE.
 * - Transitional provisions and effective dates (Law 174 of 2025 amended by Law 160 of 2026 effective Oct 1, 2027).
 * - Timeline milestones integrity and Arabic audit explanation.
 */
class LegalDeadlineCalculatorTest {

    private lateinit var calculator: LegalDeadlineCalculator

    @Before
    fun setUp() {
        calculator = LegalDeadlineCalculator()
    }

    /**
     * Test 1: Friday is excluded as a weekly holiday and extends deadline to Saturday (working day).
     */
    @Test
    fun test01_whenProvisionalDeadlineIsFriday_resultMustMoveToSaturday() {
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
     * Test 2: Saturday is a working day in Egyptian courts and must NOT be excluded.
     */
    @Test
    fun test02_whenProvisionalDeadlineIsSaturday_resultMustRemainSaturday() {
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
     * Test 3: Official holiday on provisional deadline moves to next working day.
     */
    @Test
    fun test03_whenProvisionalDeadlineIsOfficialHoliday_resultMovesToNextWorkingDay() {
        val announcementDate = LocalDate.of(2026, 10, 6) // Tuesday (Armed Forces Day)
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
        assertEquals(LocalDate.of(2026, 10, 7), result.finalDeadline)
        assertEquals(DayOfWeek.WEDNESDAY, result.finalDeadline.dayOfWeek)
        assertEquals(1, result.excludedDays.size)
        assertTrue(result.excludedDays.first().reason.contains("عيد القوات المسلحة"))
    }

    /**
     * Test 4: Consecutive exclusions (Friday + Saturday as official holiday) roll over to Sunday.
     */
    @Test
    fun test04_whenExcludedDatesAreConsecutive_continueUntilFirstWorkingDay() {
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
        // Friday excluded (weekly), Saturday excluded (official holiday) -> Sunday Oct 4
        assertEquals(LocalDate.of(2026, 10, 4), result.finalDeadline)
        assertEquals(DayOfWeek.SUNDAY, result.finalDeadline.dayOfWeek)
        assertEquals(2, result.excludedDays.size)
    }

    /**
     * Test 5: StartRule.NEXT_DAY does not count the day of the event (Article 15).
     */
    @Test
    fun test05_whenStartRuleIsNextDay_eventDateMustNotBeStartCountingDate() {
        val announcementDate = LocalDate.of(2026, 5, 10) // Sunday
        val rule = LegalRule(
            id = 5,
            actionName = "اختبار بدء الميعاد من اليوم التالي",
            duration = 10,
            unit = DurationUnit.DAYS,
            startRule = StartRule.NEXT_DAY
        )

        val result = calculator.calculate(announcementDate, rule)

        assertEquals(LocalDate.of(2026, 5, 11), result.startDate)
        assertEquals(LocalDate.of(2026, 5, 20), result.provisionalDate)
        assertEquals(LocalDate.of(2026, 5, 20), result.finalDeadline)
    }

    /**
     * Test 6: Month addition clamping to last day of shorter month (Jan 31 + 1 month = Feb 28).
     */
    @Test
    fun test06_whenMonthAdditionReachesShorterMonth_handlesEndOfMonthCorrectly() {
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
        assertEquals(LocalDate.of(2026, 2, 28), result.finalDeadline)
        assertEquals(DayOfWeek.SATURDAY, result.finalDeadline.dayOfWeek)
    }

    /**
     * Test 7: Leap year Feb 29 + 1 year -> Feb 28 (non-leap year).
     */
    @Test
    fun test07_whenYearAdditionCrossesLeapYear_handlesCorrectly() {
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
        // 2025-02-28 is Friday -> rolls to Saturday 2025-03-01
        assertEquals(LocalDate.of(2025, 3, 1), result.finalDeadline)
        assertEquals(DayOfWeek.SATURDAY, result.finalDeadline.dayOfWeek)
    }

    /**
     * Test 8: Leap year to Leap year (Feb 29 2024 + 4 years -> Feb 29 2028).
     */
    @Test
    fun test08_whenLeapYearToLeapYear_preservesFeb29() {
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
     * Test 9: August 31 + 1 month -> September 30.
     */
    @Test
    fun test09_august31PlusOneMonth_clampsToSeptember30() {
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
     * Test 10: Article 16 Domestic distance kilometer calculation formula:
     * 1 day per 50 km, fraction > 30 km adds 1 day, maximum 4 days.
     */
    @Test
    fun test10_article16_localDistanceFormula_exactCalculations() {
        assertEquals(0, DistanceType.calculateLocalDistanceDays(0.0))
        assertEquals(0, DistanceType.calculateLocalDistanceDays(25.0))
        assertEquals(0, DistanceType.calculateLocalDistanceDays(30.0)) // Not exceeding 30 km
        assertEquals(1, DistanceType.calculateLocalDistanceDays(31.0)) // Exceeding 30 km adds 1 day
        assertEquals(1, DistanceType.calculateLocalDistanceDays(50.0))
        assertEquals(1, DistanceType.calculateLocalDistanceDays(80.0)) // 50 + 30 km = 1 day
        assertEquals(2, DistanceType.calculateLocalDistanceDays(81.0)) // 50 + 31 km = 2 days
        assertEquals(2, DistanceType.calculateLocalDistanceDays(100.0))
        assertEquals(3, DistanceType.calculateLocalDistanceDays(135.0))
        assertEquals(4, DistanceType.calculateLocalDistanceDays(200.0))
        assertEquals(4, DistanceType.calculateLocalDistanceDays(500.0)) // Capped at 4 days
    }

    /**
     * Test 11: Article 16 Domestic distance valid addition (1..4 days).
     */
    @Test
    fun test11_localDistance_addsUpToFourDays() {
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
        assertEquals(LocalDate.of(2026, 4, 14), result.provisionalDate)
        assertEquals(LocalDate.of(2026, 4, 14), result.finalDeadline)
        assertTrue(result.milestones.any { it.legalBasis?.contains("المادة 16") == true })
    }

    /**
     * Test 12: Article 16 Domestic distance exceeding 4 days must throw IllegalArgumentException.
     */
    @Test(expected = IllegalArgumentException::class)
    fun test12_localDistance_exceedingFourDaysThrowsException() {
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
     * Test 13: Article 16 Border areas statutory 15 days distance.
     */
    @Test
    fun test13_article16_borderArea_applies15Days() {
        val announcementDate = LocalDate.of(2026, 5, 1) // Friday
        val rule = LegalRule(
            id = 12,
            actionName = "إجراء لشخص بالمناطق الحدودية النائية",
            duration = 10,
            unit = DurationUnit.DAYS,
            startRule = StartRule.NEXT_DAY
        )

        val result = calculator.calculate(
            announcementDate = announcementDate,
            rule = rule,
            distanceType = DistanceType.BORDER_AREA,
            additionalDistanceDays = 15
        )

        assertEquals(15, result.additionalDistanceDays)
        assertEquals(DistanceType.BORDER_AREA, result.distanceType)
        assertTrue(result.milestones.any { it.description.contains("المناطق الحدودية النائية") })
    }

    /**
     * Test 14: Article 17 Ordinary distance abroad is 60 days (rejecting 90 days).
     */
    @Test
    fun test14_article17_abroadDistance_applies60Days() {
        val announcementDate = LocalDate.of(2026, 1, 1)
        val rule = LegalRule(
            id = 13,
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
        assertTrue(result.explanation.contains("المادة 17 من قانون المرافعات"))
    }

    /**
     * Test 15: Article 17 Domiciled abroad attempting 90 days must throw IllegalArgumentException.
     */
    @Test(expected = IllegalArgumentException::class)
    fun test15_article17_abroadDistance_attempting90DaysThrows() {
        val announcementDate = LocalDate.of(2026, 1, 1)
        val rule = LegalRule(
            id = 14,
            actionName = "اختبار ميعاد 90 يوماً المرفوض",
            duration = 10,
            unit = DurationUnit.DAYS
        )

        calculator.calculate(
            announcementDate = announcementDate,
            rule = rule,
            distanceType = DistanceType.ABROAD,
            additionalDistanceDays = 90 // Illegal under modern Article 17!
        )
    }

    /**
     * Test 16: Article 17 Judicial Reduction with documented court order.
     */
    @Test
    fun test16_article17_judicialReduction_appliesReducedPeriod() {
        val announcementDate = LocalDate.of(2026, 2, 1)
        val rule = LegalRule(
            id = 15,
            actionName = "إنقاص ميعاد المسافة بأمر وقتي",
            duration = 15,
            unit = DurationUnit.DAYS,
            startRule = StartRule.NEXT_DAY
        )

        val result = calculator.calculate(
            announcementDate = announcementDate,
            rule = rule,
            distanceType = DistanceType.ABROAD_JUDICIAL_REDUCTION,
            additionalDistanceDays = 25,
            judicialOrderDetails = "أمر على عريضة رقم 45 لسنة 2026 قاضي الأمور الوقتية بمحكمة جنوب القاهرة"
        )

        assertEquals(25, result.additionalDistanceDays)
        assertEquals(DistanceType.ABROAD_JUDICIAL_REDUCTION, result.distanceType)
        assertTrue(result.explanation.contains("أمر على عريضة رقم 45"))
    }

    /**
     * Test 17: Article 17 Judicial Reduction missing order details throws IllegalArgumentException.
     */
    @Test(expected = IllegalArgumentException::class)
    fun test17_article17_judicialReduction_missingOrderDetailsThrows() {
        val announcementDate = LocalDate.of(2026, 2, 1)
        val rule = LegalRule(
            id = 16,
            actionName = "إنقاص ميعاد المسافة بدون أمر",
            duration = 15,
            unit = DurationUnit.DAYS
        )

        calculator.calculate(
            announcementDate = announcementDate,
            rule = rule,
            distanceType = DistanceType.ABROAD_JUDICIAL_REDUCTION,
            additionalDistanceDays = 25,
            judicialOrderDetails = "" // Missing order details!
        )
    }

    /**
     * Test 18: Article 17 Para 3 Served in person while in Egypt has 0 distance days.
     */
    @Test
    fun test18_article17_abroadServedInEgypt_zeroDistance() {
        val announcementDate = LocalDate.of(2026, 3, 1)
        val rule = LegalRule(
            id = 17,
            actionName = "إعلان لشخصه في مصر",
            duration = 30,
            unit = DurationUnit.DAYS,
            startRule = StartRule.NEXT_DAY
        )

        val result = calculator.calculate(
            announcementDate = announcementDate,
            rule = rule,
            distanceType = DistanceType.ABROAD_SERVED_IN_EGYPT,
            additionalDistanceDays = 0
        )

        assertEquals(0, result.additionalDistanceDays)
        assertEquals(DistanceType.ABROAD_SERVED_IN_EGYPT, result.distanceType)
        assertTrue(result.explanation.contains("المادة 17 فقرة 3 مرافعات"))
    }

    /**
     * Test 19: Rule with distancePolicy = NOT_APPLICABLE rejects distance additions.
     */
    @Test(expected = IllegalArgumentException::class)
    fun test19_ruleDistanceNotApplicable_rejectsDistance() {
        val announcementDate = LocalDate.of(2026, 4, 1)
        val rule = LegalRule(
            id = 18,
            actionName = "إجراء لا يقبل المسافة",
            duration = 8,
            unit = DurationUnit.DAYS,
            distancePolicy = "NOT_APPLICABLE"
        )

        calculator.calculate(
            announcementDate = announcementDate,
            rule = rule,
            distanceType = DistanceType.LOCAL,
            additionalDistanceDays = 2
        )
    }

    /**
     * Test 20: Transitional provisions enforcement:
     * Law 174 of 2025 amended by Law 160 of 2026 has revised commencement date of October 1, 2027.
     * Attempting to calculate under Law 174 before 2027-10-01 must throw IllegalArgumentException.
     */
    @Test(expected = IllegalArgumentException::class)
    fun test20_transitionalProvisions_law174_priorToCommencement_throws() {
        val announcementDate = LocalDate.of(2026, 10, 15) // Before 2027-10-01
        val futureRule = LegalRule(
            id = 19,
            actionName = "الطعن بالاستئناف في قانون الإجراءات الجنائية الجديد",
            duration = 15,
            unit = DurationUnit.DAYS,
            effectiveFrom = "2027-10-01",
            lawName = "قانون الإجراءات الجنائية رقم 174 لسنة 2025 المعدل بالقانون رقم 160 لسنة 2026"
        )

        calculator.calculate(announcementDate, futureRule)
    }

    /**
     * Test 21: Calculation under Law 174 on or after October 1, 2027 succeeds.
     */
    @Test
    fun test21_transitionalProvisions_law174_afterCommencement_succeeds() {
        val announcementDate = LocalDate.of(2027, 10, 5) // After 2027-10-01
        val newCriminalRule = LegalRule(
            id = 20,
            actionName = "استئناف جنحة بموجب القانون رقم 174 لسنة 2025",
            duration = 10,
            unit = DurationUnit.DAYS,
            effectiveFrom = "2027-10-01",
            effectiveTo = null
        )

        val result = calculator.calculate(announcementDate, newCriminalRule)
        assertNotNull(result)
        assertEquals(LocalDate.of(2027, 10, 6), result.startDate)
    }

    /**
     * Test 22: Expired rule calculation throws IllegalArgumentException.
     */
    @Test(expected = IllegalArgumentException::class)
    fun test22_transitionalProvisions_expiredRule_throws() {
        val announcementDate = LocalDate.of(2028, 1, 1)
        val oldRule = LegalRule(
            id = 21,
            actionName = "قاعدة منتهية بقانون قديم",
            duration = 10,
            unit = DurationUnit.DAYS,
            effectiveFrom = "1951-10-15",
            effectiveTo = "2027-09-30"
        )

        calculator.calculate(announcementDate, oldRule)
    }

    /**
     * Test 23: Structured Milestones contains all lifecycle stages in correct order.
     */
    @Test
    fun test23_structuredMilestones_containsAllLifecycleStages() {
        val announcementDate = LocalDate.of(2026, 10, 1) // Thursday
        val rule = LegalRule(
            id = 22,
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
     * Test 24: When there are no distance days and no holiday extension,
     * no phantom distance or holiday extension milestones appear.
     */
    @Test
    fun test24_noDistanceOrHolidays_noPhantomMilestones() {
        val announcementDate = LocalDate.of(2026, 5, 2) // Saturday
        val rule = LegalRule(
            id = 23,
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

    /**
     * Test 25: Arabic explanation contains all required elements.
     */
    @Test
    fun test25_arabicExplanation_containsAllMandatoryElements() {
        val announcementDate = LocalDate.of(2026, 10, 1)
        val rule = LegalRule(
            id = 24,
            actionName = "الطعن بالاستئناف في المواد المدنية",
            duration = 40,
            unit = DurationUnit.DAYS,
            distanceDays = 1,
            startRule = StartRule.NEXT_DAY,
            lawArticle = "المادة 227 مرافعات"
        )

        val result = calculator.calculate(
            announcementDate = announcementDate,
            rule = rule,
            distanceType = DistanceType.LOCAL,
            additionalDistanceDays = 2
        )

        assertTrue(result.explanation.contains("١. "))
        assertTrue(result.explanation.contains("٢. تاريخ بدء احتساب الميعاد"))
        assertTrue(result.explanation.contains("٣. مدة الميعاد القانوني"))
        assertTrue(result.explanation.contains("٤. وحدة الاحتساب"))
        assertTrue(result.explanation.contains("٥. ميعاد المسافة"))
        assertTrue(result.explanation.contains("٦. الميعاد المبدئي"))
        assertTrue(result.explanation.contains("٧. الأيام المستبعدة"))
        assertTrue(result.explanation.contains("٨. الميعاد النهائي واجب الالتزام به"))
    }
}
