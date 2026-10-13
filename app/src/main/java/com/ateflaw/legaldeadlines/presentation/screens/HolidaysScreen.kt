package com.ateflaw.legaldeadlines.presentation.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ateflaw.legaldeadlines.R
import com.ateflaw.legaldeadlines.domain.model.Holiday
import com.ateflaw.legaldeadlines.presentation.ui.theme.EgyptianLegalDeadlinesTheme
import com.ateflaw.legaldeadlines.presentation.viewmodel.HolidaysUiState
import com.ateflaw.legaldeadlines.presentation.viewmodel.HolidaysViewModel
import com.ateflaw.legaldeadlines.utils.DateFormatterUtils
import kotlinx.coroutines.delay
import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun HolidaysScreen(
    viewModel: HolidaysViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    HolidaysScreenContent(
        uiState = uiState,
        onRefreshClick = { viewModel.syncHolidays() },
        onDismissSnackbar = { viewModel.dismissSyncMessage() },
        modifier = modifier
    )
}

@Composable
fun HolidaysScreenContent(
    uiState: HolidaysUiState,
    onRefreshClick: () -> Unit,
    onDismissSnackbar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.syncMessage) {
        val msg = uiState.syncMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            onDismissSnackbar()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = Pair(uiState.isLoading, uiState.holidays.isEmpty()),
            transitionSpec = {
                fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing))
                    .togetherWith(fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing)))
            },
            label = "HolidaysContentState"
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
                            imageVector = Icons.Default.EventAvailable,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.no_holidays_found),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        FilledTonalButton(
                            onClick = onRefreshClick,
                            enabled = !uiState.isSyncing
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = stringResource(R.string.holidays_sync_btn))
                        }
                    }
                }

                else -> {
                    val today = LocalDate.now()
                    var selectedFilter by remember { mutableStateOf("ALL") }

                    val filteredHolidays = remember(uiState.holidays, selectedFilter) {
                        when (selectedFilter) {
                            "UPCOMING" -> uiState.holidays.filter { it.holidayDate.isAfter(today) || it.holidayDate == today }
                            "PAST" -> uiState.holidays.filter { it.holidayDate.isBefore(today) }
                            "THURSDAY" -> uiState.holidays.filter { it.holidayDate.dayOfWeek == DayOfWeek.THURSDAY }
                            else -> uiState.holidays
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            // Cabinet / Governance Policy Banner
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalance,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .padding(top = 2.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "تنويه بشأن قرارات مجلس الوزراء",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = stringResource(R.string.holidays_governance_note),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "العطلات الرسمية (${filteredHolidays.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                FilledTonalButton(
                                    onClick = onRefreshClick,
                                    enabled = !uiState.isSyncing
                                ) {
                                    if (uiState.isSyncing) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (uiState.isSyncing) stringResource(R.string.holidays_syncing) else stringResource(R.string.holidays_sync_btn),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }

                        // Filter Segment Tabs
                        item {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf(
                                        "ALL" to "الكل",
                                        "UPCOMING" to "القادمة",
                                        "PAST" to "المنقضية",
                                        "THURSDAY" to "المرحّلة (الخميس)"
                                    ).forEach { (key, label) ->
                                        val isSelected = selectedFilter == key
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent
                                                )
                                                .clickable { selectedFilter = key }
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        itemsIndexed(
                            items = filteredHolidays,
                            key = { _, holiday -> "${holiday.holidayDate}_${holiday.id}" }
                        ) { index, holiday ->

                            var itemVisible by remember { mutableStateOf(false) }
                            LaunchedEffect(holiday.holidayDate) {
                                delay(((index * 30).coerceAtMost(180)).milliseconds)
                                itemVisible = true
                            }
                            val itemAlpha by animateFloatAsState(
                                targetValue = if (itemVisible) 1f else 0f,
                                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                                label = "HolidayItemAlpha"
                            )
                            val itemTranslateY by animateFloatAsState(
                                targetValue = if (itemVisible) 0f else 16f,
                                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                                label = "HolidayItemTranslateY"
                            )

                            Box(
                                modifier = Modifier.graphicsLayer {
                                    alpha = itemAlpha
                                    translationY = itemTranslateY
                                }
                            ) {
                                HolidayCard(holiday = holiday)
                            }
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

@Composable
private fun HolidayCard(
    holiday: Holiday,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val isToday = holiday.holidayDate == today
    val isFuture = holiday.holidayDate.isAfter(today)
    val isThursday = holiday.holidayDate.dayOfWeek == DayOfWeek.THURSDAY

    val dayName = DateFormatterUtils.getArabicDayName(holiday.holidayDate.dayOfWeek)
    val fullFormattedDate = DateFormatterUtils.formatArabicFullDate(holiday.holidayDate)

    val cardBorderColor = if (isToday) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    }

    val cardContainerColor = if (isToday) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardContainerColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, cardBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isToday) 4.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = holiday.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                // Status chip (Today / Future / Past)
                val statusBg = when {
                    isToday -> MaterialTheme.colorScheme.primaryContainer
                    isFuture -> MaterialTheme.colorScheme.secondaryContainer
                    else -> MaterialTheme.colorScheme.surfaceContainerHigh
                }
                val statusTextColor = when {
                    isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                    isFuture -> MaterialTheme.colorScheme.onSecondaryContainer
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                val statusText = when {
                    isToday -> "اليوم"
                    isFuture -> "قادمة"
                    else -> "منقضية"
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusTextColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    imageVector = Icons.Default.EventAvailable,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$dayName، $fullFormattedDate",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isThursday) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.holiday_day_shifted),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Holidays Screen")
@Composable
fun HolidaysScreenPreview() {
    val sampleHolidays = listOf(
        Holiday(id = 1, holidayDate = LocalDate.of(2025, 1, 7), name = "عيد الميلاد المجيد"),
        Holiday(id = 2, holidayDate = LocalDate.of(2025, 1, 25), name = "ثورة 25 يناير وعيد الشرطة"),
        Holiday(id = 3, holidayDate = LocalDate.of(2025, 4, 25), name = "عيد تحرير سيناء"),
        Holiday(id = 4, holidayDate = LocalDate.of(2025, 5, 1), name = "عيد العمال")
    )

    EgyptianLegalDeadlinesTheme {
        HolidaysScreenContent(
            uiState = HolidaysUiState(
                holidays = sampleHolidays,
                isLoading = false
            ),
            onRefreshClick = {},
            onDismissSnackbar = {}
        )
    }
}
