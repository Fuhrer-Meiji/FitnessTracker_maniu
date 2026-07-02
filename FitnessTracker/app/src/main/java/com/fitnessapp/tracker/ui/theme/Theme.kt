package com.fitnessapp.tracker.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color.Unspecified,
    onPrimary = Color.White,
    surface = Color(0xFF16161A),
    background = Color(0xFF0A0A0C),
    onBackground = Color(0xFFE4E4E6),
    surfaceVariant = Color(0xFF202026),
    outline = Color(0xFF25252E),
    onSurface = Color(0xFFF3F3F5),
    onSurfaceVariant = Color(0xFF8E8E9F),
    error = Color(0xFFFF5252)
)

val LocalThemeColors = compositionLocalOf { THEMES[0] }

@Composable
fun FitnessTheme(
    themeColors: ThemeColors = LocalThemeColors.current,
    content: @Composable () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val themeManager = remember { ThemeManager(context) }
    val bgImageEnabled by themeManager.bgImageEnabled.collectAsState(initial = false)

    val dynamicOnPrimary = if (themeColors.primary.let { it.red * 0.299f + it.green * 0.587f + it.blue * 0.114f } > 0.6f) {
        Color.Black
    } else {
        Color.White
    }

    val colorScheme = DarkColors.copy(
        primary = themeColors.primary,
        onPrimary = dynamicOnPrimary,
        surfaceVariant = if (bgImageEnabled) themeColors.primaryBg.copy(alpha = 0.5f) else themeColors.primaryBg,
        surface = if (bgImageEnabled) Color(0xD916161A) else Color(0xFF16161A)
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = FitnessTypography,
        content = content
    )
}
