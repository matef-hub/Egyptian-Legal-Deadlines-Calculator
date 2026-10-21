package com.ateflaw.legaldeadlines.presentation.components

import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.ateflaw.legaldeadlines.domain.model.DeadlineResult
import com.ateflaw.legaldeadlines.domain.model.DurationUnit
import com.ateflaw.legaldeadlines.presentation.ui.theme.AntiqueBrass
import com.ateflaw.legaldeadlines.presentation.ui.theme.EgyptianLegalDeadlinesTheme
import com.ateflaw.legaldeadlines.utils.DateFormatterUtils
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import com.ateflaw.legaldeadlines.presentation.components.custom.BlueprintResultBox
import com.ateflaw.legaldeadlines.presentation.components.custom.ProceduralTimeline
import com.ateflaw.legaldeadlines.presentation.components.custom.TimelineNode
import com.ateflaw.legaldeadlines.presentation.ui.theme.JudicialNavy
import com.ateflaw.legaldeadlines.presentation.ui.theme.TerracottaSeal
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface

private fun String.toArabicDigits(): String {
    return this.replace("0", "٠").replace("1", "١").replace("2", "٢").replace("3", "٣")
        .replace("4", "٤").replace("5", "٥").replace("6", "٦").replace("7", "٧")
        .replace("8", "٨").replace("9", "٩")
}

