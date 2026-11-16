package com.ateflaw.legaldeadlines.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ateflaw.legaldeadlines.domain.model.DeadlineResult
import com.ateflaw.legaldeadlines.domain.model.DurationUnit
import com.ateflaw.legaldeadlines.domain.model.LegalRule
import com.ateflaw.legaldeadlines.domain.model.LegalRuleDraft
import com.ateflaw.legaldeadlines.presentation.components.CalculationResultCard
import com.ateflaw.legaldeadlines.presentation.screens.RulesCatalogScreen
import com.ateflaw.legaldeadlines.presentation.ui.theme.EgyptianLegalDeadlinesTheme
import com.ateflaw.legaldeadlines.presentation.viewmodel.RuleCatalogUiState
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReleaseReadinessUiTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun resultShowsSpecificWarningsAndRuleNotesBeforeSave() {
        val warning = "بيانات عطلات سنة ٢٠٢٧ ناقصة"
        val notes = "هذا الإجراء يتطلب مراجعة توقيت الواقعة"
        val date = LocalDate.of(2027, 1, 30)
        val result = DeadlineResult(
            startDate = date.minusDays(15),
            provisionalDate = date,
            duration = 15,
            unit = DurationUnit.DAYS,
            ruleDistanceDays = 0,
            additionalDistanceDays = 0,
            totalDistanceDays = 0,
            excludedDays = emptyList(),
            finalDeadline = date,
            explanation = "شرح الحساب",
            lawArticle = "مادة تجريبية",
            notes = notes,
            warnings = listOf(warning),
            isProvisional = true
        )
        compose.setContent {
            EgyptianLegalDeadlinesTheme {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    CalculationResultCard(result = result, isSaved = false)
                }
            }
        }

        compose.onNodeWithText(warning).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(notes).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("حفظ تاريخ استرشادي").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun existingRuleDraftAndTargetSurviveSavedStateRestoration() {
        val original = LegalRule(
            id = 7, actionName = "إجراء تجريبي", duration = 15,
            unit = DurationUnit.DAYS, lawName = "قانون تجريبي", revision = 2
        )
        var savedRule: LegalRule? = null
        var savedDraft: LegalRuleDraft? = null
        val restoration = StateRestorationTester(compose)
        restoration.setContent {
            EgyptianLegalDeadlinesTheme {
                RulesCatalogScreen(
                    state = RuleCatalogUiState(rules = listOf(original), loading = false),
                    onDismissMessage = {},
                    onSaveRule = { rule, draft -> savedRule = rule; savedDraft = draft }
                )
            }
        }
        compose.onNodeWithText("تعديل").performClick()
        compose.onNodeWithTag("rule_editor_action").performScrollTo()
            .performTextReplacement("إجراء بعد التعديل")
        compose.onNodeWithTag("rule_editor_article_text").performScrollTo()
            .performTextReplacement("نص طويل لمادة قانونية لم يتم حفظه بعد")
        compose.onNodeWithTag("rule_editor_duration").performScrollTo()
            .performTextReplacement("30")

        restoration.emulateSavedInstanceStateRestore()

        compose.onNodeWithText("تعديل الميعاد القانوني").assertIsDisplayed()
        compose.onNodeWithTag("rule_editor_action").performScrollTo()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString("إجراء بعد التعديل")))
        compose.onNodeWithTag("rule_editor_article_text").performScrollTo()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString("نص طويل لمادة قانونية لم يتم حفظه بعد")))
        compose.onNodeWithTag("rule_editor_duration").performScrollTo()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString("30")))
        compose.onNodeWithText("حفظ واعتماد").performClick()
        compose.runOnIdle {
            assertEquals(original.id, savedRule?.id)
            assertEquals(original.revision, savedRule?.revision)
            assertEquals("إجراء بعد التعديل", savedDraft?.actionName)
            assertEquals("نص طويل لمادة قانونية لم يتم حفظه بعد", savedDraft?.articleText)
            assertEquals("30", savedDraft?.durationText)
        }
    }

    @Test
    fun newRuleDraftSurvivesRestorationAndCancelStartsFreshDraft() {
        val restoration = StateRestorationTester(compose)
        restoration.setContent {
            EgyptianLegalDeadlinesTheme {
                RulesCatalogScreen(
                    state = RuleCatalogUiState(loading = false),
                    onDismissMessage = {},
                    onSaveRule = { _, _ -> }
                )
            }
        }
        compose.onNodeWithText("إضافة").performClick()
        compose.onNodeWithTag("rule_editor_action").performScrollTo()
            .performTextReplacement("مسودة قاعدة جديدة")

        restoration.emulateSavedInstanceStateRestore()

        compose.onNodeWithText("إضافة ميعاد قانوني").assertIsDisplayed()
        compose.onNodeWithTag("rule_editor_action").performScrollTo()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString("مسودة قاعدة جديدة")))
        compose.onNodeWithText("إلغاء").performClick()
        compose.onNodeWithText("إضافة").performClick()
        compose.onNodeWithTag("rule_editor_action").performScrollTo().assert(SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString("")))
    }
}
