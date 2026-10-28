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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GamingAvatar(sizeDp: Int = 64, accent: Color = C.Gold) {
    val inf = rememberInfiniteTransition(label = "avatar")
    val glow by inf.animateFloat(0.6f, 1f, infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "glow")
    val textMeasurer = rememberTextMeasurer()

    Box(Modifier.size(sizeDp.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(sizeDp.dp)) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val r = size.minDimension * 0.46f

            drawCircle(Brush.radialGradient(listOf(C.Gold.copy(alpha = 0.15f * glow), Color.Transparent), center = Offset(cx, cy), radius = r * 1.5f), r * 1.5f, Offset(cx, cy))
            drawCircle(Brush.linearGradient(listOf(Color(0xFF1A1A1A), C.BgDeep), start = Offset(cx - r, cy - r), end = Offset(cx + r, cy + r)), r, Offset(cx, cy))
            drawCircle(brush = Brush.sweepGradient(listOf(C.Gold, C.Silver, C.DarkGold, C.Gold)), radius = r, center = Offset(cx, cy), style = Stroke(width = size.minDimension * 0.04f))
            
            val crownY = cy - r * 0.7f
            val crownPath = Path().apply {
                moveTo(cx - r * 0.2f, crownY + r * 0.15f)
                lineTo(cx - r * 0.2f, crownY)
                lineTo(cx - r * 0.1f, crownY + r * 0.1f)
                lineTo(cx, crownY - r * 0.05f)
                lineTo(cx + r * 0.1f, crownY + r * 0.1f)
                lineTo(cx + r * 0.2f, crownY)
                lineTo(cx + r * 0.2f, crownY + r * 0.15f)
                close()
            }
            drawPath(crownPath, color = C.Gold)

            val textLayout = textMeasurer.measure(
                text = "AP",
                style = TextStyle(
                    brush = Brush.linearGradient(listOf(C.Silver, C.Gold, C.DarkGold)),
                    fontSize = (size.width * 0.35f).sp,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic
                )
            )
            drawText(textLayout, topLeft = Offset(cx - textLayout.size.width / 2f, cy - textLayout.size.height / 2f - r * 0.1f))

            val boltX = cx + r * 0.2f
            val boltY = cy - r * 0.1f
            val boltPath = Path().apply {
                moveTo(boltX, boltY)
                lineTo(boltX - r * 0.15f, boltY + r * 0.25f)
                lineTo(boltX + r * 0.05f, boltY + r * 0.25f)
                lineTo(boltX - r * 0.1f, boltY + r * 0.55f)
                lineTo(boltX + r * 0.2f, boltY + r * 0.15f)
                lineTo(boltX, boltY + r * 0.15f)
                close()
            }
            drawPath(boltPath, brush = Brush.linearGradient(listOf(C.Gold, Color.White)))

            val padY = cy + r * 0.5f
            val padW = r * 0.5f
            val padH = r * 0.2f
            val padPath = Path().apply {
                moveTo(cx - padW/2, padY)
                quadraticBezierTo(cx - padW/2, padY + padH, cx - padW/4, padY + padH)
                lineTo(cx + padW/4, padY + padH)
                quadraticBezierTo(cx + padW/2, padY + padH, cx + padW/2, padY)
                quadraticBezierTo(cx + padW/4, padY - padH/2, cx, padY)
                quadraticBezierTo(cx - padW/4, padY - padH/2, cx - padW/2, padY)
                close()
            }
            drawPath(padPath, color = C.Gold, style = Stroke(width = 2f))
            drawCircle(C.Gold, 1.5f, Offset(cx - padW * 0.15f, padY + padH * 0.4f))
            drawCircle(C.Gold, 1.5f, Offset(cx + padW * 0.15f, padY + padH * 0.4f))
        }
    }
}
