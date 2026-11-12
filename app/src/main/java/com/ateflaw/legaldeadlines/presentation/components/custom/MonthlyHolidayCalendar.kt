package com.ateflaw.legaldeadlines.presentation.components.custom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ateflaw.legaldeadlines.domain.model.Holiday
import com.ateflaw.legaldeadlines.presentation.ui.theme.*
import com.ateflaw.legaldeadlines.utils.DateFormatterUtils
import com.ateflaw.legaldeadlines.utils.DateFormatterUtils.toArabicDigits
import java.time.YearMonth

/** Data provenance is visually explicit: verified dates green, unverified candidates brass, Friday red. */
@Composable
fun MonthlyHolidayCalendar(
    currentMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    holidays: List<Holiday>,
    modifier: Modifier = Modifier
) {
    val monthName = DateFormatterUtils.getArabicMonthName(currentMonth.monthValue)
    val yearString = currentMonth.year.toString().toArabicDigits()
    
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "السابق", tint = MaterialTheme.colorScheme.onSurface)
            }
            Text("$monthName $yearString", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            IconButton(onClick = onNextMonth) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "التالي", tint = MaterialTheme.colorScheme.onSurface)
            }
        }
        
        val daysOfWeek = listOf("السبت", "الأحد", "الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة")
        Row(modifier = Modifier.fillMaxWidth()) {
            daysOfWeek.forEach { dayName ->
                Text(
                    text = dayName,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = if (dayName == "الجمعة") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        val firstDayOfMonth = currentMonth.atDay(1).dayOfWeek
        val firstDayOffset = (firstDayOfMonth.value + 1) % 7
        val daysInMonth = currentMonth.lengthOfMonth()
        val rows = (daysInMonth + firstDayOffset + 6) / 7
        
        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                for (col in 0 until 7) {
                    val dayNum = row * 7 + col - firstDayOffset + 1
                    if (dayNum in 1..daysInMonth) {
                        val date = currentMonth.atDay(dayNum)
                        val isFriday = col == 6
                        val verifiedHoliday = holidays.any { it.holidayDate == date && it.verificationStatus == "VERIFIED_OFFICIAL" }
                        val candidateHoliday = holidays.any { it.holidayDate == date && it.verificationStatus != "VERIFIED_OFFICIAL" }
                        val textColor = when {
                            isFriday -> MaterialTheme.colorScheme.error
                            verifiedHoliday -> MaterialTheme.colorScheme.tertiary
                            candidateHoliday -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                        val bgColor = when {
                            verifiedHoliday -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.14f)
                            candidateHoliday -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.13f)
                            else -> Color.Transparent
                        }
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(bgColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayNum.toString().toArabicDigits(),
                                color = textColor,
                                fontWeight = if (verifiedHoliday || candidateHoliday || isFriday) FontWeight.Bold else FontWeight.Normal,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text("● عطلة موثقة", color = MaterialTheme.colorScheme.tertiary,
                style = MaterialTheme.typography.labelSmall)
            Text("● مرشحة للمراجعة", color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.labelSmall)
            Text("● الجمعة", color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall)
        }
    }
}

