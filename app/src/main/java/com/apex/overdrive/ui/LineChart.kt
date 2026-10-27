package com.apex.overdrive.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LineChart(
    data: List<Float>,
    color: Color,
    modifier: Modifier = Modifier,
    label: String = ""
) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(C.BgSurface)
            .padding(10.dp)
    ) {
        Row {
            Text(
                label,
                color = C.TextSecondary,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.weight(1f))
            if (data.isNotEmpty()) {
                Text(
                    "${data.last().toInt()}",
                    color = color,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth().height(90.dp)) {
            if (data.isEmpty()) {
                Text(
                    "No data",
                    color = C.TextSecondary,
                    fontSize = 10.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
                return@Box
            }
            Canvas(Modifier.fillMaxSize()) {
                val maxV = (data.maxOrNull() ?: 100f).coerceAtLeast(1f)
                val stepX = if (data.size > 1) size.width / (data.size - 1) else size.width

                for (i in 1..2) {
                    val y = size.height * i / 3f
                    drawLine(
                        C.Border.copy(alpha = 0.3f),
                        Offset(0f, y),
                        Offset(size.width, y),
                        strokeWidth = 1f
                    )
                }

                val path = Path()
                data.forEachIndexed { i, v ->
                    val x = i * stepX
                    val y = size.height - (v / maxV) * size.height
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }

                val fillPath = Path().apply {
                    addPath(path)
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(
                    fillPath,
                    Brush.verticalGradient(
                        listOf(color.copy(alpha = 0.25f), color.copy(alpha = 0f))
                    )
                )
                drawPath(path, color, style = Stroke(width = 2f, cap = StrokeCap.Round))

                val lastX = (data.size - 1) * stepX
                val lastY = size.height - (data.last() / maxV) * size.height
                drawCircle(color.copy(alpha = 0.25f), 6f, Offset(lastX, lastY))
                drawCircle(color, 3f, Offset(lastX, lastY))
            }
        }
    }
}
