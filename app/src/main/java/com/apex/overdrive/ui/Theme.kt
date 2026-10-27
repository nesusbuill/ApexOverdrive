package com.apex.overdrive.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object C {
    val Cyan = Color(0xFF00E5FF)
    val Purple = Color(0xFFD500F9)
    val Blue = Color(0xFF2979FF)
    val DarkBg = Color(0xFF060B14)
    val Surface = Color(0xFF131A2A)
    val SurfaceGlass = Color(0xBB1A233A)
    val Border = Color(0xFF2A3756)
    
    val TextPrimary = Color(0xFFF1F5F9)
    val TextSecondary = Color(0xFF8B94A8)
    
    val Red = Color(0xFFFF1744)
    val Magenta = Purple
    val Lime = Color(0xFF00E676)
    val Orange = Color(0xFFFF9800)
    val Gold = Color(0xFFFFD700)
    val Success = Lime
    val Warning = Orange
    val Danger = Red
    fun fromLong(l: Long) = Color(l)
}

private val scheme = darkColorScheme(
    primary = C.Cyan, secondary = C.Purple, background = C.DarkBg, surface = C.Surface,
    onPrimary = C.DarkBg, onBackground = C.TextPrimary, onSurface = C.TextPrimary, error = C.Red
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
