package com.fitnessapp.tracker.ui.settings.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitnessapp.tracker.data.model.BodyPart
import com.fitnessapp.tracker.data.model.Exercise
import com.fitnessapp.tracker.data.model.RecordType
import com.fitnessapp.tracker.data.model.Equipment

@Composable
fun ExerciseLibraryScreen(
    exercises: List<Exercise>,
    onBack: () -> Unit,
    onAddExercise: (String, BodyPart, Equipment, RecordType, String) -> Unit,
    onDeleteExercise: (Exercise) -> Unit
) {
    var showAddForm by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var selectedBodyPart by remember { mutableStateOf(BodyPart.CHEST) }
    var selectedEquipment by remember { mutableStateOf(Equipment.BARBELL) }
    var selectedRecordType by remember { mutableStateOf(RecordType.STRENGTH) }
    var iconName by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val grouped = remember(exercises) {
        exercises.groupBy { it.bodyPart }.entries.sortedBy { it.key.ordinal }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) { Text("← 返回") }
            Spacer(Modifier.weight(1f))
            Text("动作库管理", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.weight(1f))
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Button(
                    onClick = { 
                        showAddForm = !showAddForm
                        if (!showAddForm) {
                            errorMessage = null
                            successMessage = null
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(if (showAddForm) "收起表单" else "+ 添加自定义动作")
                }
            }

            if (showAddForm) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("添加自定义动作", fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 12.dp))

                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it; errorMessage = null; successMessage = null },
                                label = { Text("动作名称 *(必填)*") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                isError = errorMessage != null
                            )
                            Spacer(Modifier.height(10.dp))

                            Text("身体部位", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BodyPart.entries.forEach { part ->
                                    FilterChip(
                                        selected = selectedBodyPart == part,
                                        onClick = { selectedBodyPart = part },
                                        label = { Text(part.label, fontSize = 11.sp) }
                                    )
                                }
                            }
                            Spacer(Modifier.height(10.dp))

                            Text("训练器材", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Equipment.entries.forEach { equip ->
                                    FilterChip(
                                        selected = selectedEquipment == equip,
                                        onClick = { selectedEquipment = equip },
                                        label = { Text(equip.label, fontSize = 11.sp) }
                                    )
                                }
                            }
                            Spacer(Modifier.height(10.dp))

                            Text("记录类型", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                RecordType.entries.forEach { rt ->
                                    FilterChip(
                                        selected = selectedRecordType == rt,
                                        onClick = { selectedRecordType = rt },
                                        label = { Text(rt.label, fontSize = 11.sp) }
                                    )
                                }
                            }
                            Spacer(Modifier.height(10.dp))

                            OutlinedTextField(
                                value = iconName,
                                onValueChange = { iconName = it; errorMessage = null; successMessage = null },
                                label = { Text("图标标识 *(选填，如 squat)*") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                            Spacer(Modifier.height(12.dp))

                            if (errorMessage != null) {
                                Text(
                                    text = errorMessage!!,
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                            if (successMessage != null) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                ) {
                                    Text(
                                        text = successMessage!!,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    val finalName = if (name.isNotBlank()) name.trim() else iconName.trim()
                                    if (finalName.isNotBlank()) {
                                        onAddExercise(finalName, selectedBodyPart, selectedEquipment, selectedRecordType,
                                            iconName.ifBlank { finalName.lowercase().replace(" ", "_") })
                                        successMessage = "✅ 动作「$finalName」已成功保存至动作库！"
                                        name = ""
                                        iconName = ""
                                        errorMessage = null
                                    } else {
                                        errorMessage = "⚠️ 请先填写第一行的「动作名称」噢！"
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                enabled = true
                            ) {
                                Text("保存动作")
                            }
                        }
                    }
                }
            }

            grouped.forEach { (bodyPart, exs) ->
                item {
                    Text(bodyPart.label, fontWeight = FontWeight.SemiBold, fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 2.dp))
                }
                items(exs, key = { it.id }) { exercise ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(exercise.iconName.take(2), fontSize = 14.sp)
                                }
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(exercise.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("${exercise.bodyPart.label} · ${exercise.equipment.label}", fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = MaterialTheme.colorScheme.secondaryContainer
                                        ) {
                                            Text(exercise.recordType.label, fontSize = 8.sp,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                        }
                                        if (exercise.isPreset) {
                                            Text("预设", fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }
                            }
                            if (!exercise.isPreset) {
                                TextButton(
                                    onClick = { onDeleteExercise(exercise) },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("删除", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
