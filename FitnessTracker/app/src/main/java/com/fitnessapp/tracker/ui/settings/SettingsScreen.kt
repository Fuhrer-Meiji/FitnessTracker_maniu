package com.fitnessapp.tracker.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Balance
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MonitorWeight
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fitnessapp.tracker.ui.settings.screens.BodyMetricsScreen
import com.fitnessapp.tracker.ui.settings.screens.ExerciseLibraryScreen
import com.fitnessapp.tracker.ui.settings.screens.ThemeSettingsScreen
import com.fitnessapp.tracker.ui.settings.screens.UnitSettingsScreen
import com.fitnessapp.tracker.ui.theme.THEMES

enum class SettingsPage {
    MAIN, BODY_METRICS, EXERCISE_LIBRARY, THEME, UNIT
}

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    var currentPage by remember { mutableStateOf(SettingsPage.MAIN) }

    when (currentPage) {
        SettingsPage.MAIN -> SettingsMainScreen(
            state = state,
            onNavigate = { currentPage = it }
        )
        SettingsPage.EXERCISE_LIBRARY -> ExerciseLibraryScreen(
            onBack = { currentPage = SettingsPage.MAIN },
            exercises = state.exercises,
            onAddExercise = { name, bodyPart, recordType, iconName -> viewModel.addExercise(name, bodyPart, recordType, iconName) },
            onDeleteExercise = { viewModel.deleteExercise(it) }
        )
        SettingsPage.THEME -> ThemeSettingsScreen(
            currentIndex = state.currentThemeIndex,
            onSelectTheme = { viewModel.setThemeIndex(it) },
            onBack = { currentPage = SettingsPage.MAIN }
        )
        SettingsPage.UNIT -> UnitSettingsScreen(
            currentUnit = state.currentUnit,
            onSelectUnit = { viewModel.setUnit(it) },
            onBack = { currentPage = SettingsPage.MAIN }
        )
        SettingsPage.BODY_METRICS -> BodyMetricsScreen(
            metrics = state.bodyMetrics,
            currentUnit = state.currentUnit,
            onBack = { currentPage = SettingsPage.MAIN },
            onAddMetric = { weight, bodyFat -> viewModel.addBodyMetric(weight, bodyFat) },
            onDeleteMetric = { viewModel.deleteBodyMetric(it) }
        )
    }
}

@Composable
private fun SettingsMainScreen(
    state: SettingsUiState,
    onNavigate: (SettingsPage) -> Unit
) {
    val currentTheme = THEMES[state.currentThemeIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        Column(modifier = Modifier.padding(bottom = 18.dp)) {
            Text("设置", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(2.dp))
            Text("管理你的训练数据", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        SettingsGroup {
            SettingsItem(
                icon = Icons.Outlined.MonitorWeight,
                title = "身体数据",
                description = "体重、体脂率记录",
                trailing = { Text("${state.latestBodyWeight ?: "--"} ${state.currentUnit}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                onClick = { onNavigate(SettingsPage.BODY_METRICS) }
            )
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(modifier = Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuickStat("最新体重", "${state.latestBodyWeight?.let { if (state.currentUnit == "lb") it * 2.20462 else it }?.let { String.format("%.1f", it) } ?: "--"}", state.currentUnit)
                    QuickStat("体脂率", "${state.latestBodyFat?.toInt() ?: "--"}", "%")
                    QuickStat("记录", "${state.bodyMetricCount}", "次")
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        SettingsGroup {
            SettingsItem(
                icon = Icons.Outlined.FitnessCenter,
                title = "动作库管理",
                description = "查看/添加自定义动作",
                trailing = { Text("${state.exerciseCount} 个", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                onClick = { onNavigate(SettingsPage.EXERCISE_LIBRARY) }
            )
            SettingsItem(
                icon = Icons.Outlined.Palette,
                title = "主题颜色",
                description = "自定义 App 主色调",
                trailing = {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(RoundedCornerShape(50))
                            .background(currentTheme.primary)
                    )
                },
                onClick = { onNavigate(SettingsPage.THEME) }
            )
            SettingsItem(
                icon = Icons.Outlined.Balance,
                title = "单位设置",
                description = "kg / lb 切换",
                trailing = { Text(state.currentUnit, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary) },
                onClick = { onNavigate(SettingsPage.UNIT) }
            )
            SettingsItem(
                icon = Icons.Outlined.Info,
                title = "关于",
                description = "版本 1.0",
                onClick = { }
            )
        }
    }
}

@Composable
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    description: String,
    trailing: @Composable (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            trailing?.invoke()
        }
    }
}

@Composable
private fun RowScope.QuickStat(label: String, value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(unit, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
