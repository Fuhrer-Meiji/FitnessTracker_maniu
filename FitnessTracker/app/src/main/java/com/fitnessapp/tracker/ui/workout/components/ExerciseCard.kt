package com.fitnessapp.tracker.ui.workout.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitnessapp.tracker.data.model.RecordType
import com.fitnessapp.tracker.ui.workout.ActiveExerciseCard
import com.fitnessapp.tracker.util.UnitConverter

@Composable
fun ExerciseCard(
    card: ActiveExerciseCard,
    cardIndex: Int,
    onAdjustField: (String, Double) -> Unit,
    onAddSet: () -> Unit,
    onDeleteSet: (Int) -> Unit,
    onChangeExercise: ((Int) -> Unit)? = null,
    currentUnit: String = "kg",
    modifier: Modifier = Modifier
) {
    val ex = card.exercise

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(ex.iconName.take(2), fontSize = 18.sp)
                    }
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(ex.name, style = MaterialTheme.typography.titleMedium)
                        Text(ex.bodyPart.label, style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (card.isActive && onChangeExercise != null) {
                    TextButton(
                        onClick = { onChangeExercise(cardIndex) },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("换动作", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
                if (!card.isActive) {
                    Text("✓", color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            if (card.sets.isNotEmpty()) {
                Column(modifier = Modifier.padding(bottom = 6.dp)) {
                    card.sets.forEachIndexed { i, set ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${i + 1}", fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.width(28.dp))
                            when (ex.recordType) {
                                RecordType.STRENGTH -> {
                                    val displayWeight = UnitConverter.displayWeight(set.weight ?: 0.0, currentUnit)
                                    Text("${String.format("%.1f", displayWeight)} $currentUnit", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text("${set.reps} 次", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                                }
                                RecordType.REPS -> {
                                    Text("${set.reps} 次", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Spacer(Modifier.width(28.dp))
                                }
                                RecordType.DURATION -> {
                                    Text("${set.durationSeconds}s", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Spacer(Modifier.width(28.dp))
                                }
                            }
                            if (card.isActive) {
                                Text("✕", fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .clickable { onDeleteSet(i) })
                            }
                        }
                        if (i < card.sets.lastIndex) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                        }
                    }
                }
            }

            if (card.isActive) {
                RecordPanel(
                    recordType = ex.recordType,
                    setNumber = card.setNumber,
                    weight = card.currentWeight,
                    reps = card.currentReps,
                    duration = card.currentDuration,
                    currentUnit = currentUnit,
                    onWeightChange = { newWeight -> onAdjustField("weight", newWeight - card.currentWeight) },
                    onRepsChange = { newReps -> onAdjustField("reps", (newReps - card.currentReps).toDouble()) },
                    onDurationChange = { newDuration -> onAdjustField("duration", (newDuration - card.currentDuration).toDouble()) },
                    onComplete = onAddSet
                )
            } else if (card.sets.isEmpty()) {
                Text("未记录", fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    textAlign = TextAlign.Center)
            }
        }
    }
}
