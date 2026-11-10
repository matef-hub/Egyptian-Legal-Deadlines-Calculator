package com.ateflaw.legaldeadlines.domain.review

import com.ateflaw.legaldeadlines.data.seed.LegalRuleSeedData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LegalRuleReviewCatalogTest {
    @Test fun all169SeededRulesHaveSpecificReviewFindings() {
        val rules = LegalRuleSeedData.getSeedRules()
        assertEquals(169, rules.size)
        assertEquals(169, LegalRuleReviewCatalog.reviewedCount())
        rules.forEach { rule ->
            val finding = LegalRuleReviewCatalog.finding(rule)
            assertTrue(finding.note.length > 70)
            assertTrue(finding.level != AuditLevel.VERIFIED)
        }
    }
    @Test fun knownCorrectionsAreNeverSilencedByChangingDuration() {
        assertEquals(setOf(131L, 132L, 159L, 161L), LegalRuleReviewCatalog.correctionIds())
        val old = LegalRuleSeedData.getSeedRules().first { it.id == 159L }
        assertEquals(AuditLevel.CORRECTION, LegalRuleReviewCatalog.finding(old.copy(duration = 3)).level)
    }
}
