package com.fitnessapp.tracker.ui.navigation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.fitnessapp.tracker.ui.progress.ProgressScreen
import com.fitnessapp.tracker.ui.settings.SettingsScreen
import com.fitnessapp.tracker.ui.workout.WorkoutScreen
import kotlinx.coroutines.launch

sealed class BottomNavItem(val route: String, val label: String, val icon: ImageVector) {
    data object Workout : BottomNavItem("workout", "训练", Icons.Default.FitnessCenter)
    data object Progress : BottomNavItem("progress", "进度", Icons.AutoMirrored.Filled.ShowChart)
    data object Settings : BottomNavItem("settings", "设置", Icons.Default.Settings)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppNavigation() {
    val items = listOf(BottomNavItem.Workout, BottomNavItem.Progress, BottomNavItem.Settings)
    val pagerState = rememberPagerState(pageCount = { items.size })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                tonalElevation = NavigationBarDefaults.Elevation
            ) {
                items.forEachIndexed { index, item ->
                    val selected = pagerState.currentPage == index
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                        selected = selected,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { page ->
            when (items[page]) {
                BottomNavItem.Workout -> WorkoutScreen()
                BottomNavItem.Progress -> ProgressScreen()
                BottomNavItem.Settings -> SettingsScreen()
            }
        }
    }
}
