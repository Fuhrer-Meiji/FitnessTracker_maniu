package com.fitnessapp.tracker.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color.Unspecified,
    onPrimary = Color.White,
    surface = Color.White,
    background = Color(0xFFF8F8FC),
    onBackground = Color(0xFF1A1A2E),
    surfaceVariant = Color(0xFFF0EEFF),
    outline = Color(0xFFEEEEF4),
    outlineVariant = Color(0xFFEEEFF4),
    error = Color(0xFFFF6B6B),
    onSurface = Color(0xFF1A1A2E),
    onSurfaceVariant = Color(0xFF8E8EA0)
)

val LocalThemeColors = compositionLocalOf { THEMES[0] }

@Composable
fun FitnessTheme(
    themeColors: ThemeColors = LocalThemeColors.current,
    content: @Composable () -> Unit
) {
    val colorScheme = LightColors.copy(
        primary = themeColors.primary,
        surfaceVariant = themeColors.primaryBg
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = FitnessTypography,
        content = content
    )
}
