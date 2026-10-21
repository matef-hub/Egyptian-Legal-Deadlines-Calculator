package com.ateflaw.legaldeadlines.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ateflaw.legaldeadlines.domain.model.Holiday
import com.ateflaw.legaldeadlines.presentation.components.custom.DiamondDivider
import com.ateflaw.legaldeadlines.presentation.ui.theme.AntiqueBrass
import com.ateflaw.legaldeadlines.presentation.ui.theme.ArefRuqaaFontFamily
import com.ateflaw.legaldeadlines.presentation.ui.theme.EgyptianLegalDeadlinesTheme
import com.ateflaw.legaldeadlines.presentation.ui.theme.JudicialNavy
import com.ateflaw.legaldeadlines.presentation.ui.theme.TerracottaSeal
import com.ateflaw.legaldeadlines.presentation.ui.theme.VerifiedEmerald
import com.ateflaw.legaldeadlines.presentation.ui.theme.VintageBackground
import com.ateflaw.legaldeadlines.presentation.viewmodel.HolidaysUiState
import com.ateflaw.legaldeadlines.presentation.viewmodel.HolidaysViewModel
import com.ateflaw.legaldeadlines.utils.DateFormatterUtils
import java.time.LocalDate
import java.time.YearMonth

fun Int.toEasternArabic(): String {
    val easternArabicNumerals = arrayOf("٠", "١", "٢", "٣", "٤", "٥", "٦", "٧", "٨", "٩")
    return this.toString().map { char ->
        if (char.isDigit()) easternArabicNumerals[char.digitToInt()] else char
    }.joinToString("")
}

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

    Box(modifier = modifier.fillMaxSize().background(VintageBackground)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "تقويم العطلات",
                    fontFamily = ArefRuqaaFontFamily,
                    color = JudicialNavy,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "مواعيد العمل بالمحاكم المصرية",
                    color = AntiqueBrass,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            DiamondDivider()
            Spacer(modifier = Modifier.height(16.dp))
            
            // Grid
            var currentMonth by remember { mutableStateOf(YearMonth.now()) }
            MonthlyCalendarGrid(
                currentMonth = currentMonth,
                onPreviousMonth = { currentMonth = currentMonth.minusMonths(1) },
                onNextMonth = { currentMonth = currentMonth.plusMonths(1) },
                holidays = uiState.holidays
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Important Holidays Section
            val upcomingHolidays = remember(uiState.holidays) {
                uiState.holidays.filter { !it.holidayDate.isBefore(LocalDate.now()) }.take(5)
            }
            
            if (upcomingHolidays.isNotEmpty()) {
                Text(
                    text = "أهم العطلات",
                    color = JudicialNavy,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AntiqueBrass.copy(alpha = 0.1f))
                        .border(1.dp, AntiqueBrass, RoundedCornerShape(12.dp))
                ) {
                    upcomingHolidays.forEachIndexed { index, holiday ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.EventAvailable,
                                contentDescription = null,
                                tint = JudicialNavy,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = holiday.name,
                                color = JudicialNavy,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = DateFormatterUtils.formatArabicFullDate(holiday.holidayDate),
                                color = TerracottaSeal,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        if (index < upcomingHolidays.size - 1) {
                            HorizontalDivider(color = AntiqueBrass, thickness = 1.dp)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Bottom verification bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = VerifiedEmerald,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "آخر تحقق من المصادر الرسمية",
                        color = VerifiedEmerald,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                OutlinedButton(
                    onClick = onRefreshClick,
                    enabled = !uiState.isSyncing,
                    border = BorderStroke(1.dp, JudicialNavy),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = JudicialNavy)
                ) {
                    if (uiState.isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = JudicialNavy
                        )
                    } else {
                        Text("تحديث البيانات")
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
fun MonthlyCalendarGrid(
    currentMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    holidays: List<Holiday>,
    modifier: Modifier = Modifier
) {
    val monthName = DateFormatterUtils.getArabicMonthName(currentMonth.monthValue)
    val yearString = currentMonth.year.toEasternArabic()
    
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "السابق", tint = JudicialNavy)
            }
            Text("$monthName $yearString", color = JudicialNavy, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            IconButton(onClick = onNextMonth) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "التالي", tint = JudicialNavy)
            }
        }
        
        val daysOfWeek = listOf("السبت", "الأحد", "الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة")
        Row(modifier = Modifier.fillMaxWidth()) {
            daysOfWeek.forEach { dayName ->
                Text(
                    text = dayName,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = if (dayName == "الجمعة") TerracottaSeal else JudicialNavy,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium
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
                        val isHoliday = holidays.any { it.holidayDate == date }
                        val textColor = if (isFriday || isHoliday) TerracottaSeal else JudicialNavy
                        val bgColor = if (isHoliday) TerracottaSeal.copy(alpha = 0.1f) else Color.Transparent
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(bgColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayNum.toEasternArabic(),
                                color = textColor,
                                fontWeight = if (isHoliday || isFriday) FontWeight.Bold else FontWeight.Normal,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
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
        Holiday(id = 1, holidayDate = LocalDate.now().plusDays(2), name = "عيد الميلاد المجيد"),
        Holiday(id = 2, holidayDate = LocalDate.now().plusDays(20), name = "ثورة 25 يناير وعيد الشرطة")
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
