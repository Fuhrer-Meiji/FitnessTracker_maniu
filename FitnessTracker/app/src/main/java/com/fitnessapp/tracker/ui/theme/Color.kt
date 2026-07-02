package com.fitnessapp.tracker.ui.theme

import androidx.compose.ui.graphics.Color

data class ThemeColors(
    val primary: Color,
    val primaryLight: Color,
    val primaryBg: Color,
    val name: String
)

val THEMES = listOf(
    ThemeColors(Color(0xFF9E8BFF), Color(0xFFB5A7FF), Color(0x1F9E8BFF), "紫罗兰"),
    ThemeColors(Color(0xFF00E676), Color(0xFF66FFA6), Color(0x1F00E676), "翡翠绿"),
    ThemeColors(Color(0xFF00B0FF), Color(0xFF82B1FF), Color(0x1F00B0FF), "天空蓝"),
    ThemeColors(Color(0xFFFF6D00), Color(0xFFFFAB40), Color(0x1FFF6D00), "日落橙"),
    ThemeColors(Color(0xFFFF1744), Color(0xFFFF5252), Color(0x1FFFF1744), "玫瑰红"),
    ThemeColors(Color(0xFF00E5FF), Color(0xFF80DEEA), Color(0x1F00E5FF), "深海蓝"),
    ThemeColors(Color(0xFFAEEA00), Color(0xFFE4FF3A), Color(0x1FAEEA00), "火山绿"),
    ThemeColors(Color(0xFFFFD600), Color(0xFFFFE57F), Color(0x1FFFD600), "香槟金")
)
