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
import com.fitnessapp.tracker.ui.components.RepsWheelPicker
import com.fitnessapp.tracker.ui.components.Stepper
import com.fitnessapp.tracker.ui.components.VerticalWeightPicker
import com.fitnessapp.tracker.util.UnitConverter

import com.fitnessapp.tracker.data.model.WorkoutSet

@Composable
fun RecordPanel(
    recordType: RecordType,
    setNumber: Int,
    weight: Double,
    reps: Int,
    duration: Int,
    currentUnit: String = "kg",
    historicalMaxSet: WorkoutSet? = null,
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
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            textAlign = TextAlign.Center
        )

        // Calculate progressive recommendation
        val recommendation = remember(recordType, setNumber, historicalMaxSet, currentUnit) {
            if (historicalMaxSet == null) {
                "暂无推荐"
            } else {
                val phase = when (setNumber) {
                    1 -> "热身组"
                    2 -> "过渡组"
                    3 -> "正式组"
                    else -> "超载组"
                }
                val pct = when (setNumber) {
                    1 -> 0.60
                    2 -> 0.80
                    3 -> 1.00
                    else -> 1.025
                }
                
                when (recordType) {
                    RecordType.STRENGTH -> {
                        val maxW = historicalMaxSet.weight ?: 60.0
                        val recW = kotlin.math.round((maxW * pct) * 2) / 2.0
                        val displayRecW = UnitConverter.displayWeight(recW, currentUnit)
                        val recR = when (setNumber) {
                            1 -> 12
                            2 -> 10
                            3 -> 8
                            else -> 6
                        }
                        "$phase (${(pct * 100).toInt()}%): ${String.format("%.1f", displayRecW)} $currentUnit * $recR 次"
                    }
                    RecordType.REPS -> {
                        val maxR = (historicalMaxSet.reps ?: 10).toDouble()
                        val recR = kotlin.math.max(1, kotlin.math.round(maxR * pct).toInt())
                        "$phase (${(pct * 100).toInt()}%): $recR 次"
                    }
                    RecordType.DURATION -> {
                        val maxD = (historicalMaxSet.durationSeconds ?: 30).toDouble()
                        val recD = kotlin.math.max(5, (kotlin.math.round((maxD * pct) / 5.0) * 5).toInt())
                        "$phase (${(pct * 100).toInt()}%): ${recD}s"
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(6.dp)
                )
                .padding(vertical = 6.dp, horizontal = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "💡 推荐: $recommendation",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
        }

        when (recordType) {
            RecordType.STRENGTH -> {
                var showPlateCalculator by remember { mutableStateOf(false) }

                if (showPlateCalculator) {
                    PlateCalculatorDialog(
                        initialWeight = weight,
                        currentUnit = currentUnit,
                        onConfirm = { calculatedWeight ->
                            onWeightChange(calculatedWeight)
                            showPlateCalculator = false
                        },
                        onDismiss = { showPlateCalculator = false }
                    )
                }

                val displayWeight = UnitConverter.displayWeight(weight, currentUnit)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        VerticalWeightPicker(
                            value = displayWeight,
                            onValueChange = { newDisplayWeight ->
                                val inKg = if (currentUnit == "lb") UnitConverter.lbToKg(newDisplayWeight) else newDisplayWeight
                                onWeightChange(inKg)
                            },
                            label = "重量 ($currentUnit)"
                        )
                        Spacer(Modifier.height(4.dp))
                        TextButton(
                            onClick = { showPlateCalculator = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(24.dp)
                        ) {
                            Text("盘片计算", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Spacer(Modifier.width(32.dp))
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        RepsWheelPicker(
                            value = reps,
                            onValueChange = { onRepsChange(it) },
                            label = "次数"
                        )
                        Spacer(Modifier.height(28.dp))
                    }
                }
            }
            RecordType.REPS -> {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    RepsWheelPicker(
                        value = reps,
                        onValueChange = { onRepsChange(it) },
                        label = "次数"
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
