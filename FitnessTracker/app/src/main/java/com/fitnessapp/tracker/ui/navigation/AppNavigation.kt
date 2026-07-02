package com.fitnessapp.tracker.ui.navigation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.BitmapFactory
import com.fitnessapp.tracker.R
import com.fitnessapp.tracker.ui.progress.ProgressScreen
import com.fitnessapp.tracker.ui.settings.SettingsScreen
import com.fitnessapp.tracker.ui.theme.ThemeManager
import com.fitnessapp.tracker.ui.theme.LocalThemeColors
import com.fitnessapp.tracker.ui.workout.WorkoutScreen
import com.fitnessapp.tracker.ui.calendar.CalendarScreen
import java.io.File
import kotlinx.coroutines.launch

sealed class BottomNavItem(val route: String, val label: String, val icon: ImageVector) {
    data object Workout : BottomNavItem("workout", "训练", Icons.Default.FitnessCenter)
    data object Calendar : BottomNavItem("calendar", "日历", Icons.Default.DateRange)
    data object Progress : BottomNavItem("progress", "进度", Icons.AutoMirrored.Filled.ShowChart)
    data object Settings : BottomNavItem("settings", "设置", Icons.Default.Settings)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager(context) }
    val themeColors = LocalThemeColors.current
    val bgImageEnabled by themeManager.bgImageEnabled.collectAsState(initial = false)
    val bgImagePath by themeManager.bgImagePath.collectAsState(initial = null)

    val items = listOf(BottomNavItem.Workout, BottomNavItem.Calendar, BottomNavItem.Progress, BottomNavItem.Settings)
    val pagerState = rememberPagerState(pageCount = { items.size })
    val coroutineScope = rememberCoroutineScope()

    val customBitmap = remember(bgImagePath) {
        val path = bgImagePath
        if (!path.isNullOrEmpty()) {
            val file = File(path)
            if (file.exists()) {
                try {
                    BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
                } catch (e: Exception) {
                    null
                }
            } else null
        } else null
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (bgImageEnabled) {
            if (customBitmap != null) {
                Image(
                    bitmap = customBitmap,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.default_bg),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.65f))
            )
        }

        Scaffold(
            containerColor = if (bgImageEnabled) Color.Transparent else MaterialTheme.colorScheme.background,
            bottomBar = {
                NavigationBar(
                    containerColor = if (bgImageEnabled) {
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                    },
                    tonalElevation = NavigationBarDefaults.Elevation
                ) {
                    items.forEachIndexed { index, item ->
                        val selected = pagerState.currentPage == index
                        val scale by animateFloatAsState(
                            targetValue = if (selected) 1.25f else 1.0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "tabIconScale"
                        )

                        NavigationBarItem(
                            icon = { 
                                Icon(
                                    imageVector = item.icon, 
                                    contentDescription = item.label,
                                    modifier = Modifier.scale(scale)
                                ) 
                            },
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
                                indicatorColor = themeColors.primary.copy(alpha = 0.22f)
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
                Box(modifier = Modifier.fillMaxSize()) {
                    when (items[page]) {
                        BottomNavItem.Workout -> WorkoutScreen()
                        BottomNavItem.Calendar -> CalendarScreen()
                        BottomNavItem.Progress -> ProgressScreen()
                        BottomNavItem.Settings -> SettingsScreen()
                    }
                }
            }
        }
    }
}
