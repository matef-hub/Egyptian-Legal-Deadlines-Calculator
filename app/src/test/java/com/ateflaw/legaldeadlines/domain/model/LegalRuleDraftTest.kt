package com.ateflaw.legaldeadlines.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class LegalRuleDraftTest {
    private fun draft(days: String = "15", text: String = "نص المادة") = LegalRuleDraft(
        actionName = "تجربة ميعاد",
        lawName = "قانون تجريبي",
        lawArticle = "المادة 1",
        articleText = text,
        durationText = days
    )

    @Test fun acceptsArabicIndicDigits() {
        assertEquals(15L, draft("١٥").validate(true).duration)
        assertEquals(15L, draft("۱۵").validate(true).duration)
    }

    @Test fun validationRejectsInvalidDayCounts() {
        listOf("", "0", "-2", "1.5", "999999").forEach { days ->
            assertThrows(IllegalArgumentException::class.java) { draft(days).validate(true) }
        }
    }

    @Test fun newRulesNeedFullArticleText() {
        assertThrows(IllegalArgumentException::class.java) { draft(text = "").validate(true) }
        draft(text = "").validate(false)
    }

    @Test fun existingDurationUnitIsPreserved() {
        val rule = LegalRule(id = 8, actionName = "قيد التجربة", duration = 3,
            unit = DurationUnit.MONTHS, lawName = "القانون المدني", lawArticle = "المادة 12")
        val input = LegalRuleDraft.fromRule(rule)
        assertEquals(DurationUnit.MONTHS, input.validate(false).unit)
    }

    @Test fun hoursAreNotAnEditableDurationUnit() {
        assertThrows(IllegalArgumentException::class.java) {
            draft().copy(unit = DurationUnit.HOURS).validate(true)
        }
    }

    @Test fun trimsAllVisibleText() {
        val input = draft().copy(actionName = "  عنوان  ", lawName = " القانون ")
        assertEquals("عنوان", input.validate(true).actionName)
        assertEquals("القانون", input.validate(true).lawName)
    }
}
