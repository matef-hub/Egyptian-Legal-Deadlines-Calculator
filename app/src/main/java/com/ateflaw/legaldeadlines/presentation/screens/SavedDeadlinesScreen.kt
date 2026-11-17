package com.ateflaw.legaldeadlines.presentation.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ateflaw.legaldeadlines.domain.model.DurationUnit
import com.ateflaw.legaldeadlines.domain.model.SavedDeadline
import com.ateflaw.legaldeadlines.presentation.components.ConfirmDeleteDialog
import com.ateflaw.legaldeadlines.presentation.components.SweetAlertNotice
import com.ateflaw.legaldeadlines.presentation.components.NoticeKind
import com.ateflaw.legaldeadlines.presentation.components.SavedDeadlineItem
import com.ateflaw.legaldeadlines.presentation.components.custom.DiamondDivider
import com.ateflaw.legaldeadlines.presentation.ui.theme.EgyptianLegalDeadlinesTheme
import com.ateflaw.legaldeadlines.presentation.viewmodel.DeadlineListUiState
import com.ateflaw.legaldeadlines.presentation.viewmodel.DeadlineListViewModel
import com.ateflaw.legaldeadlines.utils.DateFormatterUtils.toArabicDigits
import com.ateflaw.legaldeadlines.utils.normalizedArabicSearchText
import java.time.LocalDate

private enum class DeadlineDateFilter(val label: String) {
    ALL("الكل"),
    TODAY("اليوم"),
    SOON("خلال ٣ أيام"),
    EXPIRED("المنتهية");

    fun includes(date: LocalDate, today: LocalDate): Boolean = when (this) {
        ALL -> true
        TODAY -> date == today
        SOON -> date > today && date <= today.plusDays(3)
        EXPIRED -> date < today
    }
}

@Composable
fun SavedDeadlinesScreen(
    viewModel: DeadlineListViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SavedDeadlinesScreenContent(
        uiState = uiState,
        onDeleteClick = { viewModel.requestDeleteConfirmation(it) },
        onConfirmDelete = { viewModel.confirmDelete() },
        onDismissDeleteConfirmation = { viewModel.dismissDeleteConfirmation() },
        modifier = modifier
    )
    SweetAlertNotice(
        message = uiState.errorMessage,
        kind = NoticeKind.ERROR,
        onDismiss = viewModel::dismissErrorMessage
    )
}

@Composable
fun SavedDeadlinesScreenContent(
    uiState: DeadlineListUiState,
    onDeleteClick: (SavedDeadline) -> Unit,
    onConfirmDelete: () -> Unit,
    onDismissDeleteConfirmation: () -> Unit,
    modifier: Modifier = Modifier
) {
    var query by rememberSaveable { mutableStateOf("") }
    var dateFilter by rememberSaveable { mutableStateOf(DeadlineDateFilter.ALL) }
    val today = LocalDate.now()
    val filtered = remember(uiState.deadlines, query, dateFilter, today) {
        val term = query.normalizedArabicSearchText()
        uiState.deadlines.filter { deadline ->
            dateFilter.includes(deadline.finalDeadline, today) &&
                (term.isBlank() || listOf(deadline.caseNumber, deadline.clientName, deadline.actionName)
                    .any { it.normalizedArabicSearchText().contains(term) })
        }
    }
    Box(modifier = modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = uiState.isLoading to uiState.deadlines.isEmpty(),
            transitionSpec = {
                fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing))
                    .togetherWith(fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing)))
            },
            label = "SavedDeadlinesContentState"
        ) { (isLoading, isEmpty) ->
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                isEmpty -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkBorder,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "لا توجد مواعيد محفوظة حالياً",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "يمكنك احتساب أي ميعاد قانوني من شاشة الحاسبة والضغط على زر 'حفظ' لإضافته إلى هنا.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text(
                                text = "المواعيد المسجلة (${uiState.deadlines.size.toString().toArabicDigits()})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            DiamondDivider()
                            OutlinedTextField(
                                value = query,
                                onValueChange = { query = it },
                                label = { Text("بحث برقم الدعوى أو الموكل") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                DeadlineDateFilter.entries.forEach { filter ->
                                    item(key = filter.name) {
                                        FilterChip(
                                            selected = dateFilter == filter,
                                            onClick = { dateFilter = filter },
                                            label = { Text(filter.label) }
                                        )
                                    }
                                }
                            }
                        }

                        if (filtered.isEmpty()) {
                            item { Text("لا توجد مواعيد مطابقة.", style = MaterialTheme.typography.bodyMedium) }
                        }
                        items(filtered, key = { it.id }) { savedDeadline ->
                            SavedDeadlineItem(
                                deadline = savedDeadline,
                                onDeleteClick = { onDeleteClick(savedDeadline) },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }
        }

        // Delete confirmation dialog
        if (uiState.deleteConfirmationDeadline != null) {
            ConfirmDeleteDialog(
                deadlineToDelete = uiState.deleteConfirmationDeadline,
                onConfirm = onConfirmDelete,
                onDismiss = onDismissDeleteConfirmation
            )
        }
    }
}

@Preview(showBackground = true, name = "Saved Deadlines Screen")
@Composable
fun SavedDeadlinesScreenPreview() {
    val sampleDeadlines = listOf(
        SavedDeadline(
            id = 1L,
            caseNumber = "1234 لسنة 2024",
            clientName = "أحمد محمد علي",
            actionName = "استئناف حكم مدني",
            announcementDate = LocalDate.of(2024, 5, 1),
            duration = 40,
            unit = DurationUnit.DAYS,
            distanceDays = 0,
            finalDeadline = LocalDate.of(2024, 6, 10),
            lawArticle = "المادة 227 مرافعات",
            calculationExplanation = "بدأ الميعاد من اليوم التالي للإعلان (2 مايو) وينتهي في 10 يونيو بعد مراعاة العطلات الرسمية."
        ),
        SavedDeadline(
            id = 2L,
            caseNumber = "5678 لسنة 2024",
            clientName = "محمود حسن",
            actionName = "طعن بالنقض جنائي",
            announcementDate = LocalDate.of(2024, 4, 15),
            duration = 60,
            unit = DurationUnit.DAYS,
            distanceDays = 0,
            finalDeadline = LocalDate.of(2024, 6, 14),
            lawArticle = "المادة 34 من قانون حالات وإجراءات الطعن أمام محكمة النقض",
            calculationExplanation = "بدأ الميعاد من اليوم التالي للصدور (16 أبريل) وينتهي في 14 يونيو."
        )
    )

    EgyptianLegalDeadlinesTheme {
        SavedDeadlinesScreenContent(
            uiState = DeadlineListUiState(
                deadlines = sampleDeadlines,
                isLoading = false
            ),
            onDeleteClick = {},
            onConfirmDelete = {},
            onDismissDeleteConfirmation = {}
        )
    }
}

@Preview(showBackground = true, name = "Saved Deadlines Screen - Empty")
@Composable
fun SavedDeadlinesScreenEmptyPreview() {
    EgyptianLegalDeadlinesTheme {
        SavedDeadlinesScreenContent(
            uiState = DeadlineListUiState(
                deadlines = emptyList(),
                isLoading = false
            ),
            onDeleteClick = {},
            onConfirmDelete = {},
            onDismissDeleteConfirmation = {}
        )
    }
}
