package com.apex.overdrive.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun LineChart(
    data: List<Float>,
    maxData: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    Canvas(modifier = modifier.fillMaxWidth().height(60.dp)) {
        val width = size.width
        val height = size.height
        val stepX = width / 60f

        val path = Path()
        val fillPath = Path()

        data.forEachIndexed { index, value ->
            val x = index * stepX
            val safeValue = value.coerceIn(0f, maxData)
            val y = height - (safeValue / maxData) * height
            
            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, height)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
            if (index == data.size - 1) {
                fillPath.lineTo(x, height)
                fillPath.close()
            }
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(colors = listOf(color.copy(alpha = 0.3f), Color.Transparent))
        )

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}
