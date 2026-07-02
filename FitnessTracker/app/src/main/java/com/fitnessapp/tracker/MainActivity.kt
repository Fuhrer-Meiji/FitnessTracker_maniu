package com.fitnessapp.tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.fitnessapp.tracker.ui.navigation.AppNavigation
import com.fitnessapp.tracker.ui.splash.SplashScreen
import com.fitnessapp.tracker.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val themeManager = ThemeManager(applicationContext)
        enableEdgeToEdge()
        setContent {
            val themeIndex by themeManager.themeIndex.collectAsState(initial = 0)
            val themeColors = THEMES[themeIndex]
            var showSplash by remember { mutableStateOf(true) }

            CompositionLocalProvider(LocalThemeColors provides themeColors) {
                FitnessTheme(themeColors = themeColors) {
                    if (showSplash) {
                        SplashScreen(onTimeout = { showSplash = false })
                    } else {
                        AppNavigation()
                    }
                }
            }
        }
    }
}
