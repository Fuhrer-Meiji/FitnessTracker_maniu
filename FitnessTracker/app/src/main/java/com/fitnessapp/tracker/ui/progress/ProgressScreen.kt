package com.fitnessapp.tracker.ui.progress

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import com.fitnessapp.tracker.data.model.RecordType
import com.fitnessapp.tracker.ui.workout.components.CalendarView
import java.util.*

@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        Column(modifier = Modifier.padding(bottom = 18.dp)) {
            Text("进度", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(2.dp))
            Text("你的训练数据概览", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("本周训练", "${state.weeklyCount}", Modifier.weight(1f))
            StatCard("本月训练", "${state.monthlyCount}", Modifier.weight(1f))
            StatCard("总训练", "${state.totalCount}", Modifier.weight(1f))
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                CalendarView(
                    workoutDates = state.workoutDates,
                    dailyFrequency = state.dailyFrequency,
                    selectedDay = state.selectedDay,
                    onDayClick = { viewModel.selectDay(it) },
                )

                if (state.selectedDay != null) {
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    Spacer(Modifier.height(12.dp))

                    if (state.dayWorkouts.isEmpty()) {
                        Text("无训练记录", fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp))
                    } else {
                        Text("训练详情", style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onBackground)
                        Spacer(Modifier.height(8.dp))
                        state.dayWorkouts.forEach { detail ->
                            Text(detail.exerciseName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onBackground)
                            detail.sets.forEach { set ->
                                Row(modifier = Modifier.padding(start = 8.dp, top = 2.dp)) {
                                    Text("第${set.setNumber}组", fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(Modifier.width(12.dp))
                                    when (set.recordType) {
                                        RecordType.STRENGTH -> {
                                            val w = com.fitnessapp.tracker.util.UnitConverter.displayWeight(set.weight ?: 0.0, state.currentUnit)
                                            Text(String.format("%.1f ${state.currentUnit}", w), fontSize = 11.sp)
                                            if (set.reps != null) { Spacer(Modifier.width(8.dp)); Text("× ${set.reps} 次", fontSize = 11.sp) }
                                        }
                                        RecordType.REPS -> Text("${set.reps} 次", fontSize = 11.sp)
                                        RecordType.DURATION -> Text("${set.durationSeconds} 秒", fontSize = 11.sp)
                                    }
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        StrengthTrendCard(
            exercises = state.exercises,
            selectedExercise = state.selectedExercise,
            trendData = state.strengthTrendData,
            currentUnit = state.currentUnit,
            onSelectExercise = { viewModel.selectExercise(it) }
        )

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun StrengthTrendCard(
    exercises: List<com.fitnessapp.tracker.data.model.Exercise>,
    selectedExercise: com.fitnessapp.tracker.data.model.Exercise?,
    trendData: List<Pair<String, Double>>,
    currentUnit: String,
    onSelectExercise: (com.fitnessapp.tracker.data.model.Exercise) -> Unit
) {
    var showDropdown by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
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

                Box {
                    Surface(
                        onClick = { showDropdown = true },
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
                    DropdownMenu(
                        expanded = showDropdown,
                        onDismissRequest = { showDropdown = false }
                    ) {
                        exercises.forEach { ex ->
                            DropdownMenuItem(
                                text = { Text(ex.name, fontSize = 13.sp) },
                                onClick = {
                                    onSelectExercise(ex)
                                    showDropdown = false
                                }
                            )
                        }
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
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 24.dp)) {
                        Text(latest?.let { String.format("%.1f", it.second) } ?: "--",
                            fontWeight = FontWeight.Bold, fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary)
                        Text("当前 ($currentUnit)", fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 24.dp)) {
                        Text(prev?.let { String.format("%.1f", it) } ?: "--",
                            fontWeight = FontWeight.Bold, fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary)
                        Text("最高 ($currentUnit)", fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val primaryAlpha = primaryColor.copy(alpha = 0.15f)
    val values = data.map { it.second }

    Canvas(modifier = modifier) {
        if (values.size < 2) return@Canvas
        val maxVal = values.max()
        val minVal = values.min()
        val range = (maxVal - minVal).coerceAtLeast(1.0)
        val stepX = size.width / (values.size - 1)
        val padding = 8.dp.toPx()
        val drawHeight = size.height - padding * 2

        val points = values.mapIndexed { i, v ->
            Offset(
                x = i * stepX,
                y = padding + drawHeight * (1 - ((v - minVal) / range)).toFloat()
            )
        }

        // Grid lines
        for (i in 0..3) {
            val y = padding + drawHeight * i / 3
            drawLine(surfaceVariant, Offset(0f, y), Offset(size.width, y), strokeWidth = 0.5.dp.toPx())
        }

        // Gradient fill
        val fillPath = Path().apply {
            moveTo(points[0].x, size.height - padding)
            for (p in points) lineTo(p.x, p.y)
            lineTo(points.last().x, size.height - padding)
            close()
        }
        drawPath(fillPath, primaryAlpha)

        // Line path
        val path = Path().apply {
            moveTo(points[0].x, points[0].y)
            for (i in 1 until points.size) {
                lineTo(points[i].x, points[i].y)
            }
        }
        drawPath(path, primaryColor, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

        // Dots
        points.forEach { p ->
            drawCircle(primaryColor, radius = 3.5.dp.toPx(), center = p)
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.height(72.dp)) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
            )
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary)
                Text(label, fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
