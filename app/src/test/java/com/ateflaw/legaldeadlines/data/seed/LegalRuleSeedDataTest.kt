package com.ateflaw.legaldeadlines.data.seed

import com.ateflaw.legaldeadlines.domain.calculator.LegalRuleSafety
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LegalRuleSeedDataTest {
    @Test fun everyBuiltInRuleHasUniqueIdAndUnverifiedStatus() {
        val rules = LegalRuleSeedData.getSeedRules()
        assertEquals(169, rules.size)
        assertEquals(169, rules.map { it.id }.toSet().size)
        assertTrue(rules.all { it.reviewStatus == "NEEDS_REVIEW" })
        assertTrue(rules.all { it.effectiveFrom.isBlank() })
        assertTrue(rules.all { it.officialSourceUrl.isBlank() })
        assertTrue(rules.all { it.revision == 1 })
        assertTrue(rules.all { it.calculationKind != "UNREVIEWED" })
        val byId = rules.associateBy { it.id }
        assertEquals("القانون المدني", byId.getValue(61).lawName)
        assertEquals("قانون الإثبات", byId.getValue(21).lawName)
        assertEquals("قانون الإثبات", byId.getValue(24).lawName)
        assertEquals("قانون الرسوم القضائية رقم 90 لسنة 1944", byId.getValue(57).lawName)
        assertEquals("قانون الإجراءات الجنائية", byId.getValue(122).lawName)
        assertEquals("قانون العمل", byId.getValue(159).lawName)
        assertEquals("قانون حالات وإجراءات الطعن أمام محكمة النقض رقم 57 لسنة 1959", byId.getValue(128).lawName)
        assertEquals("قانون إنشاء محاكم الأسرة رقم 10 لسنة 2004", byId.getValue(142).lawName)
        assertEquals("قانون البناء", byId.getValue(168).lawName)
    }

    @Test fun legacyWorkAndCriminalRulesHaveExplicitExpiryBoundaries() {
        val rules = LegalRuleSeedData.getSeedRules().associateBy { it.id }
        for (id in 147L..167L) assertEquals("2025-08-31", rules.getValue(id).effectiveTo)
        for (id in 92L..128L) assertEquals("2027-09-30", rules.getValue(id).effectiveTo)
    }

    @Test fun dangerousRuleFamiliesAreNotDeclaredAsForward() {
        val rules = LegalRuleSeedData.getSeedRules().associateBy { it.id }
        assertEquals(LegalRuleSafety.CalculationKind.BACKWARD_FROM_EVENT, LegalRuleSafety.kind(rules.getValue(4)))
        assertEquals(LegalRuleSafety.CalculationKind.FORWARD, LegalRuleSafety.kind(rules.getValue(7)))
        assertEquals(LegalRuleSafety.CalculationKind.LIMITATION_OR_CONDITION, LegalRuleSafety.kind(rules.getValue(93)))
        assertEquals(LegalRuleSafety.CalculationKind.AGE_THRESHOLD, LegalRuleSafety.kind(rules.getValue(136)))
    }

    @Test fun published2026PresidencyDatesAreSourcedAndUnique() {
        val dates = LegalRuleSeedData.getInitialOfficialHolidays()
        assertEquals(21, dates.size)
        assertEquals(21, dates.map { it.holidayDate }.toSet().size)
        assertTrue(dates.all { it.verificationStatus == "VERIFIED_OFFICIAL" })
        assertTrue(dates.all { it.sourceUrl.startsWith("https://www.presidency.eg/") })
    }
}
