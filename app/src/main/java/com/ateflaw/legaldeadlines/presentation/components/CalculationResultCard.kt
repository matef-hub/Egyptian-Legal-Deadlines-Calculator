package com.ateflaw.legaldeadlines.presentation.components

import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.ateflaw.legaldeadlines.domain.model.DeadlineResult
import com.ateflaw.legaldeadlines.domain.model.DurationUnit
import com.ateflaw.legaldeadlines.presentation.ui.theme.EgyptianLegalDeadlinesTheme
import com.ateflaw.legaldeadlines.utils.DateFormatterUtils
import com.ateflaw.legaldeadlines.utils.DateFormatterUtils.toArabicDigits
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
    modifier: Modifier = Modifier,
    dateFocusModifier: Modifier = Modifier
) {
    val finalDateFormatted = DateFormatterUtils.formatArabicFullDate(result.finalDeadline)
    val startDateFormatted = DateFormatterUtils.formatArabicFullDate(result.startDate)
    val dayNameArabic = DateFormatterUtils.getArabicDayName(result.finalDeadline.dayOfWeek)
    val context = LocalContext.current
    var proactiveReminderDays by remember(initialReminderDays) { mutableIntStateOf(initialReminderDays ?: 0) }
    var isTimelineExpanded by rememberSaveable(result.finalDeadline.toString()) { mutableStateOf(false) }

    // 1. Add FINAL DEADLINE to Calendar (Target Date = finalDeadline)
    val onAddFinalDeadlineToCalendar = {
        val title = "⚠️ تاريخ قانوني للمراجعة: ${result.actionSummary.ifBlank { result.lawArticle }}".toArabicDigits()
        val description = "تاريخ محسوب استرشادياً ويجب مراجعة العطلات والسند القانوني: $dayNameArabic، $finalDateFormatted\n" +
            "السند التشريعي: ${result.lawArticle.toArabicDigits()}\n\n" +
            result.explanation

        try {
            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, title)
                putExtra(CalendarContract.Events.DESCRIPTION, description)
                putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true)
                val epochMillis = result.finalDeadline.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, epochMillis)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, result.finalDeadline.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
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

        val title = "🔔 تنبيه استباقي: ميعاد ${result.actionSummary.ifBlank { result.lawArticle }} (متبقي ${proactiveDays.toString().toArabicDigits()} أيام)".toArabicDigits()
        val description = "تنبيه إجرائي استباقي قبل فوات الميعاد بـ ${proactiveDays.toString().toArabicDigits()} أيام.\n" +
            "تاريخ التنبيه: $proactiveDayName، $proactiveFormatted.\n" +
            "التاريخ المحسوب للمراجعة: $dayNameArabic، $finalDateFormatted.\n" +
            "السند: ${result.lawArticle.toArabicDigits()}\n\n" +
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
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, proactiveDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
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

    // A single layout grid: no oversized green parent card wrapped around
    // smaller cards. Every section now shares the same horizontal edges.
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
            // Header Row: Check Icon + Title "الميعاد النهائي"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (result.isProvisional) "تاريخ حسابي استرشادي" else "الميعاد النهائي",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )

                Icon(
                    imageVector = if (result.isProvisional) Icons.Default.Info else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.size(22.dp)
                )
            }

            // A compact, baseline-consistent primary date display.
            Surface(
                modifier = dateFocusModifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = finalDateFormatted,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontSize = 22.sp,
                                lineHeight = 30.sp
                            ),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )
                    }
                    Text(
                        text = dayNameArabic,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f)
                    )
                }
            }

            // Keep the deadline prominent; the audit timeline is available on demand.
            if (result.milestones.isNotEmpty()) {
                OutlinedButton(
                    onClick = { isTimelineExpanded = !isTimelineExpanded },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.HourglassTop,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isTimelineExpanded) "إخفاء مسار الحساب" else "عرض مسار الحساب (${result.milestones.size.toString().toArabicDigits()} محطات)",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                if (isTimelineExpanded) {
                    DeadlineTimelineView(
                        milestones = result.milestones,
                        finalDeadline = result.finalDeadline
                    )
                }
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
                        value = "${result.duration.toString().toArabicDigits()} ${durationUnitLabel(result.duration, result.unit)}"
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )

                    // Row 2: Legal Action / Statutory Basis
                    ResultDetailRow(
                        icon = Icons.Default.Gavel,
                        label = "الإجراء وسنده",
                        value = result.actionSummary.ifBlank { result.lawArticle.ifBlank { "صحيفة دعوى" } }.toArabicDigits()
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
                        value = "${result.totalDistanceDays}".toArabicDigits() + " يوم"
                    )
                }
            }

            // Compact reminder settings. Visual chips are 36dp, but touch
            // targets retain at least 48dp for accessibility.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "التنبيه الاستباقي للتقويم",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (proactiveReminderDays > 0) {
                    val proactiveDate = result.finalDeadline.minusDays(proactiveReminderDays.toLong())
                    Text(
                        text = "بتاريخ ${DateFormatterUtils.formatArabicFullDate(proactiveDate)} (قبل ${proactiveReminderDays.toString().toArabicDigits()} أيام)",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(1 to "١ يوم", 3 to "٣ أيام", 5 to "٥ أيام", 7 to "أسبوع", 0 to "بدون")
                        .forEach { (days, label) ->
                            val selected = proactiveReminderDays == days
                            val pillBgColor by animateColorAsState(
                                targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                label = "ReminderPillBackground"
                            )
                            val pillTextColor = if (selected)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant
                            Box(
                                modifier = Modifier
                                    .widthIn(min = 52.dp)
                                    .heightIn(min = 48.dp)
                                    .clickable {
                                        proactiveReminderDays = days
                                        onReminderDaysChanged(if (days == 0) null else days)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    modifier = Modifier.height(36.dp),
                                    shape = RoundedCornerShape(9.dp),
                                    color = pillBgColor
                                ) {
                                    Box(
                                        modifier = Modifier.padding(horizontal = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = pillTextColor,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                }
            }

            // One clear primary action; calendar operations remain available
            // separately so the user never silently creates two events.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        onSaveClick(if (proactiveReminderDays == 0) null else proactiveReminderDays)
                    },
                    enabled = !isSaved,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
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
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSaved) "تم الحفظ بالمحفوظات"
                            else if (result.isProvisional) "حفظ تاريخ استرشادي"
                            else "حفظ الميعاد",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onAddFinalDeadlineToCalendar,
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "إضافة للتقويم",
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (proactiveReminderDays > 0) {
                        OutlinedButton(
                            onClick = onAddProactiveAlertToCalendar,
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "تنبيه استباقي",
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                TextButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .heightIn(min = 48.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "احتساب جديد",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
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
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Row(
            modifier = Modifier.weight(0.43f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            modifier = Modifier.weight(0.57f),
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End
        )
    }
}

/** Arabic day labels for durations; numeric representation is handled separately. */
private fun durationUnitLabel(value: Long, unit: DurationUnit): String {
    if (unit != DurationUnit.DAYS) return unit.arabicDisplayName
    return when (value) {
        0L, 1L -> "يوم"
        2L -> "يومان"
        in 3L..10L -> "أيام"
        else -> "يومًا"
    }
}

@Preview(showBackground = true, name = "Calculation Result Card")
@Composable
fun CalculationResultCardPreview() {
    val sampleMilestones = listOf(
        com.ateflaw.legaldeadlines.domain.model.CalculationMilestone(
            type = com.ateflaw.legaldeadlines.domain.model.MilestoneType.TRIGGER_EVENT,
            date = LocalDate.of(2026, 9, 23),
            title = "تاريخ الإعلان أو صدور الحكم",
            description = "تاريخ صدور الحكم أو الإعلان به وفقاً للمادة 82 من قانون المرافعات.",
            legalBasis = "المادة 82 من قانون المرافعات المدنية والتجارية"
        ),
        com.ateflaw.legaldeadlines.domain.model.CalculationMilestone(
            type = com.ateflaw.legaldeadlines.domain.model.MilestoneType.COUNTING_START,
            date = LocalDate.of(2026, 9, 24),
            title = "بدء سريان الميعاد الإجرائي",
            description = "يبدأ الاحتساب من اليوم التالي إعمالاً للمادة 15 مرافعات.",
            legalBasis = "المادة 15 من قانون المرافعات المدنية والتجارية"
        ),
        com.ateflaw.legaldeadlines.domain.model.CalculationMilestone(
            type = com.ateflaw.legaldeadlines.domain.model.MilestoneType.BASE_DEADLINE,
            date = LocalDate.of(2026, 11, 22),
            title = "نهاية المدة القانونية الأصلية",
            description = "انقضاء مدة الميعاد الأصلية.",
            legalBasis = "المادة 82 من قانون المرافعات المدنية والتجارية"
        ),
        com.ateflaw.legaldeadlines.domain.model.CalculationMilestone(
            type = com.ateflaw.legaldeadlines.domain.model.MilestoneType.FINAL_DEADLINE,
            date = LocalDate.of(2026, 11, 22),
            title = "الميعاد النهائي واجب الالتزام به",
            description = "آخر يوم عمل لإيداع الصحيفة.",
            legalBasis = "المادتان 15 و 18 من قانون المرافعات"
        )
    )

    val sampleResult = DeadlineResult(
        startDate = LocalDate.of(2026, 10, 2),
        provisionalDate = LocalDate.of(2026, 11, 10),
        duration = 40,
        unit = DurationUnit.DAYS,
        ruleDistanceDays = 0,
        additionalDistanceDays = 0,
        totalDistanceDays = 0,
        excludedDays = emptyList(),
        milestones = sampleMilestones,
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
