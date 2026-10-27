package com.apex.overdrive.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun GamingAvatar(sizeDp: Int = 72, accent: Color = C.Cyan) {
    val inf = rememberInfiniteTransition(label = "predator")
    val glow by inf.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            tween(2500, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "glow"
    )
    val flamePhase by inf.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            tween(1800, easing = LinearEasing),
            RepeatMode.Restart
        ),
        label = "flame"
    )
    val pupilPulse by inf.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            tween(1800, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "pupil"
    )

    var blinkPhase by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(7000)
            for (i in 0..5) {
                blinkPhase = i / 5f
                kotlinx.coroutines.delay(40)
            }
            blinkPhase = 0f
        }
    }

    Box(Modifier.size(sizeDp.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val eyeW = size.minDimension * 0.88f
            val eyeH = size.minDimension * 0.42f

            // Ambient glow
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0xFFFF1744).copy(alpha = 0.22f * glow), Color.Transparent),
                    center = Offset(cx, cy),
                    radius = eyeW * 0.85f
                ),
                radius = eyeW * 0.85f,
                center = Offset(cx, cy)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0xFFFF6B00).copy(alpha = 0.28f * glow), Color.Transparent),
                    center = Offset(cx, cy),
                    radius = eyeW * 0.62f
                ),
                radius = eyeW * 0.62f,
                center = Offset(cx, cy)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0xFFFFD700).copy(alpha = 0.3f * glow), Color.Transparent),
                    center = Offset(cx, cy),
                    radius = eyeW * 0.42f
                ),
                radius = eyeW * 0.42f,
                center = Offset(cx, cy)
            )

            // Eye shape
            val eyePath = Path().apply {
                moveTo(cx - eyeW / 2f, cy)
                cubicTo(
                    cx - eyeW * 0.35f, cy - eyeH * 0.75f,
                    cx - eyeW * 0.05f, cy - eyeH * 0.95f,
                    cx, cy - eyeH * 0.85f
                )
                cubicTo(
                    cx + eyeW * 0.05f, cy - eyeH * 0.95f,
                    cx + eyeW * 0.35f, cy - eyeH * 0.75f,
                    cx + eyeW / 2f, cy
                )
                cubicTo(
                    cx + eyeW * 0.35f, cy + eyeH * 0.7f,
                    cx + eyeW * 0.05f, cy + eyeH * 0.9f,
                    cx, cy + eyeH * 0.82f
                )
                cubicTo(
                    cx - eyeW * 0.05f, cy + eyeH * 0.9f,
                    cx - eyeW * 0.35f, cy + eyeH * 0.7f,
                    cx - eyeW / 2f, cy
                )
                close()
            }
            drawPath(eyePath, Color(0xFF05020A))

            clipPath(eyePath) {
                drawRect(
                    brush = Brush.radialGradient(
                        listOf(Color(0xFF3D0A0A), Color(0xFF1A0505)),
                        center = Offset(cx, cy),
                        radius = eyeW * 0.5f
                    ),
                    topLeft = Offset(cx - eyeW / 2f, cy - eyeH),
                    size = androidx.compose.ui.geometry.Size(eyeW, eyeH * 2f)
                )

                val irisR = eyeH * 0.78f
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(
                            Color(0xFFFFE066),
                            Color(0xFFFFB000),
                            Color(0xFFFF6B00),
                            Color(0xFFCC2200),
                            Color(0xFF4A0500)
                        ),
                        center = Offset(cx, cy),
                        radius = irisR
                    ),
                    radius = irisR,
                    center = Offset(cx, cy)
                )

                drawCircle(
                    Color(0xFFFF8800).copy(alpha = 0.6f * glow),
                    irisR,
                    Offset(cx, cy),
                    style = Stroke(size.minDimension * 0.008f)
                )

                val pupilW = irisR * 0.38f * pupilPulse
                val pupilH = irisR * 0.92f * pupilPulse
                val pupilPath = Path().apply {
                    moveTo(cx, cy - pupilH)
                    cubicTo(
                        cx + pupilW, cy - pupilH * 0.35f,
                        cx + pupilW, cy + pupilH * 0.35f,
                        cx, cy + pupilH
                    )
                    cubicTo(
                        cx - pupilW, cy + pupilH * 0.35f,
                        cx - pupilW, cy - pupilH * 0.35f,
                        cx, cy - pupilH
                    )
                    close()
                }
                drawPath(pupilPath, Color(0xFF000000))

                drawCircle(
                    Color.White.copy(alpha = 0.6f * glow),
                    irisR * 0.09f,
                    Offset(cx - irisR * 0.15f, cy - irisR * 0.3f)
                )
                drawCircle(
                    Color.White.copy(alpha = 0.3f * glow),
                    irisR * 0.05f,
                    Offset(cx + irisR * 0.12f, cy + irisR * 0.15f)
                )

                // Flame particles
                for (i in 0 until 9) {
                    val seed = i * 0.618f
                    val baseX = cx + (Random(seed.toInt() * 100).nextFloat() - 0.5f) * eyeW * 0.7f
                    val phase = flamePhase + seed * 2 * PI.toFloat()
                    val flicker = sin(phase.toDouble()).toFloat() * 0.5f + 0.5f
                    val waveY = sin(phase.toDouble() * 0.7).toFloat()
                    val flameHeight = eyeH * (0.3f + flicker * 0.25f)
                    val flameY = cy + eyeH * 0.4f - flameHeight - waveY * eyeH * 0.1f
                    val flameWidth = eyeW * (0.02f + flicker * 0.015f)
                    val flamePath = Path().apply {
                        moveTo(baseX, flameY + flameHeight)
                        cubicTo(
                            baseX - flameWidth, flameY + flameHeight * 0.6f,
                            baseX - flameWidth * 0.7f, flameY + flameHeight * 0.3f,
                            baseX, flameY
                        )
                        cubicTo(
                            baseX + flameWidth * 0.7f, flameY + flameHeight * 0.3f,
                            baseX + flameWidth, flameY + flameHeight * 0.6f,
                            baseX, flameY + flameHeight
                        )
                        close()
                    }
                    drawPath(
                        flamePath,
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFFFFE066).copy(alpha = 0.9f),
                                Color(0xFFFF6B00).copy(alpha = 0.7f),
                                Color(0xFFFF1744).copy(alpha = 0.3f)
                            ),
                            startY = flameY,
                            endY = flameY + flameHeight
                        )
                    )
                }
            }

            drawPath(
                eyePath,
                Brush.linearGradient(
                    listOf(Color(0xFFFF1744), Color(0xFFFFB000), Color(0xFFFF1744)),
                    start = Offset(cx - eyeW / 2f, cy),
                    end = Offset(cx + eyeW / 2f, cy)
                ),
                style = Stroke(size.minDimension * 0.028f)
            )

            // Lashes
            val lashes = listOf(
                Triple(-0.35f, -0.65f, 0.13f),
                Triple(-0.15f, -0.88f, 0.16f),
                Triple(0.0f, -0.92f, 0.18f),
                Triple(0.15f, -0.88f, 0.16f),
                Triple(0.35f, -0.65f, 0.13f),
                Triple(-0.28f, 0.55f, 0.10f),
                Triple(0.0f, 0.75f, 0.12f),
                Triple(0.28f, 0.55f, 0.10f)
            )
            lashes.forEach { (xOff, yOff, len) ->
                val startX = cx + xOff * eyeW / 2f
                val startY = cy + yOff * eyeH
                val dx = xOff * 0.3f * eyeW / 2f
                val dy = if (yOff < 0) -len * eyeH * 2.2f else len * eyeH * 2.2f
                drawLine(
                    Brush.linearGradient(
                        listOf(Color(0xFFFFB000), Color(0xFFFF1744).copy(alpha = 0.4f)),
                        start = Offset(startX, startY),
                        end = Offset(startX + dx, startY + dy)
                    ),
                    start = Offset(startX, startY),
                    end = Offset(startX + dx, startY + dy),
                    strokeWidth = 2.2f,
                    cap = StrokeCap.Round
                )
            }

            // Blink overlay
            if (blinkPhase > 0f) {
                val blinkH = eyeH * 1.2f * blinkPhase
                val blinkPath = Path().apply {
                    moveTo(cx - eyeW / 2f, cy - blinkH)
                    lineTo(cx + eyeW / 2f, cy - blinkH)
                    lineTo(cx + eyeW / 2f, cy + blinkH)
                    lineTo(cx - eyeW / 2f, cy + blinkH)
                    close()
                }
                drawPath(blinkPath, Color(0xFF05020A))
            }
        }
    }
}
