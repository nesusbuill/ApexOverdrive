package com.apex.overdrive.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object C {
    val Gold = Color(0xFFFFD700)
    val Silver = Color(0xFFE0E0E0)
    val DarkGold = Color(0xFFB8860B)
    
    val BgDeep = Color(0xFF050505)
    val Surface = Color(0xFF121212)
    val SurfaceGlass = Color(0xBB1A1A1A)
    val Border = Color(0xFF333333)
    
    val TextPrimary = Color(0xFFF5F5F5)
    val TextSecondary = Color(0xFF9E9E9E)
    
    val Red = Color(0xFFFF1744)
    val Green = Color(0xFF00E676)
    val Cyan = Color(0xFF00E5FF)
    val Purple = Color(0xFFD500F9)
    val Magenta = Purple
    val Lime = Green
    val Orange = Color(0xFFFF9800)
    val Success = Green
    val Warning = Color(0xFFFFB300)
    val Danger = Red
    
    val BgSurface = Surface
    val BgElevated = Color(0xFF1E1E1E)
    
    fun fromLong(l: Long) = Color(l)
}

private val scheme = darkColorScheme(
    primary = C.Gold, secondary = C.Silver, background = C.BgDeep, surface = C.Surface,
    onPrimary = C.BgDeep, onBackground = C.TextPrimary, onSurface = C.TextPrimary, error = C.Red
)

val SansFont: FontFamily = FontFamily.SansSerif
val ApexTypography = Typography(
    displayLarge = TextStyle(fontFamily = SansFont, fontWeight = FontWeight.Black, fontSize = 24.sp, letterSpacing = 1.sp),
    titleLarge = TextStyle(fontFamily = SansFont, fontWeight = FontWeight.Bold, fontSize = 16.sp),
    bodyLarge = TextStyle(fontFamily = SansFont, fontWeight = FontWeight.Medium, fontSize = 14.sp),
    bodyMedium = TextStyle(fontFamily = SansFont, fontSize = 12.sp),
    labelSmall = TextStyle(fontFamily = SansFont, fontWeight = FontWeight.Bold, fontSize = 10.sp)
)

@Composable
fun ApexTheme(content: @Composable () -> Unit) =
    MaterialTheme(colorScheme = scheme, typography = ApexTypography, content = content)
