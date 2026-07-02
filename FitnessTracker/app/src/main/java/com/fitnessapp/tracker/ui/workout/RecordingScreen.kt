package com.fitnessapp.tracker.ui.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.animation.core.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.fitnessapp.tracker.data.model.Exercise
import com.fitnessapp.tracker.data.model.RecordType
import com.fitnessapp.tracker.data.model.BodyPart
import com.fitnessapp.tracker.data.model.Equipment
import com.fitnessapp.tracker.ui.theme.LocalThemeColors
import com.fitnessapp.tracker.ui.workout.components.ExerciseCard
import com.fitnessapp.tracker.util.DateUtils

@Composable
fun RecordingScreen(
    viewModel: WorkoutViewModel,
    state: WorkoutUiState
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
                .padding(bottom = if (state.restCountdownSeconds > 0) 88.dp else 0.dp) // Leave room for floating rest timer
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    WorkoutTimerGlowDisk(elapsedSeconds = state.elapsedSeconds)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("当前训练中...", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("计时开始", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Surface(
                    onClick = { viewModel.showEndConfirm() },
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text("结束训练", fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                }
            }
            Spacer(Modifier.height(4.dp))

            val activeSupersets = remember(state.cards) {
                state.cards.mapNotNull { it.supersetGroupId }.distinct()
            }

            state.cards.forEachIndexed { index, card ->
                val supersetIndex = if (card.supersetGroupId != null) activeSupersets.indexOf(card.supersetGroupId) else -1
                val supersetLabel = if (supersetIndex != -1) "超级组 ${'A' + supersetIndex}" else null
                val supersetColor = if (supersetIndex != -1) {
                    when (supersetIndex % 3) {
                        0 -> Color(0xFF8B5CF6) // Purple
                        1 -> Color(0xFF0D9488) // Teal
                        else -> Color(0xFFEA580C) // Orange
                    }
                } else null

                ExerciseCard(
                    card = card,
                    cardIndex = index,
                    onAdjustField = { field, delta -> viewModel.adjustField(index, field, delta) },
                    onAddSet = { viewModel.addSetToCard(index) },
                    onDeleteSet = { setIndex -> viewModel.deleteSet(index, setIndex) },
                    onDeleteCard = { viewModel.deleteCard(index) },
                    onChangeExercise = { viewModel.showExercisePicker(index) },
                    onLinkSuperset = if (index < state.cards.size - 1) { { viewModel.linkCardsAsSuperset(index) } } else null,
                    onUnlinkSuperset = if (card.supersetGroupId != null) { { viewModel.unlinkCardFromSuperset(index) } } else null,
                    onActivate = { viewModel.activateCard(index) },
                    supersetLabel = supersetLabel,
                    supersetColor = supersetColor,
                    currentUnit = state.currentUnit,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }

            OutlinedButton(
                onClick = { viewModel.showExercisePicker() },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("+ 添加新动作", fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }

        if (state.restCountdownSeconds > 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                val themeColors = LocalThemeColors.current
                val borderGradient = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.2f),
                        themeColors.primary.copy(alpha = 0.4f),
                        Color.White.copy(alpha = 0.05f)
                    )
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    border = BorderStroke(1.dp, borderGradient)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Countdown ring
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(44.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                val progress = state.restCountdownSeconds.toFloat() / state.totalRestTimeSeconds.toFloat()
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val strokeW = 3.dp.toPx()
                                    drawCircle(
                                        color = Color(0xFF202026),
                                        radius = (size.minDimension - strokeW) / 2,
                                        center = center,
                                        style = Stroke(width = strokeW)
                                    )
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
                                    text = "${state.restCountdownSeconds}s",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            Spacer(Modifier.width(12.dp))
                            
                            Column {
                                Text(
                                    "休息中...", 
                                    fontSize = 13.sp, 
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "下组科学间歇中", 
                                    fontSize = 10.sp, 
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Right: Controls
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = { viewModel.adjustRestTimer(-30) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Text("-30s", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            
                            IconButton(
                                onClick = { viewModel.adjustRestTimer(30) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Text("+30s", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }

                            IconButton(
                                onClick = { 
                                    if (state.restCountdownActive) {
                                        viewModel.pauseRestTimer()
                                    } else {
                                        viewModel.resumeRestTimer()
                                    }
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        color = themeColors.primary.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(18.dp)
                                    )
                            ) {
                                Text(
                                    text = if (state.restCountdownActive) "⏸" else "▶",
                                    fontSize = 12.sp,
                                    color = themeColors.primary
                                )
                            }

                            TextButton(
                                onClick = { viewModel.skipRestTimer() },
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text(
                                    "跳过", 
                                    fontSize = 12.sp, 
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // End workout confirmation
    if (state.showEndConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.hideEndConfirm() },
            title = { Text("确定结束本次训练？", fontWeight = FontWeight.Bold) },
            text = { Text("您可以选择保存并结束本次训练，或者放弃并删除所有本次的训练记录。") },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = { viewModel.endWorkout() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("保存并结束", fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { viewModel.discardCurrentWorkout() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                    ) {
                        Text("放弃并删除", fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(4.dp))
                    TextButton(
                        onClick = { viewModel.hideEndConfirm() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("继续训练", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        )
    }

    // Exercise picker modal
    if (state.showExercisePicker) {
        ExercisePickerModal(
            exercises = state.exercises,
            onSelect = { exercise ->
                val target = state.pickerTargetCardIndex
                if (target != null) {
                    viewModel.replaceCardExercise(target, exercise)
                } else {
                    viewModel.addExerciseCard(exercise)
                }
            },
            onDismiss = { viewModel.hideExercisePicker() }
        )
    }
}

@Composable
private fun ExercisePickerModal(
    exercises: List<Exercise>,
    onSelect: (Exercise) -> Unit,
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
                Text("选择动作", fontWeight = FontWeight.Bold, fontSize = 17.sp)
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
                                Spacer(Modifier.weight(1f))
                                RecordTypeBadge(exercise.recordType)
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
private fun RecordTypeBadge(recordType: RecordType) {
    val (bg, fg) = when (recordType) {
        RecordType.STRENGTH -> Pair(androidx.compose.ui.graphics.Color(0xFFE8F5E9), androidx.compose.ui.graphics.Color(0xFF2E7D32))
        RecordType.REPS -> Pair(androidx.compose.ui.graphics.Color(0xFFE3F2FD), androidx.compose.ui.graphics.Color(0xFF1565C0))
        RecordType.DURATION -> Pair(androidx.compose.ui.graphics.Color(0xFFFFF3E0), androidx.compose.ui.graphics.Color(0xFFE65100))
    }
    Text(
        recordType.label,
        fontSize = 8.sp,
        fontWeight = FontWeight.SemiBold,
        color = fg,
        modifier = Modifier
            .background(bg, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

@Composable
fun WorkoutTimerGlowDisk(elapsedSeconds: Long) {
    val themeColors = LocalThemeColors.current
    val infiniteTransition = rememberInfiniteTransition(label = "timerGlow")
    
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotationAngle"
    )

    val formattedTime = DateUtils.formatDuration(elapsedSeconds)

    Box(
        modifier = Modifier.size(64.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 3.dp.toPx()
            val radius = (size.minDimension - strokeWidth - 6.dp.toPx()) / 2
            val center = center

            // 1. Draw outer neon breathing glow shadow
            drawCircle(
                color = themeColors.primary.copy(alpha = 0.15f * glowScale),
                radius = radius + 4.dp.toPx() * glowScale,
                center = center
            )

            // 2. Draw circular track
            drawCircle(
                color = Color(0xFF202026),
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth)
            )

            // 3. Draw animated sweeping progress indicator with gradient
            val gradientBrush = Brush.sweepGradient(
                colors = listOf(themeColors.primary, themeColors.primaryLight, themeColors.primary),
                center = center
            )

            val progress = (elapsedSeconds % 60) / 60f
            val sweepAngle = (progress * 360f).coerceAtLeast(6f)

            rotate(degrees = rotationAngle - 90f) {
                drawArc(
                    brush = gradientBrush,
                    startAngle = 0f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth + 0.5.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        Text(
            text = formattedTime,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            letterSpacing = 0.5.sp
        )
    }
}
