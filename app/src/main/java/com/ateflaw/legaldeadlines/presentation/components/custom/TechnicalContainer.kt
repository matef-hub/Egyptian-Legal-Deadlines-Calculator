package com.ateflaw.legaldeadlines.presentation.components.custom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ateflaw.legaldeadlines.presentation.ui.theme.BlueprintSteel
import com.ateflaw.legaldeadlines.presentation.ui.theme.VintageFieldFill

@Composable
fun TechnicalContainer(
    modifier: Modifier = Modifier,
    fillColor: Color = VintageFieldFill,
    strokeColor: Color = BlueprintSteel,
    strokeWidth: Dp = 1.2.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .drawBehind {
                val path = Path()
                val width = size.width
                val height = size.height
                
                val tlChamfer = 8.dp.toPx()
                val brChamfer = 8.dp.toPx()
                val notchWidth = 6.dp.toPx()
                val notchHeight = 4.dp.toPx()
                val tickLength = 4.dp.toPx()
                val tickSpacing = 4.dp.toPx()
                
                // Start from top-left chamfer
                path.moveTo(0f, tlChamfer)
                path.lineTo(tlChamfer, 0f)
                
                // Top line up to the notch
                path.lineTo(width - notchWidth, 0f)
                
                // Top-right notch (goes UP)
                path.lineTo(width - notchWidth, -notchHeight)
                path.lineTo(width, -notchHeight)
                path.lineTo(width, 0f)
                
                // Right line down to bottom-right chamfer start
                path.lineTo(width, height - brChamfer)
                
                // Bottom-right chamfer
                path.lineTo(width - brChamfer, height)
                
                // Bottom line leftwards to bottom-left
                path.lineTo(0f, height)
                
                // Close path (left line up)
                path.close()

                // Draw filled background
                drawPath(
                    path = path,
                    color = fillColor
                )
                
                // Draw stroke
                drawPath(
                    path = path,
                    color = strokeColor,
                    style = Stroke(width = strokeWidth.toPx())
                )
                
                // Draw bottom-left ticks (Two perpendicular ticks pointing downwards)
                val tick1X = 6.dp.toPx()
                val tick2X = tick1X + tickSpacing
                
                drawLine(
                    color = strokeColor,
                    start = Offset(tick1X, height),
                    end = Offset(tick1X, height + tickLength),
                    strokeWidth = strokeWidth.toPx()
                )
                
                drawLine(
                    color = strokeColor,
                    start = Offset(tick2X, height),
                    end = Offset(tick2X, height + tickLength),
                    strokeWidth = strokeWidth.toPx()
                )
                
                // A small horizontal tick at top left just outside the chamfer
                drawLine(
                    color = strokeColor,
                    start = Offset(-4.dp.toPx(), tlChamfer),
                    end = Offset(0f, tlChamfer),
                    strokeWidth = strokeWidth.toPx()
                )
            }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        content = content
    )
}
