package com.fitnessapp.tracker.ui.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitnessapp.tracker.data.model.Exercise
import com.fitnessapp.tracker.data.model.RecordType
import com.fitnessapp.tracker.ui.workout.components.ExerciseCard
import com.fitnessapp.tracker.util.DateUtils

@Composable
fun RecordingScreen(
    viewModel: WorkoutViewModel,
    state: WorkoutUiState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(DateUtils.formatDuration(state.elapsedSeconds),
                    fontSize = 22.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = 1.sp)
                Spacer(Modifier.height(2.dp))
                Text("训练时长", fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
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

        state.cards.forEachIndexed { index, card ->
            ExerciseCard(
                card = card,
                cardIndex = index,
                onAdjustField = { field, delta -> viewModel.adjustField(index, field, delta) },
                onAddSet = { viewModel.addSetToCard(index) },
                onDeleteSet = { setIndex -> viewModel.deleteSet(index, setIndex) },
                onDeleteCard = { viewModel.deleteCard(index) },
                onChangeExercise = { viewModel.showExercisePicker(index) },
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

    // End workout confirmation
    if (state.showEndConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.hideEndConfirm() },
            title = { Text("确定结束本次训练？") },
            confirmButton = {
                TextButton(onClick = { viewModel.endWorkout() }) {
                    Text("确定", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideEndConfirm() }) {
                    Text("继续训练")
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
    val filteredExercises = remember(exercises, searchQuery) {
        if (searchQuery.isBlank()) exercises
        else exercises.filter { it.name.contains(searchQuery, ignoreCase = true) }
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
            }
        },
        text = {
            Column(modifier = Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState())) {
                grouped.forEach { (part, exs) ->
                    Text(part.label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp))
                    exs.forEach { exercise ->
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
                                Text(exercise.bodyPart.label, fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(Modifier.weight(1f))
                            RecordTypeBadge(exercise.recordType)
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
