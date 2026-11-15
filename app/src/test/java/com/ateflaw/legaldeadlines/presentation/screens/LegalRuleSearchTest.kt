package com.ateflaw.legaldeadlines.presentation.screens

import com.ateflaw.legaldeadlines.data.seed.LegalRuleSeedData
import com.ateflaw.legaldeadlines.domain.model.DurationUnit
import com.ateflaw.legaldeadlines.domain.model.LegalRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LegalRuleSearchTest {
    @Test
    fun appealSearchPutsActionNameMatchesBeforeNotesMatches() {
        // Room supplies rules ordered by action_name, as in the calculator.
        val rules = LegalRuleSeedData.getSeedRules().sortedBy { it.actionName }

        val results = filterLegalRules(rules, "استئناف", selectedRule = null)

        assertEquals(107L, results.first().id)
    }

    @Test
    fun appealSearchRanksVisibleMatchesAboveNotes() {
        val rules = LegalRuleSeedData.getSeedRules()
            .filter { it.id in setOf(115L, 6L, 107L) }
            .sortedBy { it.actionName }

        val results = filterLegalRules(rules, "استئناف", selectedRule = null)

        assertEquals(listOf(107L, 6L, 115L), results.map { it.id })
    }

    @Test
    fun nameAndArticleMatchesAreRankedBeforeNotesBeforeApplyingLimit() {
        val notesMatches = (1L..31L).map { id ->
            rule(id = id, actionName = "إجراء $id", notes = "محكمة الاستئناف")
        }
        val articleMatch = rule(40L, "إجراء آخر", lawArticle = "مادة عن الاستئناف")
        val nameMatch = rule(41L, "ميعاد الاستئناف")
        val prefixMatch = rule(42L, "استئناف حكم")

        val results = filterLegalRules(
            notesMatches + articleMatch + nameMatch + prefixMatch,
            "استئناف",
            selectedRule = null
        )

        assertEquals(30, results.size)
        assertEquals(listOf(42L, 41L, 40L), results.take(3).map { it.id })
        assertTrue(results.drop(3).all { it.id in 1L..31L })
    }

    @Test
    fun blankSearchKeepsOriginalOrderAndLimit() {
        val rules = (1L..35L).map { rule(it, "إجراء $it") }

        val results = filterLegalRules(rules, "  ", selectedRule = null)

        assertEquals(rules.take(30), results)
    }

    @Test
    fun articleNumberAndNotesRemainSearchable() {
        val articleMatch = rule(1L, "إجراء أول", lawArticle = "المادة 227")
        val notesMatch = rule(2L, "إجراء ثان", notes = "تبدأ بعد 227 يوماً")
        val noMatch = rule(3L, "إجراء ثالث")

        val results = filterLegalRules(
            listOf(notesMatch, noMatch, articleMatch),
            " 227 ",
            selectedRule = null
        )

        assertEquals(listOf(1L, 2L), results.map { it.id })
    }

    private fun rule(
        id: Long,
        actionName: String,
        lawArticle: String = "",
        notes: String = ""
    ) = LegalRule(
        id = id,
        actionName = actionName,
        duration = 10,
        unit = DurationUnit.DAYS,
        lawArticle = lawArticle,
        notes = notes
    )
}
