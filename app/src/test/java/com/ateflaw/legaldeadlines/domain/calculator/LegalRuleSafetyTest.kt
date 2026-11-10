package com.ateflaw.legaldeadlines.domain.calculator

import com.ateflaw.legaldeadlines.domain.model.DurationUnit
import com.ateflaw.legaldeadlines.domain.model.LegalRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.LocalDate

class LegalRuleSafetyTest {
    private fun seeded(id: Long, duration: Long = 30, unit: DurationUnit = DurationUnit.DAYS): LegalRule =
        LegalRule(id = id, actionName = "قاعدة للاختبار", duration = duration, unit = unit)

    @Test fun backwardsAndHoursAreNeverTreatedAsForwardDeadlines() {
        assertEquals(LegalRuleSafety.CalculationKind.BACKWARD_FROM_EVENT, LegalRuleSafety.kind(seeded(4)))
        assertEquals(LegalRuleSafety.CalculationKind.FORWARD, LegalRuleSafety.kind(seeded(7, 1)))
        assertEquals(LegalRuleSafety.CalculationKind.FORWARD, LegalRuleSafety.kind(seeded(96, 1)))
        assertEquals(LegalRuleSafety.CalculationKind.SPECIAL_UNSUPPORTED, LegalRuleSafety.kind(seeded(103, 8)))
        assertEquals(LegalRuleSafety.CalculationKind.SPECIAL_UNSUPPORTED, LegalRuleSafety.kind(seeded(104, 4)))
        assertEquals(LegalRuleSafety.CalculationKind.AGE_THRESHOLD, LegalRuleSafety.kind(seeded(134, 15, DurationUnit.YEARS)))
        assertEquals(LegalRuleSafety.CalculationKind.LIMITATION_OR_CONDITION, LegalRuleSafety.kind(seeded(93, 10, DurationUnit.YEARS)))
    }

    @Test fun oldLaborRulesBlockedAfterNewLawEffectiveDate() {
        val error = assertThrows(IllegalArgumentException::class.java) {
            LegalRuleSafety.validate(seeded(159), LocalDate.of(2026, 11, 10))
        }
        assert(error.message!!.contains("2025"))
    }

    @Test fun oldCriminalRulesBlockedAfterNewLawEffectiveDate() {
        assertThrows(IllegalArgumentException::class.java) {
            LegalRuleSafety.validate(seeded(122), LocalDate.of(2027, 10, 1))
        }
    }

    @Test fun oneDayLegacyRuleExplainsMissingClockPrecision() {
        val source = seeded(7, 1).copy(
            reviewStatus = "NEEDS_REVIEW",
            calculationKind = "EXACT_HOURS"
        )
        val warnings = LegalRuleSafety.previewWarnings(source, LocalDate.of(2026, 11, 10))
        assert(warnings.any { it.contains("24 ساعة") })
    }

    @Test fun unreviewedForwardRuleIsBlocked() {
        assertThrows(IllegalArgumentException::class.java) {
            LegalRuleSafety.validate(seeded(33), LocalDate.of(2026, 5, 1))
        }
    }

    @Test fun explicitlyVerifiedForwardRuleWithOfficialLinkCanBeEvaluated() {
        val rule = seeded(33).copy(
            reviewStatus = "VERIFIED_ACCURATE",
            officialSourceUrl = "https://www.alamiria.com/",
            effectiveFrom = "2025-01-01"
        )
        LegalRuleSafety.validate(rule, LocalDate.of(2026, 5, 1))
    }

    @Test fun verifiedRuleWithMissingSourceIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            LegalRuleSafety.validate(seeded(33).copy(
                reviewStatus = "VERIFIED_ACCURATE", effectiveFrom = "2025-01-01"
            ), LocalDate.of(2026, 5, 1))
        }
    }
}
