package com.ateflaw.legaldeadlines.presentation.components.custom

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ateflaw.legaldeadlines.presentation.ui.theme.AntiqueBrass
import com.ateflaw.legaldeadlines.presentation.ui.theme.JudicialNavy

@Composable
fun EditorialMargin(
    number: String,
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min)
    ) {
        // Content Area
        Box(modifier = Modifier.weight(1f)) {
            content()
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Vertical Margin Column (Left side in RTL)
        Box(
            modifier = Modifier
                .width(48.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopCenter
        ) {
            androidx.compose.foundation.layout.Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = number,
                    style = MaterialTheme.typography.titleLarge,
                    color = AntiqueBrass,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Rotated vertical text
                androidx.compose.ui.layout.Layout(
                    content = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodySmall,
                            color = AntiqueBrass,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                ) { measurables, constraints ->
                    val placeable = measurables.first().measure(constraints)
                    layout(placeable.height, placeable.width) {
                        placeable.place(
                            x = -(placeable.width / 2) + (placeable.height / 2),
                            y = (placeable.width / 2) - (placeable.height / 2),
                            zIndex = 0f
                        )
                    }
                }
            }
            
            // Vertical Brass Line and Flag
            Spacer(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(16.dp)
                    .align(Alignment.TopStart) // Start is Right in RTL
                    .drawBehind {
                        val strokeW = 1.dp.toPx()
                        // Draw line
                        drawLine(
                            color = AntiqueBrass,
                            start = Offset(0f, 0f),
                            end = Offset(0f, size.height),
                            strokeWidth = strokeW
                        )
                        // Draw triangle flag pointing right (in RTL context)
                        val path = Path().apply {
                            moveTo(0f, 8.dp.toPx())
                            lineTo(6.dp.toPx(), 12.dp.toPx())
                            lineTo(0f, 16.dp.toPx())
                            close()
                        }
                        drawPath(
                            path = path,
                            color = AntiqueBrass,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW)
                        )
                    }
            )
        }
    }
}
