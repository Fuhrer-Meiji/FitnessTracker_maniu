package com.fitnessapp.tracker.ui.progress

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import com.fitnessapp.tracker.data.model.RecordType
import com.fitnessapp.tracker.data.model.BodyPart
import com.fitnessapp.tracker.data.model.Equipment
import com.fitnessapp.tracker.data.db.dao.WorkoutSetWithDate
import com.fitnessapp.tracker.util.UnitConverter
import com.fitnessapp.tracker.ui.theme.LocalThemeColors
import com.fitnessapp.tracker.ui.workout.components.CalendarView
import com.fitnessapp.tracker.ui.progress.components.MuscleHeatmapCard
import java.util.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.nativeCanvas

@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    val themeColors = LocalThemeColors.current
    val borderGradient = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.18f),
            themeColors.primary.copy(alpha = 0.35f),
            Color.White.copy(alpha = 0.05f)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        Column(modifier = Modifier.padding(bottom = 14.dp)) {
            Text("进度", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(2.dp))
            Text("你的训练数据概览", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        // Bento Grid Row 1: [Weekly/Monthly Training Counts (Column)] + [Total Stats Card (Double Height)]
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val weeklyProgress = (state.weeklyCount / 3f).coerceIn(0f, 1f)
            val monthlyProgress = (state.monthlyCount / 12f).coerceIn(0f, 1f)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard("本周训练", "${state.weeklyCount} 次", weeklyProgress, Modifier.fillMaxWidth())
                StatCard("本月训练", "${state.monthlyCount} 次", monthlyProgress, Modifier.fillMaxWidth())
            }

            Card(
                modifier = Modifier.weight(1f).height(162.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(0.5.dp, borderGradient)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("累计数据", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "${state.totalCount} 次",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text("完成训练总数", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    val totalMinutes = state.totalDuration / 60000
                    val totalHours = totalMinutes / 60.0
                    val formattedDuration = if (totalHours >= 1.0) String.format("%.1f 小时", totalHours) else "$totalMinutes 分钟"

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = formattedDuration,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.primaryLight
                        )
                        Text("累计训练时长", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Bento Grid Row 2: [Weight Record Card]
        Card(
            modifier = Modifier.fillMaxWidth().height(90.dp).padding(bottom = 10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(0.5.dp, borderGradient)
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("最新体重", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${state.latestWeight} ${state.currentUnit}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("今日体征记录", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(
                    onClick = { viewModel.showWeightDialog(true) },
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.primary.copy(alpha = 0.15f), contentColor = themeColors.primary),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("记录", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        RadarChartCard(bodyPartCounts = state.bodyPartCounts)

        Spacer(Modifier.height(10.dp))

        MuscleHeatmapCard(bodyPartSetsCount = state.bodyPartCounts)

        CalorieBarChartCard(dailyCalories = state.dailyCalories)

        Spacer(Modifier.height(6.dp))

        StrengthTrendCard(
            selectedExercise = state.selectedExercise,
            trendData = state.strengthTrendData,
            currentUnit = state.currentUnit,
            selectedTrendType = state.selectedTrendType,
            exerciseSetsHistory = state.exerciseSetsHistory,
            onSelectExerciseClick = { viewModel.showExercisePicker() },
            onSelectTrendType = { viewModel.selectTrendType(it) },
            onDeleteSetClick = { viewModel.deleteSetFromTrend(it) }
        )

        Spacer(Modifier.height(24.dp))
    }

    if (state.showWeightDialog) {
        WeightInputDialog(
            currentWeight = state.latestWeight,
            currentUnit = state.currentUnit,
            onConfirm = { weight ->
                viewModel.logWeight(weight)
                viewModel.showWeightDialog(false)
            },
            onDismiss = { viewModel.showWeightDialog(false) }
        )
    }

    if (state.showExercisePicker) {
        ProgressExercisePickerModal(
            exercises = state.exercises,
            onSelect = { viewModel.selectExercise(it) },
            onDismiss = { viewModel.hideExercisePicker() }
        )
    }
}

@Composable
private fun WeightInputDialog(
    currentWeight: Double,
    currentUnit: String,
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var weightInput by remember { mutableStateOf(currentWeight.toString()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("记录体重", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
        text = {
            Column {
                Text("输入您今天的体重 ($currentUnit)", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = weightInput,
                    onValueChange = { weightInput = it },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { 
                        weightInput.toDoubleOrNull()?.let { onConfirm(it) }
                    }),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { weightInput.toDoubleOrNull()?.let { onConfirm(it) } }
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun StrengthTrendCard(
    selectedExercise: com.fitnessapp.tracker.data.model.Exercise?,
    trendData: List<Pair<String, Double>>,
    currentUnit: String,
    selectedTrendType: TrendType,
    exerciseSetsHistory: List<com.fitnessapp.tracker.data.db.dao.WorkoutSetWithDate>,
    onSelectExerciseClick: () -> Unit,
    onSelectTrendType: (TrendType) -> Unit,
    onDeleteSetClick: (Long) -> Unit
) {
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
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(18.dp)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("力量趋势", style = MaterialTheme.typography.titleMedium)
                }

                Surface(
                    onClick = onSelectExerciseClick,
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        selectedExercise?.name ?: "选择动作",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TrendType.entries.forEach { type ->
                    val selected = type == selectedTrendType
                    val bg = if (selected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent
                    val fg = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(bg)
                            .clickable { onSelectTrendType(type) }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = type.label,
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = fg
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            if (trendData.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("暂无数据", fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        Text("完成训练后趋势将显示在这里", fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    }
                }
            } else {
                StrengthLineChart(
                    data = trendData,
                    modifier = Modifier.fillMaxWidth().height(140.dp)
                )
            }

            if (trendData.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    val latest = trendData.lastOrNull()
                    val prev = trendData.dropLast(1).maxOfOrNull { it.second }
                    val (latestLabel, bestLabel) = when (selectedTrendType) {
                        TrendType.MAX_WEIGHT -> Pair("当前重量", "最高重量")
                        TrendType.ESTIMATED_1RM -> Pair("当前估算 1RM", "最高估算 1RM")
                        TrendType.TOTAL_VOLUME -> Pair("本次总容量", "最高单次容量")
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 24.dp)) {
                        Text(latest?.let { String.format("%.1f", it.second) } ?: "--",
                            fontWeight = FontWeight.Bold, fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary)
                        Text("$latestLabel ($currentUnit)", fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 24.dp)) {
                        Text(prev?.let { String.format("%.1f", it) } ?: "--",
                            fontWeight = FontWeight.Bold, fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary)
                        Text("$bestLabel ($currentUnit)", fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            if (exerciseSetsHistory.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                Spacer(Modifier.height(12.dp))
                
                Text(
                    "历史训练组数记录", 
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 180.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    val dateFormat = remember { java.text.SimpleDateFormat("yyyy/MM/dd", java.util.Locale.CHINESE) }
                    exerciseSetsHistory.forEach { set ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = dateFormat.format(java.util.Date(set.date)),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(2.dp))
                                val recordStr = when (selectedExercise?.recordType) {
                                    RecordType.STRENGTH -> {
                                        val displayWeight = UnitConverter.displayWeight(set.weight ?: 0.0, currentUnit)
                                        "第 ${set.setNumber} 组: ${String.format("%.1f", displayWeight)} $currentUnit * ${set.reps ?: 0} 次"
                                    }
                                    RecordType.REPS -> "第 ${set.setNumber} 组: ${set.reps ?: 0} 次"
                                    RecordType.DURATION -> "第 ${set.setNumber} 组: ${set.durationSeconds ?: 0}s"
                                    else -> ""
                                }
                                Text(
                                    text = recordStr,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            
                            IconButton(
                                onClick = { onDeleteSetClick(set.id) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Text(
                                    text = "✕",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StrengthLineChart(
    data: List<Pair<String, Double>>,
    modifier: Modifier = Modifier
) {
    val themeColors = LocalThemeColors.current
    val primaryColor = themeColors.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val values = data.map { it.second }

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(data) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
    }

    Canvas(modifier = modifier) {
        val padding = 8.dp.toPx()
        val drawHeight = size.height - padding * 2

        // Grid lines
        for (i in 0..3) {
            val y = padding + drawHeight * i / 3
            drawLine(surfaceVariant, Offset(0f, y), Offset(size.width, y), strokeWidth = 0.5.dp.toPx())
        }

        if (values.size == 1) {
            val p = Offset(size.width / 2, size.height / 2)
            // Outer glow ring
            drawCircle(
                color = primaryColor.copy(alpha = 0.3f),
                radius = 10.dp.toPx() * animationProgress.value,
                center = p
            )
            // Inner solid dot
            drawCircle(
                color = primaryColor,
                radius = 5.dp.toPx() * animationProgress.value,
                center = p
            )
            return@Canvas
        }

        if (values.size < 2) return@Canvas
        val maxVal = values.max()
        val minVal = values.min()
        val range = (maxVal - minVal).coerceAtLeast(1.0)
        val stepX = size.width / (values.size - 1)
        val baselineY = size.height - padding

        val points = values.mapIndexed { i, v ->
            val targetY = padding + drawHeight * (1 - ((v - minVal) / range)).toFloat()
            val animatedY = baselineY - (baselineY - targetY) * animationProgress.value
            Offset(
                x = i * stepX,
                y = animatedY
            )
        }

        // Vertical gradient for the fill path (cyberpunk glow under the line)
        val fillGradient = Brush.verticalGradient(
            colors = listOf(
                primaryColor.copy(alpha = 0.3f * animationProgress.value),
                primaryColor.copy(alpha = 0.0f)
            ),
            startY = padding,
            endY = size.height - padding
        )

        // Gradient fill path
        val fillPath = Path().apply {
            moveTo(points[0].x, size.height - padding)
            for (p in points) lineTo(p.x, p.y)
            lineTo(points.last().x, size.height - padding)
            close()
        }
        drawPath(fillPath, fillGradient)

        // Horizontal gradient brush for the line path itself
        val lineGradient = Brush.linearGradient(
            colors = listOf(themeColors.primary, themeColors.primaryLight),
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f)
        )

        // Line path
        val path = Path().apply {
            moveTo(points[0].x, points[0].y)
            for (i in 1 until points.size) {
                lineTo(points[i].x, points[i].y)
            }
        }
        drawPath(
            path = path,
            brush = lineGradient,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Dual-layered glowing nodes
        points.forEach { p ->
            // Outer glow ring
            drawCircle(
                color = primaryColor.copy(alpha = 0.3f),
                radius = 7.dp.toPx() * animationProgress.value,
                center = p
            )
            // Inner solid dot
            drawCircle(
                color = primaryColor,
                radius = 3.5.dp.toPx() * animationProgress.value,
                center = p
            )
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, progress: Float?, modifier: Modifier = Modifier) {
    val themeColors = LocalThemeColors.current
    val borderGradient = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.18f),
            themeColors.primary.copy(alpha = 0.35f),
            Color.White.copy(alpha = 0.05f)
        )
    )

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, borderGradient)
    ) {
        Row(
            modifier = Modifier
                .height(76.dp)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = value,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = label,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier.size(32.dp),
                contentAlignment = Alignment.Center
            ) {
                if (progress != null) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeW = 3.dp.toPx()
                        // Track
                        drawCircle(
                            color = Color(0xFF202026),
                            radius = (size.minDimension - strokeW) / 2,
                            center = center,
                            style = Stroke(width = strokeW)
                        )
                        // Indicator
                        drawArc(
                            color = themeColors.primary,
                            startAngle = -90f,
                            sweepAngle = progress * 360f,
                            useCenter = false,
                            topLeft = Offset(strokeW / 2, strokeW / 2),
                            size = Size(size.width - strokeW, size.height - strokeW),
                            style = Stroke(width = strokeW, cap = StrokeCap.Round)
                        )
                    }
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                color = themeColors.primary.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = themeColors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressExercisePickerModal(
    exercises: List<com.fitnessapp.tracker.data.model.Exercise>,
    onSelect: (com.fitnessapp.tracker.data.model.Exercise) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedBodyPartFilter by remember { mutableStateOf<BodyPart?>(null) }
    var selectedEquipmentFilter by remember { mutableStateOf<Equipment?>(null) }

    val filteredExercises = remember(exercises, searchQuery, selectedBodyPartFilter, selectedEquipmentFilter) {
        exercises.filter { ex ->
            val matchQuery = searchQuery.isBlank() || ex.name.contains(searchQuery, ignoreCase = true)
            val matchBodyPart = selectedBodyPartFilter == null || ex.bodyPart == selectedBodyPartFilter
            val matchEquipment = selectedEquipmentFilter == null || ex.equipment == selectedEquipmentFilter
            matchQuery && matchBodyPart && matchEquipment
        }
    }
    val grouped = remember(filteredExercises) {
        filteredExercises.groupBy { it.bodyPart }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("选择趋势动作", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("搜索...", fontSize = 14.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { /* no-op */ })
                )
                Spacer(Modifier.height(8.dp))

                // Body Part filter chips row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = selectedBodyPartFilter == null,
                        onClick = { selectedBodyPartFilter = null },
                        label = { Text("全部部位", fontSize = 11.sp) }
                    )
                    BodyPart.entries.forEach { part ->
                        FilterChip(
                            selected = selectedBodyPartFilter == part,
                            onClick = { selectedBodyPartFilter = part },
                            label = { Text(part.label, fontSize = 11.sp) }
                        )
                    }
                }

                // Equipment filter chips row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = selectedEquipmentFilter == null,
                        onClick = { selectedEquipmentFilter = null },
                        label = { Text("全部器材", fontSize = 11.sp) }
                    )
                    Equipment.entries.forEach { equip ->
                        FilterChip(
                            selected = selectedEquipmentFilter == equip,
                            onClick = { selectedEquipmentFilter = equip },
                            label = { Text(equip.label, fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 350.dp)
            ) {
                if (grouped.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("未找到相关动作", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    grouped.forEach { (part, exs) ->
                        item {
                            Text(part.label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp))
                        }
                        items(exs, key = { it.id }) { exercise ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelect(exercise) }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(exercise.iconName.take(2), fontSize = 16.sp)
                                }
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(exercise.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Text("${exercise.bodyPart.label} · ${exercise.equipment.label}", fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@Composable
private fun RadarChartCard(
    bodyPartCounts: Map<com.fitnessapp.tracker.data.model.BodyPart, Int>
) {
    val themeColors = LocalThemeColors.current
    val borderGradient = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.18f),
            themeColors.primary.copy(alpha = 0.35f),
            Color.White.copy(alpha = 0.05f)
        )
    )

    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, borderGradient)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(18.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                )
                Spacer(Modifier.width(8.dp))
                Text("部位训练分布", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(Modifier.height(16.dp))

            RadarChart(
                bodyPartCounts = bodyPartCounts,
                modifier = Modifier.fillMaxWidth().height(220.dp)
            )
        }
    }
}

@Composable
private fun RadarChart(
    bodyPartCounts: Map<com.fitnessapp.tracker.data.model.BodyPart, Int>,
    modifier: Modifier = Modifier
) {
    val themeColors = LocalThemeColors.current
    val primaryColor = themeColors.primary
    val gridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
    val textColor = MaterialTheme.colorScheme.onSurface

    val entries = remember(bodyPartCounts) {
        com.fitnessapp.tracker.data.model.BodyPart.entries.map { part ->
            part.label to (bodyPartCounts[part] ?: 0)
        }
    }
    
    val maxVal = remember(entries) {
        val maxCount = entries.maxOfOrNull { it.second } ?: 0
        maxOf(maxCount.toFloat(), 5f)
    }

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(bodyPartCounts) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )
    }

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val maxRadius = minOf(size.width, size.height) / 2 - 32.dp.toPx()
        val numAxes = 8
        val angleStep = (2 * Math.PI / numAxes).toFloat()

        // 1. Concentric octagons (grid lines)
        val numLevels = 4
        for (level in 1..numLevels) {
            val radius = maxRadius * (level.toFloat() / numLevels)
            val path = Path().apply {
                for (i in 0 until numAxes) {
                    val angle = i * angleStep - Math.PI.toFloat() / 2
                    val x = center.x + radius * Math.cos(angle.toDouble()).toFloat()
                    val y = center.y + radius * Math.sin(angle.toDouble()).toFloat()
                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }
            drawPath(
                path = path,
                color = gridColor,
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // 2. Axes and labels
        val textPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.argb(
                (textColor.alpha * 255).toInt(),
                (textColor.red * 255).toInt(),
                (textColor.green * 255).toInt(),
                (textColor.blue * 255).toInt()
            )
            textSize = 10.sp.toPx()
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }

        for (i in 0 until numAxes) {
            val angle = i * angleStep - Math.PI.toFloat() / 2
            val endX = center.x + maxRadius * Math.cos(angle.toDouble()).toFloat()
            val endY = center.y + maxRadius * Math.sin(angle.toDouble()).toFloat()
            
            drawLine(
                color = gridColor,
                start = center,
                end = Offset(endX, endY),
                strokeWidth = 1.dp.toPx()
            )

            val (labelName, count) = entries[i]
            val labelText = "$labelName\n(${count}次)"
            
            val cosVal = Math.cos(angle.toDouble()).toFloat()
            val sinVal = Math.sin(angle.toDouble()).toFloat()
            
            // Adjust horizontal text alignment dynamically
            textPaint.textAlign = when {
                cosVal > 0.2f -> android.graphics.Paint.Align.LEFT
                cosVal < -0.2f -> android.graphics.Paint.Align.RIGHT
                else -> android.graphics.Paint.Align.CENTER
            }
            
            // Adjust coordinate padding to prevent overlaps and edge clipping
            val extraPaddingX = when {
                cosVal > 0.2f -> 8.dp.toPx()
                cosVal < -0.2f -> -8.dp.toPx()
                else -> 0f
            }
            val extraPaddingY = when {
                sinVal < -0.8f -> -10.dp.toPx() // Push top labels upwards
                sinVal > 0.8f -> 6.dp.toPx()   // Push bottom labels downwards
                else -> 0f
            }
            
            val labelRadius = maxRadius + 10.dp.toPx()
            val labelX = center.x + labelRadius * cosVal + extraPaddingX
            val labelY = center.y + labelRadius * sinVal + extraPaddingY

            val lines = labelText.split("\n")
            lines.forEachIndexed { index, line ->
                val lineY = labelY + index * 12.dp.toPx() - (lines.size - 1) * 6.dp.toPx()
                drawContext.canvas.nativeCanvas.drawText(
                    line,
                    labelX,
                    lineY,
                    textPaint
                )
            }
        }

        // 3. Data polygon
        val minFraction = 0.4f
        val dataPoints = entries.mapIndexed { i, (_, count) ->
            val angle = i * angleStep - Math.PI.toFloat() / 2
            val rawFraction = count.toFloat() / maxVal
            val fraction = minFraction + (1f - minFraction) * rawFraction
            val valueFraction = fraction * animationProgress.value
            val radius = maxRadius * valueFraction
            Offset(
                x = center.x + radius * Math.cos(angle.toDouble()).toFloat(),
                y = center.y + radius * Math.sin(angle.toDouble()).toFloat()
            )
        }

        if (dataPoints.isNotEmpty()) {
            val fillPath = Path().apply {
                moveTo(dataPoints[0].x, dataPoints[0].y)
                for (i in 1 until dataPoints.size) {
                    lineTo(dataPoints[i].x, dataPoints[i].y)
                }
                close()
            }
            
            drawPath(
                path = fillPath,
                color = primaryColor.copy(alpha = 0.2f)
            )
            
            drawPath(
                path = fillPath,
                color = primaryColor,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            dataPoints.forEach { point ->
                drawCircle(
                    color = primaryColor.copy(alpha = 0.3f),
                    radius = 4.dp.toPx(),
                    center = point
                )
                drawCircle(
                    color = primaryColor,
                    radius = 2.dp.toPx(),
                    center = point
                )
            }
        }
    }
}

@Composable
private fun CalorieBarChartCard(
    dailyCalories: List<Pair<String, Double>>
) {
    val themeColors = LocalThemeColors.current
    val borderGradient = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.18f),
            themeColors.primary.copy(alpha = 0.35f),
            Color.White.copy(alpha = 0.05f)
        )
    )

    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, borderGradient)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(18.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                )
                Spacer(Modifier.width(8.dp))
                Text("热量消耗追踪 (kcal)", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(Modifier.height(16.dp))

            if (dailyCalories.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("暂无数据，完成训练后将在此处展示消耗热量", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                CalorieBarChart(
                    data = dailyCalories,
                    modifier = Modifier.fillMaxWidth().height(160.dp)
                )
            }
        }
    }
}

@Composable
private fun CalorieBarChart(
    data: List<Pair<String, Double>>,
    modifier: Modifier = Modifier
) {
    val themeColors = LocalThemeColors.current
    val primaryColor = themeColors.primary
    val secondaryColor = themeColors.primaryLight
    val gridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
    val textColor = MaterialTheme.colorScheme.onSurface

    val maxVal = remember(data) {
        val maxKcal = data.maxOfOrNull { it.second } ?: 0.0
        maxOf(maxKcal.toFloat(), 200f)
    }

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(data) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
    }

    Canvas(modifier = modifier) {
        val paddingLeft = 32.dp.toPx()
        val paddingBottom = 20.dp.toPx()
        val paddingTop = 15.dp.toPx()
        val paddingRight = 10.dp.toPx()

        val chartWidth = size.width - paddingLeft - paddingRight
        val chartHeight = size.height - paddingTop - paddingBottom

        val textPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.argb(
                (textColor.alpha * 150).toInt(),
                (textColor.red * 255).toInt(),
                (textColor.green * 255).toInt(),
                (textColor.blue * 255).toInt()
            )
            textSize = 8.sp.toPx()
            textAlign = android.graphics.Paint.Align.RIGHT
            isAntiAlias = true
        }

        val gridLines = 4
        for (i in 0 until gridLines) {
            val ratio = i.toFloat() / (gridLines - 1)
            val y = paddingTop + chartHeight * (1 - ratio)
            
            drawLine(
                color = gridColor,
                start = Offset(paddingLeft, y),
                end = Offset(size.width - paddingRight, y),
                strokeWidth = 0.5.dp.toPx()
            )

            val yValue = (maxVal * ratio).toInt()
            drawContext.canvas.nativeCanvas.drawText(
                "$yValue",
                paddingLeft - 6.dp.toPx(),
                y + 3.dp.toPx(),
                textPaint
            )
        }

        val numBars = data.size
        val barSpacingFraction = 0.4f
        val totalSpaceWidth = chartWidth / numBars
        val barWidth = totalSpaceWidth * (1 - barSpacingFraction)
        
        val datePaint = android.graphics.Paint().apply {
            color = android.graphics.Color.argb(
                (textColor.alpha * 180).toInt(),
                (textColor.red * 255).toInt(),
                (textColor.green * 255).toInt(),
                (textColor.blue * 255).toInt()
            )
            textSize = 8.sp.toPx()
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }

        val kcalPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.argb(
                (primaryColor.alpha * 255).toInt(),
                (primaryColor.red * 255).toInt(),
                (primaryColor.green * 255).toInt(),
                (primaryColor.blue * 255).toInt()
            )
            textSize = 8.sp.toPx()
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }

        data.forEachIndexed { index, (dateStr, kcal) ->
            val fraction = (kcal.toFloat() / maxVal) * animationProgress.value
            val barHeight = chartHeight * fraction
            
            val barLeft = paddingLeft + index * totalSpaceWidth + (totalSpaceWidth * barSpacingFraction / 2)
            val barTop = paddingTop + chartHeight - barHeight
            val barRight = barLeft + barWidth
            val barBottom = paddingTop + chartHeight

            val gradient = Brush.verticalGradient(
                colors = listOf(primaryColor, secondaryColor.copy(alpha = 0.5f)),
                startY = barTop,
                endY = barBottom
            )

            val barPath = Path().apply {
                val cornerRadius = 4.dp.toPx()
                moveTo(barLeft, barBottom)
                lineTo(barLeft, barTop + cornerRadius)
                quadraticBezierTo(barLeft, barTop, barLeft + cornerRadius, barTop)
                lineTo(barRight - cornerRadius, barTop)
                quadraticBezierTo(barRight, barTop, barRight, barTop + cornerRadius)
                lineTo(barRight, barBottom)
                close()
            }
            drawPath(path = barPath, brush = gradient)

            drawContext.canvas.nativeCanvas.drawText(
                dateStr,
                barLeft + barWidth / 2,
                size.height - 4.dp.toPx(),
                datePaint
            )

            if (kcal > 0) {
                drawContext.canvas.nativeCanvas.drawText(
                    "${kcal.toInt()}",
                    barLeft + barWidth / 2,
                    barTop - 4.dp.toPx(),
                    kcalPaint
                )
            }
        }
    }
}
