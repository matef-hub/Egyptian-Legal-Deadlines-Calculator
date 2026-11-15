package com.ateflaw.legaldeadlines.presentation.screens

import com.ateflaw.legaldeadlines.domain.model.LegalRule

internal fun filterLegalRules(
    rules: List<LegalRule>,
    searchQuery: String,
    selectedRule: LegalRule?
): List<LegalRule> {
    val query = searchQuery.trim()
    return if (query.isBlank() || (selectedRule != null && query == selectedRule.actionName)) {
        rules.take(30)
    } else {
        rules.filter { rule ->
            rule.actionName.contains(query, ignoreCase = true) ||
                rule.lawArticle.contains(query, ignoreCase = true) ||
                rule.notes.contains(query, ignoreCase = true)
        }.sortedBy { rule ->
            when {
                rule.actionName.startsWith(query, ignoreCase = true) -> 0
                rule.actionName.contains(query, ignoreCase = true) -> 1
                rule.lawArticle.contains(query, ignoreCase = true) -> 2
                else -> 3
            }
        }.take(30)
    }
}
