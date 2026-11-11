package com.ateflaw.legaldeadlines.data.review

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Regression checks for the user's explicitly approved correction set. */
class ReviewedWorkbookRecordTest {
    private fun rule(id: Long, method: String = "أمامي", decision: String = "VERIFIED_ACCURATE") =
        ReviewedWorkbookRecord(
            id = id,
            decision = decision,
            method = method,
            sourceUrl = "https://www.alamiria.com/",
            effectiveFrom = "2026-01-01",
            reviewerNote = "قرار المراجع المرتبط بالمادة"
        )

    @Test fun sevenWorkbookCorrectionsAreRepresentedExactly() {
        val corrected = mapOf(
            11L to 15L,
            16L to 2L,
            74L to 5L,
            131L to 1L,
            132L to 0L,
            159L to 3L,
            161L to 10L
        )
        for ((id, duration) in corrected) {
            assertEquals("Rule $id", duration, rule(id).correctedDuration)
        }
        assertNull(rule(82L).correctedDuration)
    }

    @Test fun twentyFourHourEntriesAreKeptAsOneDayWithoutHourCalculation() {
        listOf(7L, 24L, 96L, 97L, 108L).forEach { id ->
            val entry = rule(id, method = "ساعات")
            assertEquals("FORWARD", entry.normalizedKind)
            assertNull(entry.correctedDuration)
        }
    }

    @Test fun nonNumericalProceduralConditionRemainsBlocked() {
        assertEquals("SPECIAL_UNSUPPORTED", rule(17L, "أخرى", "KEEP_BLOCKED").normalizedKind)
        assertEquals(0L, rule(132L, "تقادم", "NEEDS_CORRECTION").correctedDuration)
    }

    @Test fun updatedCurrentLawReferencesAreNotStale() {
        assertTrue(rule(159L).correctedLawArticle!!.contains("14 لسنة 2025"))
        assertTrue(rule(161L).correctedLawArticle!!.contains("167"))
        assertTrue(rule(132L).correctedActionName!!.contains("إثبات"))
    }
}
