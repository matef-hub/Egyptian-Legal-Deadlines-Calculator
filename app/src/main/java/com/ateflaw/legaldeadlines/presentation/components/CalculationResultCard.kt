package com.ateflaw.legaldeadlines.presentation.components

import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
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
import com.ateflaw.legaldeadlines.presentation.ui.theme.InfoContainerSky
import com.ateflaw.legaldeadlines.presentation.ui.theme.OnInfoContainerSky
import com.ateflaw.legaldeadlines.presentation.ui.theme.OnTertiaryContainerEmerald
import com.ateflaw.legaldeadlines.presentation.ui.theme.TertiaryContainerEmerald
import com.ateflaw.legaldeadlines.utils.DateFormatterUtils
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun CalculationResultCard(
    result: DeadlineResult,
    isSaved: Boolean,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val finalDateFormatted = DateFormatterUtils.formatArabicFullDate(result.finalDeadline)
    val startDateFormatted = DateFormatterUtils.formatArabicFullDate(result.startDate)
    val dayNameArabic = DateFormatterUtils.getArabicDayName(result.finalDeadline.dayOfWeek)
    val context = LocalContext.current
    var proactiveReminderDays by remember { mutableIntStateOf(3) }

    val onAddToCalendar = {
        val proactiveDate = if (proactiveReminderDays > 0) result.finalDeadline.minusDays(proactiveReminderDays.toLong()) else result.finalDeadline
        val proactiveFormatted = DateFormatterUtils.formatArabicFullDate(proactiveDate)

        val descriptionText = if (proactiveReminderDays > 0) {
            "🔔 تنبيه استباقي: قبل الميعاد النهائي بـ $proactiveReminderDays أيام (بتاريخ $proactiveFormatted)\n\n" +
            "السند: ${result.lawArticle}\n\n" +
            result.explanation
        } else {
            "الميعاد النهائي: $dayNameArabic، $finalDateFormatted\nالسند: ${result.lawArticle}\n\n${result.explanation}"
        }

        try {
            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, "ميعاد قانوني: ${result.lawArticle}" + if (proactiveReminderDays > 0) " (تنبيه مسبق $proactiveReminderDays أيام)" else "")
                putExtra(CalendarContract.Events.DESCRIPTION, descriptionText)
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
                Uri.encode("ميعاد قانوني: ${result.lawArticle}" + if (proactiveReminderDays > 0) " (تنبيه $proactiveReminderDays أيام)" else "") +
                "&dates=$cleanStart/$nextDay&details=" +
                Uri.encode(descriptionText)
            context.startActivity(Intent(Intent.ACTION_VIEW, webUrl.toUri()))
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = TertiaryContainerEmerald
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(OnTertiaryContainerEmerald.copy(alpha = 0.3f))
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
                    color = OnTertiaryContainerEmerald
                )

                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = OnTertiaryContainerEmerald,
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
                        tint = OnTertiaryContainerEmerald,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = finalDateFormatted,
                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 28.sp),
                        fontWeight = FontWeight.Bold,
                        color = OnTertiaryContainerEmerald
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = dayNameArabic,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnTertiaryContainerEmerald.copy(alpha = 0.85f)
                )
            }

            // Structured Table / Rows Inside White Card Container
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
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
                        label = "الإجراء القانوني",
                        value = result.actionSummary.ifBlank { result.lawArticle.ifBlank { "صحيفة دعوى" } }
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )

                    // Row 3: Start Date
                    ResultDetailRow(
                        icon = Icons.Default.CalendarMonth,
                        label = "تاريخ البداية",
                        value = startDateFormatted
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )

                    // Row 4: Distance Days
                    ResultDetailRow(
                        icon = Icons.Default.Straighten,
                        label = "المسافة الإضافية",
                        value = "${result.totalDistanceDays} يوم"
                    )
                }
            }

            // Info Banner Notice (Light Sky Blue Card with Info Icon)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(InfoContainerSky)
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = OnInfoContainerSky,
                        modifier = Modifier.size(20.dp)
                    )

                    Text(
                        text = "تم احتساب الميعاد وفقًا للقاعدة القانونية المحدد مع مراعاة الإجازات والعطلات الرسمية",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = OnInfoContainerSky,
                        lineHeight = 18.sp
                    )
                }
            }

            // Proactive Alert Options
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "تنبيه استباقي قبل الميعاد:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (proactiveReminderDays > 0) {
                        Text(
                            text = "قبل $proactiveReminderDays أيام",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(1 to "يوم", 3 to "3 أيام", 5 to "5 أيام", 7 to "أسبوع", 0 to "بدون").forEach { (days, label) ->
                        val isSelected = proactiveReminderDays == days
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .clickable { proactiveReminderDays = days }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                       else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Action Buttons: Add to Calendar, Save, Back
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAddToCalendar,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "إضافة إلى تقويم Google / الجهاز",
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onSaveClick,
                        enabled = !isSaved,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.CheckCircle else Icons.Default.Bookmark,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = if (isSaved) "تم الحفظ" else "حفظ")
                    }

                    OutlinedButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "رجوع")
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
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )

            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
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

@Preview(showBackground = true)
@Composable
fun CalculationResultCardPreview() {
    val fixedDate = LocalDate.of(2026, 9, 12)
    val sampleResult = DeadlineResult(
        startDate = fixedDate,
        provisionalDate = fixedDate.plusDays(13),
        duration = 13,
        unit = DurationUnit.DAYS,
        ruleDistanceDays = 15,
        additionalDistanceDays = 0,
        totalDistanceDays = 15,
        excludedDays = emptyList(),
        finalDeadline = fixedDate.plusDays(13),
        explanation = "تم احتساب الميعاد وفقًا للقاعدة القانونية المحددة مع مراعاة الإجازات والعطلات الرسمية.",
        lawArticle = "المادة 66 مرافعات",
        notes = "",
        actionSummary = "صحيفة دعوى"
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
