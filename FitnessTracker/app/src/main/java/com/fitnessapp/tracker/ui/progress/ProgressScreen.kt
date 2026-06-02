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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
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
        Spacer(Modifier.height(4.dp))
        Column(modifier = Modifier.padding(bottom = 14.dp)) {
            Text("进度", style = MaterialTheme.typography.titleLarge)
            Text("你的训练数据概览", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("本周训练", "${state.weeklyCount}", Modifier.weight(1f))
            StatCard("本月训练", "${state.monthlyCount}", Modifier.weight(1f))
            StatCard("总训练", "${state.totalCount}", Modifier.weight(1f))
        }

        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            CalendarView(
                workoutDates = state.workoutDates,
                dailyFrequency = state.dailyFrequency,
                modifier = Modifier.padding(16.dp)
            )
        }

        StrengthTrendCard(
            exercises = state.exercises,
            selectedExercise = state.selectedExercise,
            trendData = state.strengthTrendData,
            currentUnit = state.currentUnit,
            onSelectExercise = { viewModel.selectExercise(it) }
        )
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("力量趋势", style = MaterialTheme.typography.titleMedium)

                Box {
                    Surface(
                        onClick = { showDropdown = true },
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            selectedExercise?.name ?: "选择动作",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
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

            if (trendData.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(120.dp).padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("暂无数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                StrengthLineChart(
                    data = trendData,
                    modifier = Modifier.fillMaxWidth().height(140.dp).padding(vertical = 8.dp)
                )
            }

            if (trendData.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    val latest = trendData.lastOrNull()
                    val prev = trendData.dropLast(1).maxOfOrNull { it.second }
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 24.dp)) {
                        Text(latest?.let { String.format("%.1f", it.second) } ?: "--",
                            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("当前 ($currentUnit)", fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 24.dp)) {
                        Text(prev?.let { String.format("%.1f", it) } ?: "--",
                            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
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
    val values = data.map { it.second }

    Canvas(modifier = modifier) {
        if (values.size < 2) return@Canvas
        val maxVal = values.max()
        val minVal = values.min()
        val range = (maxVal - minVal).coerceAtLeast(1.0)
        val stepX = size.width / (values.size - 1)
        val padding = 4.dp.toPx()
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

        // Line path
        val path = Path().apply {
            moveTo(points[0].x, points[0].y)
            for (i in 1 until points.size) {
                lineTo(points[i].x, points[i].y)
            }
        }
        drawPath(path, primaryColor, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

        // Dots
        points.forEach { p ->
            drawCircle(primaryColor, radius = 3.dp.toPx(), center = p)
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground)
            Text(label, fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
