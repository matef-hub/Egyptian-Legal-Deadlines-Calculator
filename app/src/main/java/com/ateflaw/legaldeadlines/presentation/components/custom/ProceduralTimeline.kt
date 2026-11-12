package com.ateflaw.legaldeadlines.presentation.components.custom

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ateflaw.legaldeadlines.presentation.ui.theme.BlueprintSteel
import com.ateflaw.legaldeadlines.presentation.ui.theme.JudicialNavy
import com.ateflaw.legaldeadlines.presentation.ui.theme.TerracottaSeal

data class TimelineNode(
    val number: String,
    val title: String,
    val date: String,
    val isFinal: Boolean = false,
    val isOutlined: Boolean = false
)

@Composable
fun ProceduralTimeline(
    nodes: List<TimelineNode>,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(90.dp)
            .padding(vertical = 8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(80.dp)) {
            val width = size.width
            val nodeCount = nodes.size
            if (nodeCount == 0) return@Canvas
            
            val segmentWidth = width / nodeCount
            val circleRadius = 14.dp.toPx()
            val centerY = 24.dp.toPx()
            
            val nodeCenters = mutableListOf<Offset>()
            
            // RTL means node 0 is on the right, node 2 is on the left
            nodes.forEachIndexed { index, node ->
                val cx = width - (segmentWidth * index) - (segmentWidth / 2f)
                nodeCenters.add(Offset(cx, centerY))
            }
            
            // Draw lines and arrows connecting nodes
            for (i in 0 until nodeCenters.size - 1) {
                val start = nodeCenters[i]
                val end = nodeCenters[i + 1]
                
                // Draw line from edge to edge
                val lineStart = Offset(start.x - circleRadius - 8.dp.toPx(), centerY)
                val lineEnd = Offset(end.x + circleRadius + 8.dp.toPx(), centerY)
                
                drawLine(
                    color = BlueprintSteel.copy(alpha = 0.5f),
                    start = lineStart,
                    end = lineEnd,
                    strokeWidth = 1.dp.toPx()
                )
                
                // Draw small arrow pointing left (RTL direction)
                val arrowX = lineStart.x - ((lineStart.x - lineEnd.x) / 2f)
                val arrowPath = Path().apply {
                    moveTo(arrowX, centerY)
                    lineTo(arrowX + 4.dp.toPx(), centerY - 4.dp.toPx())
                    lineTo(arrowX + 4.dp.toPx(), centerY + 4.dp.toPx())
                    close()
                }
                drawPath(
                    path = arrowPath,
                    color = BlueprintSteel.copy(alpha = 0.5f),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
            
            // Draw nodes
            nodes.forEachIndexed { index, node ->
                val center = nodeCenters[index]
                val nodeColor = if (node.isFinal) TerracottaSeal else JudicialNavy
                
                // Draw circle
                if (node.isOutlined) {
                    drawCircle(
                        color = nodeColor,
                        radius = circleRadius,
                        center = center,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                } else {
                    drawCircle(
                        color = nodeColor,
                        radius = circleRadius,
                        center = center
                    )
                }
                
                // Draw number
                val numberColor = if (node.isOutlined) nodeColor else Color.White
                val numberLayout = textMeasurer.measure(
                    text = node.number,
                    style = TextStyle(color = numberColor, fontSize = 12.sp)
                )
                drawText(
                    textLayoutResult = numberLayout,
                    topLeft = Offset(
                        x = center.x - (numberLayout.size.width / 2f),
                        y = center.y - (numberLayout.size.height / 2f)
                    )
                )
                
                // Draw title
                val titleLayout = textMeasurer.measure(
                    text = node.title,
                    style = TextStyle(color = JudicialNavy, fontSize = 12.sp)
                )
                drawText(
                    textLayoutResult = titleLayout,
                    topLeft = Offset(
                        x = center.x - (titleLayout.size.width / 2f),
                        y = center.y + circleRadius + 8.dp.toPx()
                    )
                )
                
                // Draw date
                val dateColor = if (node.isFinal) TerracottaSeal else BlueprintSteel
                val dateLayout = textMeasurer.measure(
                    text = node.date,
                    style = TextStyle(color = dateColor, fontSize = 14.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                )
                drawText(
                    textLayoutResult = dateLayout,
                    topLeft = Offset(
                        x = center.x - (dateLayout.size.width / 2f),
                        y = center.y + circleRadius + 8.dp.toPx() + titleLayout.size.height
                    )
                )
            }
        }
    }
}