@Composable
fun CalculationResultCard(
    modifier: Modifier = Modifier,
    result: DeadlineResult,
    isSaved: Boolean,
    initialReminderDays: Int? = 3,
    onReminderDaysChanged: (Int?) -> Unit = {},
    onSaveClick: (Int?) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val finalDateFormatted = DateFormatterUtils.formatArabicFullDate(result.finalDeadline).toArabicDigits()
    val startDateFormatted = DateFormatterUtils.formatArabicFullDate(result.startDate).toArabicDigits()
    val dayNameArabic = DateFormatterUtils.getArabicDayName(result.finalDeadline.dayOfWeek)
    val context = LocalContext.current
    var proactiveReminderDays by remember(initialReminderDays) { mutableIntStateOf(initialReminderDays ?: 0) }

    // 1. Add FINAL DEADLINE to Calendar (Target Date = finalDeadline)
    val onAddFinalDeadlineToCalendar = {
        val title = "⚖️ ميعاد قانوني ملزم: ${result.actionSummary.ifBlank { result.lawArticle }}"
        val description = "الميعاد النهائي واجب الالتزام: $dayNameArabic، $finalDateFormatted\n" +
            "السند التشريعي: ${result.lawArticle}\n\n" +
            result.explanation

        try {
            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, title)
                putExtra(CalendarContract.Events.DESCRIPTION, description)
                putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true)
                val epochMillis = result.finalDeadline.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, epochMillis)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, epochMillis + 86400000L)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            val cleanStart = result.finalDeadline.toString().replace("-", "")
            val nextDay = result.finalDeadline.plusDays(1).toString().replace("-", "")
            val webUrl = "https://calendar.google.com/calendar/render?action=TEMPLATE&text=" +
                Uri.encode(title) +
                "&dates=$cleanStart/$nextDay&details=" +
                Uri.encode(description)
            context.startActivity(Intent(Intent.ACTION_VIEW, webUrl.toUri()))
        }
    }

    // 2. Add PROACTIVE ALERT to Calendar (Target Date = finalDeadline - proactiveReminderDays)
    val onAddProactiveAlertToCalendar = {
        val proactiveDays = proactiveReminderDays.coerceAtLeast(1)
        val proactiveDate = result.finalDeadline.minusDays(proactiveDays.toLong())
        val proactiveFormatted = DateFormatterUtils.formatArabicFullDate(proactiveDate)
        val proactiveDayName = DateFormatterUtils.getArabicDayName(proactiveDate.dayOfWeek)

        val title = "🔔 تنبيه استباقي: ميعاد ${result.actionSummary.ifBlank { result.lawArticle }} (متبقي $proactiveDays أيام)"
        val description = "تنبيه إجرائي استباقي قبل فوات الميعاد بـ $proactiveDays أيام.\n" +
            "تاريخ التنبيه: $proactiveDayName، $proactiveFormatted.\n" +
            "الميعاد النهائي الملزم: $dayNameArabic، $finalDateFormatted.\n" +
            "السند: ${result.lawArticle}\n\n" +
            "الإجراء المطلوب: تجهيز صحيفة الطعن أو الدعوى ومستنداتها وإيداعها بقلم الكتاب مبكراً.\n\n" +
            result.explanation

        try {
            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, title)
                putExtra(CalendarContract.Events.DESCRIPTION, description)
                putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true)
                // Use ACTUAL PROACTIVE DATE epoch!
                val epochMillis = proactiveDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, epochMillis)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, epochMillis + 86400000L)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            val cleanStart = proactiveDate.toString().replace("-", "")
            val nextDay = proactiveDate.plusDays(1).toString().replace("-", "")
            val webUrl = "https://calendar.google.com/calendar/render?action=TEMPLATE&text=" +
                Uri.encode(title) +
                "&dates=$cleanStart/$nextDay&details=" +
                Uri.encode(description)
            context.startActivity(Intent(Intent.ACTION_VIEW, webUrl.toUri()))
        }
    }

    BlueprintResultBox(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = AntiqueBrass
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "آخر يوم قانوني",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = JudicialNavy
                )
            }

            // Big Highlighted Final Date Display
            Text(
                text = finalDateFormatted,
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp),
                fontWeight = FontWeight.Bold,
                color = JudicialNavy
            )

            val remainingDays = ChronoUnit.DAYS.between(LocalDate.now(), result.finalDeadline)
            val remainingText = if (remainingDays > 0) "باقي ${remainingDays.toString().toArabicDigits()} يوماً" 
                                else if (remainingDays == 0L) "اليوم هو آخر ميعاد" 
                                else "انقضى الميعاد منذ ${(-remainingDays).toString().toArabicDigits()} يوماً"
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = TerracottaSeal
            ) {
                Text(
                    text = remainingText,
                    color = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Straighten, // Using available icon
                    contentDescription = null,
                    tint = JudicialNavy,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "المسار الزمني للإجراء",
                    style = MaterialTheme.typography.labelMedium,
                    color = JudicialNavy
                )
            }

            val announcementDate = result.milestones.find { it.type == com.ateflaw.legaldeadlines.domain.model.MilestoneType.TRIGGER_EVENT }?.date ?: result.startDate.minusDays(1)
            
            ProceduralTimeline(
                nodes = listOf(
                    TimelineNode(
                        number = "1",
                        title = "الإعلان",
                        date = "${announcementDate.dayOfMonth} ${DateFormatterUtils.getArabicMonthName(announcementDate.monthValue)}".toArabicDigits()
                    ),
                    TimelineNode(
                        number = "2",
                        title = "بداية السريان",
                        date = "${result.startDate.dayOfMonth} ${DateFormatterUtils.getArabicMonthName(result.startDate.monthValue)}".toArabicDigits()
                    ),
                    TimelineNode(
                        number = "3",
                        title = "الميعاد النهائي",
                        date = "${result.finalDeadline.dayOfMonth} ${DateFormatterUtils.getArabicMonthName(result.finalDeadline.monthValue)}".toArabicDigits(),
                        isFinal = true
                    )
                )
            )
        }
    }
}

@Composable
private fun ResultDetailRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(showBackground = true, name = "Calculation Result Card")
@Composable
fun CalculationResultCardPreview() {
    val sampleResult = DeadlineResult(
        startDate = LocalDate.of(2026, 10, 2),
        provisionalDate = LocalDate.of(2026, 11, 10),
        duration = 40,
        unit = DurationUnit.DAYS,
        ruleDistanceDays = 0,
        additionalDistanceDays = 0,
        totalDistanceDays = 0,
        excludedDays = emptyList(),
        finalDeadline = LocalDate.of(2026, 11, 10),
        explanation = "شرح تفصيلي للميعاد الإجرائي",
        lawArticle = "المادة 227 من قانون المرافعات المدنية والتجارية",
        notes = "ملاحظات القاعدة",
        actionSummary = "استئناف مدني وتجاري"
    )

    EgyptianLegalDeadlinesTheme {
        CalculationResultCard(
            result = sampleResult,
            isSaved = false,
            onSaveClick = {},
            onBackClick = {}
        )
    }
}
