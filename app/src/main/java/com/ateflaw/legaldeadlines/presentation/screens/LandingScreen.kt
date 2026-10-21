package com.ateflaw.legaldeadlines.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ateflaw.legaldeadlines.R
import com.ateflaw.legaldeadlines.presentation.ui.theme.AntiqueBrass
import com.ateflaw.legaldeadlines.presentation.ui.theme.ArefRuqaaFontFamily
import com.ateflaw.legaldeadlines.presentation.ui.theme.BlueprintSteel
import com.ateflaw.legaldeadlines.presentation.ui.theme.EgyptianLegalDeadlinesTheme
import com.ateflaw.legaldeadlines.presentation.ui.theme.JudicialNavy
import com.ateflaw.legaldeadlines.presentation.ui.theme.VintageBackground
import com.ateflaw.legaldeadlines.presentation.viewmodel.CalculatorViewModel
import java.util.Calendar
import java.util.Locale

@Composable
fun LandingScreen(
    viewModel: CalculatorViewModel,
    onNavigateToCalculator: () -> Unit,
    onNavigateToAgenda: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    LandingScreenContent(
        onNavigateToCalculator = onNavigateToCalculator,
        onNavigateToAgenda = onNavigateToAgenda,
        modifier = modifier
    )
}

@Composable
fun LandingScreenContent(
    onNavigateToCalculator: () -> Unit,
    onNavigateToAgenda: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VintageBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "ميعَاد",
                        fontFamily = ArefRuqaaFontFamily,
                        fontSize = 56.sp,
                        color = JudicialNavy,
                        lineHeight = 60.sp
                    )
                    Text(
                        text = "─── ┤ || ├ ───",
                        color = AntiqueBrass,
                        fontSize = 12.sp,
                        letterSpacing = 2.sp
                    )
                }
                
                Row(
                    modifier = Modifier.padding(top = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(48.dp)
                            .height(1.dp)
                            .background(AntiqueBrass)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("قانون", style = MaterialTheme.typography.bodySmall, color = AntiqueBrass)
                        Text("•", style = MaterialTheme.typography.bodySmall, color = AntiqueBrass)
                        Text("دقة", style = MaterialTheme.typography.bodySmall, color = AntiqueBrass)
                        Text("•", style = MaterialTheme.typography.bodySmall, color = AntiqueBrass)
                        Text("التزام", style = MaterialTheme.typography.bodySmall, color = AntiqueBrass)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Headline
            
            Text(
                text = "تقويم إجرائي للمحامي المصري",
                style = MaterialTheme.typography.titleMedium,
                color = AntiqueBrass,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(2.dp)
                    .background(AntiqueBrass)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Illustration
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                contentAlignment = Alignment.Center
            ) {
                // We assume ic_meead_illustration exists as a drawable
                Image(
                    painter = painterResource(id = R.drawable.ic_meead_illustration),
                    contentDescription = "Illustration of legal case file and calendar",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Metadata / Edition
            val currentDateStr = remember {
                val calendar = Calendar.getInstance()
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                val month = calendar.get(Calendar.MONTH) + 1
                val year = calendar.get(Calendar.YEAR)
                val str = String.format(Locale.US, "%02d / %02d / %04d", day, month, year)
                val arabic = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
                str.map { if (it in '0'..'9') arabic[it - '0'] else it }.joinToString("")
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(modifier = Modifier.width(24.dp).height(1.dp).background(AntiqueBrass))
                Text(
                    text = currentDateStr,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 18.sp,
                    color = JudicialNavy
                )
                Box(modifier = Modifier.width(24.dp).height(1.dp).background(AntiqueBrass))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "قاعدة المواعيد  •  مصر",
                style = MaterialTheme.typography.bodyMedium,
                color = BlueprintSteel
            )

            Spacer(modifier = Modifier.height(32.dp))

            // CTA Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onNavigateToCalculator,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JudicialNavy,
                        contentColor = Color.White
                    )
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "ابدأ حساب ميعاد",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Center)
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_long_left),
                            contentDescription = null,
                            tint = AntiqueBrass,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .rotate(180f)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .border(1.dp, AntiqueBrass, RoundedCornerShape(28.dp))
                        .clip(RoundedCornerShape(28.dp))
                        .clickable { onNavigateToAgenda() },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "استعرض الأجندة",
                            style = MaterialTheme.typography.titleMedium,
                            color = AntiqueBrass
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.ic_calendar_event),
                            contentDescription = null,
                            tint = AntiqueBrass
                        )
                    }
                }
            }
            
            // Bottom Left indicator
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(1.dp)
                        .background(AntiqueBrass)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "البداية / 01",
                    style = MaterialTheme.typography.bodySmall,
                    color = BlueprintSteel
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LandingScreenVintagePreview() {
    EgyptianLegalDeadlinesTheme {
        LandingScreenContent(
            onNavigateToCalculator = {}
        )
    }
}
