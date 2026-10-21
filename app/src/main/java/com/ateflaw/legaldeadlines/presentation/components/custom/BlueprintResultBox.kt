package com.ateflaw.legaldeadlines.presentation.components.custom

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ateflaw.legaldeadlines.presentation.ui.theme.AntiqueBrass
import com.ateflaw.legaldeadlines.presentation.ui.theme.BlueprintSteel

@Composable
fun BlueprintResultBox(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .background(
                color = AntiqueBrass.copy(alpha = 0.05f),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = AntiqueBrass.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(8.dp) // Gap between outer border and inner solid box
            .drawBehind {
                val innerWidth = size.width
                val innerHeight = size.height
                val cornerRadius = 6.dp.toPx()
                val crosshairLen = 8.dp.toPx()
                val strokeW = 1.dp.toPx()

                // Draw inner rectangle frame
                drawRoundRect(
                    color = BlueprintSteel,
                    size = size,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW)
                )

                // Function to draw crosshair at a corner
                fun drawCrosshair(x: Float, y: Float) {
                    // horizontal
                    drawLine(
                        color = BlueprintSteel,
                        start = Offset(x - crosshairLen, y),
                        end = Offset(x + crosshairLen, y),
                        strokeWidth = strokeW
                    )
                    // vertical
                    drawLine(
                        color = BlueprintSteel,
                        start = Offset(x, y - crosshairLen),
                        end = Offset(x, y + crosshairLen),
                        strokeWidth = strokeW
                    )
                }

                // Draw 4 crosshairs
                drawCrosshair(0f, 0f)
                drawCrosshair(innerWidth, 0f)
                drawCrosshair(0f, innerHeight)
                drawCrosshair(innerWidth, innerHeight)
            }
            .padding(8.dp), // Inner padding for content
        content = content
    )
}
