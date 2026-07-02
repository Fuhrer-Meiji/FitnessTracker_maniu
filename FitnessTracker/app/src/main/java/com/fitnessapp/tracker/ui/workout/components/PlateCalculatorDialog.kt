package com.fitnessapp.tracker.ui.workout.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitnessapp.tracker.ui.theme.LocalThemeColors

@Composable
fun PlateCalculatorDialog(
    initialWeight: Double,
    currentUnit: String = "kg",
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var targetWeightInput by remember { mutableStateOf(initialWeight.toString()) }
    var barWeightSelection by remember { mutableStateOf(20.0) } // Default 20kg Olympic bar
    
    val targetWeight = targetWeightInput.toDoubleOrNull() ?: 0.0
    val weightPerSide = ((targetWeight - barWeightSelection) / 2.0).coerceAtLeast(0.0)
    
    // Standard plates configuration
    val availablePlates = listOf(25.0, 20.0, 15.0, 10.0, 5.0, 2.5, 1.25)
    
    val platesList = remember(weightPerSide) {
        var remaining = weightPerSide
        val result = mutableListOf<Double>()
        for (plate in availablePlates) {
            while (remaining >= plate - 0.001) {
                result.add(plate)
                remaining -= plate
            }
        }
        result
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("杠铃盘片计算器", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "计算每侧所需挂载的杠片 (单侧所需: ${String.format("%.2f", weightPerSide)} $currentUnit)",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(Modifier.height(12.dp))
                
                // Target weight input
                OutlinedTextField(
                    value = targetWeightInput,
                    onValueChange = { targetWeightInput = it },
                    label = { Text("目标重量 ($currentUnit)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(Modifier.height(10.dp))
                
                // Bar weight selection
                Text("杠铃杆重量", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Start).padding(bottom = 6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(20.0, 15.0, 10.0, 0.0).forEach { weight ->
                        val label = if (weight == 0.0) "无杆" else "${weight.toInt()} kg"
                        FilterChip(
                            selected = barWeightSelection == weight,
                            onClick = { barWeightSelection = weight },
                            label = { Text(label, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                Spacer(Modifier.height(20.dp))
                
                // Barbell Visualization
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Collar / Sleeve stopper (Grey chunk)
                        Box(
                            modifier = Modifier
                                .size(width = 10.dp, height = 70.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.Gray)
                        )
                        
                        // Sleeve (Bar center line extension)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(IntrinsicSize.Max),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            // The sleeve pipe
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(20.dp)
                                    .background(Color.LightGray)
                            )
                            
                            // Stacked Plates on the sleeve
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 2.dp)
                            ) {
                                platesList.forEach { plate ->
                                    PlateBar(plate)
                                }
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                // Plate Breakdown list
                if (platesList.isEmpty()) {
                    Text("无需挂载杠片 (低于空杆重量)", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                } else {
                    Text("每侧挂载明细:", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.align(Alignment.Start))
                    Spacer(Modifier.height(4.dp))
                    
                    val groupedPlates = platesList.groupBy { it }
                    groupedPlates.forEach { (plate, list) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(getPlateColor(plate))
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("${plate} $currentUnit 杠片", fontSize = 12.sp)
                            }
                            Text("x ${list.size}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(targetWeight) },
                enabled = targetWeight > 0.0
            ) {
                Text("应用重量")
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
private fun PlateBar(weight: Double) {
    val height = when (weight) {
        25.0 -> 90.dp
        20.0 -> 82.dp
        15.0 -> 74.dp
        10.0 -> 66.dp
        5.0 -> 54.dp
        2.5 -> 44.dp
        1.25 -> 36.dp
        else -> 30.dp
    }
    
    val width = when (weight) {
        25.0 -> 16.dp
        20.0 -> 15.dp
        15.0 -> 14.dp
        10.0 -> 12.dp
        5.0 -> 10.dp
        2.5 -> 8.dp
        1.25 -> 6.dp
        else -> 6.dp
    }
    
    val color = getPlateColor(weight)
    
    Box(
        modifier = Modifier
            .size(width = width, height = height)
            .clip(RoundedCornerShape(2.dp))
            .background(color)
            .border(0.5.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(2.dp)),
        contentAlignment = Alignment.Center
    ) {
        // Center line hole indicator
        Box(
            modifier = Modifier
                .size(width = width, height = 20.dp)
                .background(Color.Black.copy(alpha = 0.15f))
        )
    }
}

private fun getPlateColor(weight: Double): Color {
    return when (weight) {
        25.0 -> Color(0xFFDC2626) // Red
        20.0 -> Color(0xFF2563EB) // Blue
        15.0 -> Color(0xFFFBBF24) // Yellow
        10.0 -> Color(0xFF16A34A) // Green
        5.0 -> Color(0xFFE5E7EB)  // Grey/White
        2.5 -> Color(0xFF1F2937)  // Dark Grey
        1.25 -> Color(0xFF9CA3AF) // Silver/Light Grey
        else -> Color.Gray
    }
}
