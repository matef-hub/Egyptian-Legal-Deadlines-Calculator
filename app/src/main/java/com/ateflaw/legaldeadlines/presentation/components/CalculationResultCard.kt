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
import com.ateflaw.legaldeadlines.presentation.ui.theme.EgyptianLegalDeadlinesTheme
import com.ateflaw.legaldeadlines.utils.DateFormatterUtils
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun CalculationResultCard(
    result: DeadlineResult,
    isSaved: Boolean,
    initialReminderDays: Int? = 3,
    onReminderDaysChanged: (Int?) -> Unit = {},
    onSaveClick: (Int?) -> Unit = {},
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val finalDateFormatted = DateFormatterUtils.formatArabicFullDate(result.finalDeadline)
    val startDateFormatted = DateFormatterUtils.formatArabicFullDate(result.startDate)
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

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.3f))
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row: Check Icon + Title "الميعاد النهائي"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "الميعاد النهائي",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )

                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Big Highlighted Final Date Display
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = finalDateFormatted,
                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 28.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = dayNameArabic,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f)
                )
            }

            // Interactive Procedural Lifecycle Timeline
            if (result.milestones.isNotEmpty()) {
                DeadlineTimelineView(
                    milestones = result.milestones,
                    finalDeadline = result.finalDeadline
                )
            }

            // Structured Table / Rows Inside Surface Card Container
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    // Row 1: Duration
                    ResultDetailRow(
                        icon = Icons.Default.HourglassTop,
                        label = "مدة الميعاد",
                        value = "${result.duration} ${if (result.duration == 13L || result.duration == 15L) "يوماً" else result.unit.arabicDisplayName}"
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )

                    // Row 2: Legal Action / Statutory Basis
                    ResultDetailRow(
                        icon = Icons.Default.Gavel,
                        label = "الإجراء وسنده",
                        value = result.actionSummary.ifBlank { result.lawArticle.ifBlank { "صحيفة دعوى" } }
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )

                    // Row 3: Start Date
                    ResultDetailRow(
                        icon = Icons.Default.CalendarMonth,
                        label = "تاريخ السريان",
                        value = startDateFormatted
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )

                    // Row 4: Distance Days (Articles 16 and 17)
                    ResultDetailRow(
                        icon = Icons.Default.Straighten,
                        label = "أيام المسافة",
                        value = "${result.totalDistanceDays} يوم"
                    )
                }
            }

            // Professional Legal Disclaimer & Note
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(top = 2.dp)
                    )

                    Text(
                        text = "تنبيه مهني: الحساب استرشادي وفقاً للمواد 15 و16 و17 و18 من قانون المرافعات وقرارات العطلات الرسمية، ويلزم التحقق من وقائع الخصومة وحضورها واستثناءاتها.",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        lineHeight = 18.sp
                    )
                }
            }

            // Prominent Statutory Review Status Warning (if rule is under review)
            if (result.warnings.isNotEmpty()) {
                result.warnings.forEach { warning ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = warning,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // Proactive Alert Configuration Selector
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "التنبيه الاستباقي للتقويم:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (proactiveReminderDays > 0) {
                        val proactiveDate = result.finalDeadline.minusDays(proactiveReminderDays.toLong())
                        Text(
                            text = "بتاريخ ${DateFormatterUtils.formatArabicFullDate(proactiveDate)} (قبل $proactiveReminderDays أيام)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(1 to "يوم", 3 to "3 أيام", 5 to "5 أيام", 7 to "أسبوع", 0 to "بدون").forEach { (days, label) ->
                        val isSelected = proactiveReminderDays == days
                        val pillBgColor by animateColorAsState(
                            targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                            label = "ProactivePillBg"
                        )
                        val pillTextColor by animateColorAsState(
                            targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                            label = "ProactivePillText"
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(pillBgColor)
                                .clickable {
                                    proactiveReminderDays = days
                                    onReminderDaysChanged(if (days == 0) null else days)
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = pillTextColor,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Separated Calendar Actions:
            // 1. Add Final Deadline (at finalDeadline)
            // 2. Add Proactive Reminder (at proactiveDate)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAddFinalDeadlineToCalendar,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "إضافة الميعاد النهائي للتقويم (${finalDateFormatted})",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (proactiveReminderDays > 0) {
                    val pDate = result.finalDeadline.minusDays(proactiveReminderDays.toLong())
                    OutlinedButton(
                        onClick = onAddProactiveAlertToCalendar,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "إضافة تنبيه استباقي للتقويم (${DateFormatterUtils.formatArabicFullDate(pDate)})",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { onSaveClick(if (proactiveReminderDays == 0) null else proactiveReminderDays) },
                        enabled = !isSaved,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSaved) "تم الحفظ بالمحفوظات" else "حفظ الميعاد",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "احتساب جديد",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
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
