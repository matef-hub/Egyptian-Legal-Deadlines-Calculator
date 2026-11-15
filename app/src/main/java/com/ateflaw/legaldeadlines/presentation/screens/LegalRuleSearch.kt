package com.ateflaw.legaldeadlines.presentation.screens

import com.ateflaw.legaldeadlines.domain.model.LegalRule
import com.ateflaw.legaldeadlines.utils.normalizedArabicSearchText

internal fun filterLegalRules(
    rules: List<LegalRule>,
    searchQuery: String,
    selectedRule: LegalRule?
): List<LegalRule> {
    val query = searchQuery.normalizedArabicSearchText()
    if (query.isBlank() || query == selectedRule?.actionName?.normalizedArabicSearchText()) return rules
    return rules.mapNotNull { rule ->
        val name = rule.actionName.normalizedArabicSearchText()
        val rank = when {
            name.startsWith(query) -> 0
            name.contains(query) -> 1
            rule.lawArticle.normalizedArabicSearchText().contains(query) -> 2
            rule.lawName.normalizedArabicSearchText().contains(query) ||
                rule.notes.normalizedArabicSearchText().contains(query) -> 3
            else -> return@mapNotNull null
        }
        rule to rank
    }.sortedBy { it.second }.map { it.first }
}
