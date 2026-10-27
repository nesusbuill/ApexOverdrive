package com.apex.overdrive.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun GamingAvatar(sizeDp: Int = 48, accent: Color = C.Cyan) {
    val inf = rememberInfiniteTransition(label = "avatar")
    val glow by inf.animateFloat(0.5f, 1f, infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "glow")

    Box(Modifier.size(sizeDp.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(sizeDp.dp)) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val r = size.minDimension * 0.45f

            // Hào quang
            drawCircle(Brush.radialGradient(listOf(accent.copy(alpha = 0.3f * glow), Color.Transparent), center = Offset(cx, cy), radius = r * 1.5f), r * 1.5f, Offset(cx, cy))
            // Nền
            drawCircle(C.DarkBg, r, Offset(cx, cy))
            // Viền Neon
            drawCircle(Brush.sweepGradient(listOf(accent, C.Purple, accent)), r, Offset(cx, cy), style = Stroke(size.minDimension * 0.05f))

            // Icon Rocket
            val path = Path().apply {
                moveTo(cx, cy - r * 0.6f)
                lineTo(cx - r * 0.25f, cy + r * 0.1f)
                lineTo(cx - r * 0.1f, cy + r * 0.1f)
                lineTo(cx - r * 0.2f, cy + r * 0.4f)
                lineTo(cx, cy + r * 0.25f)
                lineTo(cx + r * 0.2f, cy + r * 0.4f)
                lineTo(cx + r * 0.1f, cy + r * 0.1f)
                lineTo(cx + r * 0.25f, cy + r * 0.1f)
                close()
            }
            drawPath(path, Brush.linearGradient(listOf(Color.White, accent), start = Offset(cx, cy - r), end = Offset(cx, cy + r)))
        }
    }
}
