package com.ateflaw.legaldeadlines.domain.review

import com.ateflaw.legaldeadlines.data.seed.LegalRuleSeedData
import org.junit.Assert.assertEquals
import com.ateflaw.legaldeadlines.domain.model.DurationUnit
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
    @Test fun addingFullArticleMovesRule82OutOfMissingTextFilter() {
        val seed = LegalRuleSeedData.getSeedRules().first { it.id == 82L }
        val missing = seed.copy(articleText = "", officialSourceUrl = "")
        assertEquals(AuditLevel.REVIEW, LegalRuleReviewCatalog.finding(missing).level)

        val userText = missing.copy(articleText = "تم إدخال نص المادة 940، وينتظر المقارنة")
        assertEquals(AuditLevel.TEXT_ADDED, LegalRuleReviewCatalog.finding(userText).level)
        assertTrue(LegalRuleReviewCatalog.finding(userText).note.contains("تم حفظ نص المادة"))
    }

    @Test fun publishedArticle940TextMatchesButDoesNotAutomaticallyApproveWholeRule() {
        val seed = LegalRuleSeedData.getSeedRules().first { it.id == 82L }
        assertEquals(15L, seed.duration)
        assertEquals(DurationUnit.DAYS, seed.unit)
        assertEquals(LegalRuleReviewCatalog.ARTICLE_940_TEXT, seed.articleText)
        assertEquals(AuditLevel.ARTICLE_MATCHED, LegalRuleReviewCatalog.finding(seed).level)
        assertTrue(LegalRuleReviewCatalog.finding(seed).note.contains("المسافة"))
        assertEquals("NEEDS_REVIEW", seed.reviewStatus)
    }

    @Test fun changingArticleTextOrDurationInvalidatesAutomatedTextMatch() {
        val seed = LegalRuleSeedData.getSeedRules().first { it.id == 82L }
        assertEquals(AuditLevel.TEXT_ADDED, LegalRuleReviewCatalog.finding(
            seed.copy(articleText = seed.articleText + " نص إضافي لا يوجد في المصدر")
        ).level)
        assertEquals(AuditLevel.TEXT_ADDED, LegalRuleReviewCatalog.finding(seed.copy(duration = 16L)).level)
    }

    @Test fun userApprovedEditedRuleIsFinalAndRetainsWorkbookObservation() {
        val prior = LegalRuleSeedData.getSeedRules().first { it.id == 82L }
        val saved = prior.copy(
            articleText = "نص المادة الكامل الذي حُفظ",
            reviewStatus = "VERIFIED_ACCURATE",
            workbookDecision = "USER_APPROVED",
            reviewVersion = 1,
            reviewerNote = "ملاحظة قانونية من ملف المراجعة",
            revision = 3
        )
        val finding = LegalRuleReviewCatalog.finding(saved)
        assertEquals(AuditLevel.VERIFIED, finding.level)
        assertTrue(finding.note.contains("حفظه نهائيًا"))
        assertTrue(finding.note.contains("ملاحظة قانونية من ملف المراجعة"))
    }

}
