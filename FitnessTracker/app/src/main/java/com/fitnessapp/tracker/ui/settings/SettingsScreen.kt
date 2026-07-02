package com.fitnessapp.tracker.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Balance
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MonitorWeight
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.fitnessapp.tracker.ui.theme.LocalThemeColors
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.ui.platform.LocalContext
import com.fitnessapp.tracker.ui.settings.screens.BodyMetricsScreen
import com.fitnessapp.tracker.ui.settings.screens.ExerciseLibraryScreen
import com.fitnessapp.tracker.ui.settings.screens.ThemeSettingsScreen
import com.fitnessapp.tracker.ui.settings.screens.UnitSettingsScreen
import com.fitnessapp.tracker.data.model.Equipment
import com.fitnessapp.tracker.ui.theme.THEMES
import java.io.File
import java.io.FileOutputStream

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
            onNavigate = { currentPage = it },
            viewModel = viewModel
        )
        SettingsPage.EXERCISE_LIBRARY -> ExerciseLibraryScreen(
            onBack = { currentPage = SettingsPage.MAIN },
            exercises = state.exercises,
            onAddExercise = { name, bodyPart, equipment, recordType, iconName -> viewModel.addExercise(name, bodyPart, equipment, recordType, iconName) },
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
    onNavigate: (SettingsPage) -> Unit,
    viewModel: SettingsViewModel
) {
    val currentTheme = THEMES[state.currentThemeIndex]
    val context = LocalContext.current

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val destFile = File(context.filesDir, "custom_background.jpg")
                    val outputStream = FileOutputStream(destFile)
                    inputStream.use { input ->
                        outputStream.use { output ->
                            input.copyTo(output)
                        }
                    }
                    viewModel.setBgImagePath(destFile.absolutePath)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

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

        Spacer(Modifier.height(14.dp))

        Text("界面背景", style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 4.dp))
        Spacer(Modifier.height(4.dp))

        SettingsGroup {
            SettingsItemWithSwitch(
                icon = Icons.Default.Image,
                title = "显示背景图片",
                description = "开启后使用图片作为 App 底图",
                checked = state.bgImageEnabled,
                onCheckedChange = { viewModel.setBgImageEnabled(it) }
            )

            if (state.bgImageEnabled) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                SettingsItem(
                    icon = Icons.Default.AddPhotoAlternate,
                    title = "选择背景图片",
                    description = if (state.bgImagePath != null) "已设置自定义图片" else "当前使用默认太空背景",
                    trailing = {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    onClick = {
                        pickImageLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )

                if (state.bgImagePath != null) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                    SettingsItem(
                        icon = Icons.Default.Delete,
                        title = "清除自定义图片",
                        description = "恢复为默认太空背景",
                        onClick = {
                            viewModel.setBgImagePath(null)
                        }
                    )
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    val themeColors = LocalThemeColors.current
    val borderGradient = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.18f),
            themeColors.primary.copy(alpha = 0.35f),
            Color.White.copy(alpha = 0.05f)
        )
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, borderGradient)
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
private fun SettingsItemWithSwitch(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(modifier = Modifier.fillMaxWidth()) {
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
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
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
