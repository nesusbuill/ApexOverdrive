package com.apex.overdrive.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object C {
    val Cyan = Color(0xFF00D9FF)
    val Magenta = Color(0xFFEC4899)
    val Lime = Color(0xFF10B981)
    val Red = Color(0xFFEF4444)
    val Purple = Color(0xFFA855F7)
    val Orange = Color(0xFFF59E0B)
    val Gold = Color(0xFFFBBF24)

    val BgDeep = Color(0xFF0A0E1A)
    val BgSurface = Color(0xFF131824)
    val BgElevated = Color(0xFF1B2130)
    val Border = Color(0xFF252B3B)

    val TextPrimary = Color(0xFFF1F5F9)
    val TextSecondary = Color(0xFF8B94A8)

    fun fromLong(l: Long) = Color(l)
}

private val scheme = darkColorScheme(
    primary = C.Cyan,
    secondary = C.Magenta,
    tertiary = C.Lime,
    background = C.BgDeep,
    surface = C.BgSurface,
    onPrimary = C.BgDeep,
    onBackground = C.TextPrimary,
    onSurface = C.TextPrimary,
    error = C.Red
)

val SansFont: FontFamily = FontFamily.Default
val MonoFont: FontFamily = FontFamily.Monospace

val ApexTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = SansFont,
        fontWeight = FontWeight.Black,
        fontSize = 26.sp,
        letterSpacing = 1.sp
    ),
    titleLarge = TextStyle(
        fontFamily = SansFont,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        letterSpacing = 0.3.sp
    ),
    bodyLarge = TextStyle(fontFamily = SansFont, fontSize = 14.sp),
    bodyMedium = TextStyle(fontFamily = SansFont, fontSize = 12.sp),
    labelSmall = TextStyle(
        fontFamily = MonoFont,
        fontSize = 10.sp,
        letterSpacing = 0.8.sp
    )
)

@Composable
fun ApexTheme(content: @Composable () -> Unit) =
    MaterialTheme(
        colorScheme = scheme,
        typography = ApexTypography,
        content = content
    )
