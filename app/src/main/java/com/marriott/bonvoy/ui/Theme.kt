package com.marriott.bonvoy.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object BonvoyColors {
    val Black = Color(0xFF1C1C1C)
    val Ink = Color(0xFF000000)
    val Sand = Color(0xFFF4F1EC)
    val Stone = Color(0xFFE6E1D8)
    val Gold = Color(0xFFB4975A)
    val Coral = Color(0xFFFF9662)
    val White = Color(0xFFFFFFFF)
    val Grey = Color(0xFF6B6B6B)
    val Error = Color(0xFFB3261E)
}

private val Scheme = lightColorScheme(
    primary = BonvoyColors.Black,
    onPrimary = BonvoyColors.White,
    secondary = BonvoyColors.Gold,
    onSecondary = BonvoyColors.White,
    tertiary = BonvoyColors.Coral,
    background = BonvoyColors.Sand,
    onBackground = BonvoyColors.Ink,
    surface = BonvoyColors.White,
    onSurface = BonvoyColors.Ink,
    surfaceVariant = BonvoyColors.Stone,
    onSurfaceVariant = BonvoyColors.Grey,
    error = BonvoyColors.Error,
)

private val BonvoyTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 34.sp,
        letterSpacing = 0.5.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 26.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
    ),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 14.sp),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        letterSpacing = 1.5.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        letterSpacing = 2.sp,
    ),
)

@Composable
fun BonvoyTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, typography = BonvoyTypography, content = content)
}
