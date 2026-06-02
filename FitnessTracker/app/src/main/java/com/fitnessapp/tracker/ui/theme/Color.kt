package com.fitnessapp.tracker.ui.theme

import androidx.compose.ui.graphics.Color

data class ThemeColors(
    val primary: Color,
    val primaryLight: Color,
    val primaryBg: Color,
    val name: String
)

val THEMES = listOf(
    ThemeColors(Color(0xFF6C63FF), Color(0xFF8B83FF), Color(0xFFF0EEFF), "紫罗兰"),
    ThemeColors(Color(0xFF2ECC71), Color(0xFF58D68D), Color(0xFFEAF2F1), "翡翠绿"),
    ThemeColors(Color(0xFF3498DB), Color(0xFF5DADE2), Color(0xFFEBF5FB), "天空蓝"),
    ThemeColors(Color(0xFFE67E22), Color(0xFFEB984E), Color(0xFFFDF2E9), "日落橙"),
    ThemeColors(Color(0xFFE74C3C), Color(0xFFEC7063), Color(0xFFFDEDEC), "玫瑰红"),
    ThemeColors(Color(0xFF1A1A2E), Color(0xFF2D2D44), Color(0xFFEEEEF4), "暗夜黑"),
    ThemeColors(Color(0xFF4A4A4A), Color(0xFF6B6B6B), Color(0xFFF0F0F0), "石墨灰"),
    ThemeColors(Color(0xFFFF6B6B), Color(0xFFFF8E8E), Color(0xFFFFF0F0), "珊瑚粉")
)
