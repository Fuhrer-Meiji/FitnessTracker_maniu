package com.fitnessapp.tracker.ui.workout.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitnessapp.tracker.data.model.RecordType
import com.fitnessapp.tracker.ui.components.Stepper
import com.fitnessapp.tracker.util.UnitConverter

@Composable
fun RecordPanel(
    recordType: RecordType,
    setNumber: Int,
    weight: Double,
    reps: Int,
    duration: Int,
    currentUnit: String = "kg",
    onWeightChange: (Double) -> Unit,
    onRepsChange: (Int) -> Unit,
    onDurationChange: (Int) -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(10.dp)
            )
            .padding(14.dp)
    ) {
        Text(
            "第 $setNumber 组",
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            textAlign = TextAlign.Center
        )

        when (recordType) {
            RecordType.STRENGTH -> {
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("重量 ($currentUnit)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 5.dp))
                        val displayWeight = UnitConverter.displayWeight(weight, currentUnit)
                        Stepper(
                            value = String.format("%.1f", displayWeight),
                            onDecrement = {
                                val step = if (currentUnit == "lb") 2.5 / UnitConverter.KG_TO_LB else 2.5
                                onWeightChange(weight - step)
                            },
                            onIncrement = {
                                val step = if (currentUnit == "lb") 2.5 / UnitConverter.KG_TO_LB else 2.5
                                onWeightChange(weight + step)
                            },
                            onValueConfirm = { v ->
                                v.toDoubleOrNull()?.let {
                                    val inKg = if (currentUnit == "lb") UnitConverter.lbToKg(it) else it
                                    onWeightChange(inKg)
                                }
                            }
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("次数", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 5.dp))
                        Stepper(
                            value = reps.toString(),
                            onDecrement = { onRepsChange(reps - 1) },
                            onIncrement = { onRepsChange(reps + 1) },
                            onValueConfirm = { v -> v.toIntOrNull()?.let { onRepsChange(it) } }
                        )
                    }
                }
            }
            RecordType.REPS -> {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("次数", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 5.dp))
                    Stepper(
                        value = reps.toString(),
                        onDecrement = { onRepsChange(reps - 1) },
                        onIncrement = { onRepsChange(reps + 1) },
                        onValueConfirm = { v -> v.toIntOrNull()?.let { onRepsChange(it) } }
                    )
                }
            }
            RecordType.DURATION -> {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("时长 (秒)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 5.dp))
                    Stepper(
                        value = "${duration}s",
                        onDecrement = { onDurationChange(duration - 5) },
                        onIncrement = { onDurationChange(duration + 5) },
                        onValueConfirm = { v -> v.removeSuffix("s").toIntOrNull()?.let { onDurationChange(it) } }
                    )
                }
            }
        }

        Button(
            onClick = onComplete,
            modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("完成本组", fontWeight = FontWeight.SemiBold)
        }
    }
}
