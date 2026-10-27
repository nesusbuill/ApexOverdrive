package com.apex.overdrive.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.sin

@Composable
fun AnimatedBackground(accent: Color = C.Cyan) {
    val inf = rememberInfiniteTransition(label = "bg")
    val t by inf.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(12000, easing = LinearEasing),
            RepeatMode.Reverse
        ),
        label = "t"
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(C.BgDeep, Color(0xFF080B14), C.BgDeep)
                )
            )
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Blob 1 — top right
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(accent.copy(alpha = 0.10f), Color.Transparent),
                    center = Offset(w * 0.85f, h * 0.15f + sin(t * 3f) * 80f),
                    radius = w * 0.85f
                ),
                radius = w * 0.85f,
                center = Offset(w * 0.85f, h * 0.15f + sin(t * 3f) * 80f)
            )

            // Blob 2 — bottom left
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(C.Purple.copy(alpha = 0.08f), Color.Transparent),
                    center = Offset(w * 0.15f, h * 0.85f + sin(t * 3f + 2f) * 60f),
                    radius = w * 0.75f
                ),
                radius = w * 0.75f,
                center = Offset(w * 0.15f, h * 0.85f + sin(t * 3f + 2f) * 60f)
            )

            // Grid pattern
            val step = 80f
            var x = 0f
            while (x < w) {
                drawLine(
                    C.Border.copy(alpha = 0.06f),
                    Offset(x, 0f),
                    Offset(x, h),
                    strokeWidth = 1f
                )
                x += step
            }
            var y = 0f
            while (y < h) {
                drawLine(
                    C.Border.copy(alpha = 0.06f),
                    Offset(0f, y),
                    Offset(w, y),
                    strokeWidth = 1f
                )
                y += step
            }
        }
    }
}
