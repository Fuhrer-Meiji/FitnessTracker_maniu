package com.fitnessapp.tracker.ui.workout.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitnessapp.tracker.data.model.RecordType
import com.fitnessapp.tracker.ui.theme.LocalThemeColors
import com.fitnessapp.tracker.ui.workout.ActiveExerciseCard
import com.fitnessapp.tracker.util.UnitConverter

@Composable
fun ExerciseCard(
    card: ActiveExerciseCard,
    cardIndex: Int,
    onAdjustField: (String, Double) -> Unit,
    onAddSet: () -> Unit,
    onDeleteSet: (Int) -> Unit,
    onDeleteCard: (() -> Unit)? = null,
    onChangeExercise: ((Int) -> Unit)? = null,
    onLinkSuperset: (() -> Unit)? = null,
    onUnlinkSuperset: (() -> Unit)? = null,
    onActivate: (() -> Unit)? = null,
    supersetLabel: String? = null,
    supersetColor: Color? = null,
    currentUnit: String = "kg",
    modifier: Modifier = Modifier
) {
    val ex = card.exercise

    val themeColors = LocalThemeColors.current
    val borderGradient = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.18f),
            themeColors.primary.copy(alpha = 0.35f),
            Color.White.copy(alpha = 0.05f)
        )
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (!card.isActive && onActivate != null) {
                    Modifier.clickable { onActivate() }
                } else {
                    Modifier
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (supersetColor != null) supersetColor.copy(alpha = 0.04f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = if (supersetColor != null) 1.5.dp else 0.5.dp,
            brush = if (supersetColor != null) Brush.sweepGradient(listOf(supersetColor, supersetColor)) else borderGradient
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max)
        ) {
            if (supersetColor != null) {
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .fillMaxHeight()
                        .background(supersetColor)
                )
            }
            Column(modifier = Modifier.padding(16.dp).weight(1f)) {
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(ex.name, style = MaterialTheme.typography.titleMedium)
                                if (supersetLabel != null && supersetColor != null) {
                                    Spacer(Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = supersetColor.copy(alpha = 0.15f),
                                        border = BorderStroke(0.5.dp, supersetColor)
                                    ) {
                                        Text(
                                            text = supersetLabel,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = supersetColor,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(ex.bodyPart.label, style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    if (card.isActive && onChangeExercise != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            var showMenu by remember { mutableStateOf(false) }

                            TextButton(
                                onClick = { onDeleteCard?.invoke() },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("删除", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                            }
                            TextButton(
                                onClick = { onChangeExercise(cardIndex) },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("换动作", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            }

                            if (onLinkSuperset != null || onUnlinkSuperset != null) {
                                Box {
                                    TextButton(
                                        onClick = { showMenu = true },
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("超级组 ▾", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                    }
                                    DropdownMenu(
                                        expanded = showMenu,
                                        onDismissRequest = { showMenu = false }
                                    ) {
                                        if (onLinkSuperset != null) {
                                            DropdownMenuItem(
                                                text = { Text("与下方动作组合", fontSize = 13.sp) },
                                                onClick = {
                                                    onLinkSuperset()
                                                    showMenu = false
                                                }
                                            )
                                        }
                                        if (onUnlinkSuperset != null) {
                                            DropdownMenuItem(
                                                text = { Text("解除超级组", fontSize = 13.sp) },
                                                onClick = {
                                                    onUnlinkSuperset()
                                                    showMenu = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
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

                                Row(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.Start,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    when (ex.recordType) {
                                        RecordType.STRENGTH -> {
                                            val displayWeight = UnitConverter.displayWeight(set.weight ?: 0.0, currentUnit)
                                            Text(
                                                "${String.format("%.1f", displayWeight)} $currentUnit",
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp,
                                                modifier = Modifier.padding(end = 16.dp)
                                            )
                                            Text(
                                                "${set.reps} 次",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 13.sp
                                            )
                                        }
                                        RecordType.REPS -> {
                                            Text(
                                                "${set.reps} 次",
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp
                                            )
                                        }
                                        RecordType.DURATION -> {
                                            Text(
                                                "${set.durationSeconds}s",
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp
                                            )
                                        }
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
                        historicalMaxSet = card.historicalMaxSet,
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
}
