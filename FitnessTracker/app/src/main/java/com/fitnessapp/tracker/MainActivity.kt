package com.fitnessapp.tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.fitnessapp.tracker.ui.navigation.AppNavigation
import com.fitnessapp.tracker.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val themeManager = ThemeManager(applicationContext)
        enableEdgeToEdge()
        setContent {
            val themeIndex by themeManager.themeIndex.collectAsState(initial = 0)
            val themeColors = THEMES[themeIndex]
            CompositionLocalProvider(LocalThemeColors provides themeColors) {
                FitnessTheme(themeColors = themeColors) {
                    AppNavigation()
                }
            }
        }
    }
}
