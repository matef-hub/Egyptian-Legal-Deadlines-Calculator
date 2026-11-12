package com.ateflaw.legaldeadlines.presentation.components.custom

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import com.ateflaw.legaldeadlines.presentation.ui.theme.AntiqueBrass

@Composable
fun DiamondDivider(
    modifier: Modifier = Modifier,
    color: Color = AntiqueBrass
) {
    Canvas(modifier = modifier.fillMaxWidth().height(16.dp)) {
        val width = size.width
        val centerY = size.height / 2f
        val diamondSize = 6.dp.toPx()
        
        // Draw line left of diamond
        drawLine(
            color = color.copy(alpha = 0.5f),
            start = Offset(0f, centerY),
            end = Offset(width / 2f - diamondSize, centerY),
            strokeWidth = 1.dp.toPx()
        )
        
        // Draw diamond in center
        val path = Path().apply {
            moveTo(width / 2f, centerY - diamondSize)
            lineTo(width / 2f + diamondSize, centerY)
            lineTo(width / 2f, centerY + diamondSize)
            lineTo(width / 2f - diamondSize, centerY)
            close()
        }
        drawPath(path = path, color = color)
        
        // Draw line right of diamond
        drawLine(
            color = color.copy(alpha = 0.5f),
            start = Offset(width / 2f + diamondSize, centerY),
            end = Offset(width, centerY),
            strokeWidth = 1.dp.toPx()
        )
    }
}
