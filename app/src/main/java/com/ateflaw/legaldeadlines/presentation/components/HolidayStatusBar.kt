package com.ateflaw.legaldeadlines.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
    val dayNameArabic = DateFormatterUtils.getArabicDayName(todayDate.dayOfWeek)
    val formattedDate = DateFormatterUtils.formatArabicFullDate(todayDate)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
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
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "اليوم",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "اليوم: $dayNameArabic/ $formattedDate",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Status chip
                val statusBg = if (isTodayHoliday) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    MaterialTheme.colorScheme.tertiaryContainer
                }
                val statusText = if (isTodayHoliday) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onTertiaryContainer
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isTodayHoliday) Icons.Default.EventBusy else Icons.Default.Work,
                            contentDescription = null,
                            tint = statusText,
                            modifier = Modifier.width(16.dp).height(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isTodayHoliday) "عطلة رسمية" else "يوم عمل",
                            style = MaterialTheme.typography.labelLarge,
                            color = statusText
                        )
                    }
                }
            }

            if (isTodayHoliday && !holidayName.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "المناسبة: $holidayName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "قاعدة بيانات العطلات الرسمية المعتمدة: $loadedHolidaysCount عطلة مسجلة",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Preview(showBackground = true, name = "Work Day")
@Composable
fun HolidayStatusBarWorkDayPreview() {
    EgyptianLegalDeadlinesTheme {
        HolidayStatusBar(
            todayDate = LocalDate.of(2025, 5, 12),
            isTodayHoliday = false,
            holidayName = null,
            loadedHolidaysCount = 15
        )
    }
}

@Preview(showBackground = true, name = "Holiday")
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

