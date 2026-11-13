package com.ateflaw.legaldeadlines.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ateflaw.legaldeadlines.domain.calculator.CourtDayClassifier
import com.ateflaw.legaldeadlines.domain.calculator.CourtDayType
import com.ateflaw.legaldeadlines.presentation.ui.theme.EgyptianLegalDeadlinesTheme
import com.ateflaw.legaldeadlines.utils.DateFormatterUtils
import java.time.LocalDate

@Composable
fun HolidayStatusBar(
    todayDate: LocalDate,
    isTodayHoliday: Boolean,
    holidayName: String?,
    loadedHolidaysCount: Int,
    modifier: Modifier = Modifier
) {
    val dayType = CourtDayClassifier.classify(todayDate, isTodayHoliday)
    val dateText = DateFormatterUtils.formatArabicDateWithDay(todayDate)
    val isWorkingDay = dayType == CourtDayType.WORKING_DAY
    val statusLabel = when (dayType) {
        CourtDayType.WEEKLY_REST -> "عطلة أسبوعية"
        CourtDayType.OFFICIAL_HOLIDAY -> "عطلة رسمية"
        CourtDayType.WORKING_DAY -> "يوم عمل"
    }
    val statusBackground = if (isWorkingDay) {
        MaterialTheme.colorScheme.tertiaryContainer
    } else {
        MaterialTheme.colorScheme.errorContainer
    }
    val statusForeground = if (isWorkingDay) {
        MaterialTheme.colorScheme.onTertiaryContainer
    } else {
        MaterialTheme.colorScheme.onErrorContainer
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = dateText,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusBackground)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isWorkingDay) Icons.Default.Work else Icons.Default.EventBusy,
                        contentDescription = null,
                        tint = statusForeground,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = statusForeground
                    )
                }
            }

            if (isTodayHoliday && !holidayName.isNullOrBlank()) {
                Text(
                    text = "المناسبة: $holidayName",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "العطلات المعتمدة: $loadedHolidaysCount عطلة مسجلة",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true, name = "Friday - Weekly Rest")
@Composable
fun HolidayStatusBarFridayPreview() {
    EgyptianLegalDeadlinesTheme {
        HolidayStatusBar(
            todayDate = LocalDate.of(2026, 11, 13),
            isTodayHoliday = false,
            holidayName = null,
            loadedHolidaysCount = 21
        )
    }
}

@Preview(showBackground = true, name = "Work Day")
@Composable
fun HolidayStatusBarWorkDayPreview() {
    EgyptianLegalDeadlinesTheme {
        HolidayStatusBar(
            todayDate = LocalDate.of(2026, 11, 12),
            isTodayHoliday = false,
            holidayName = null,
            loadedHolidaysCount = 21
        )
    }
}

@Preview(showBackground = true, name = "Official Holiday")
@Composable
fun HolidayStatusBarHolidayPreview() {
    EgyptianLegalDeadlinesTheme {
        HolidayStatusBar(
            todayDate = LocalDate.of(2025, 1, 7),
            isTodayHoliday = true,
            holidayName = "عيد الميلاد المجيد",
            loadedHolidaysCount = 15
        )
    }
}
