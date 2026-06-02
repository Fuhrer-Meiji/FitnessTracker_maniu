package com.fitnessapp.tracker.ui.settings.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitnessapp.tracker.data.model.BodyMetric
import com.fitnessapp.tracker.util.DateUtils

@Composable
fun BodyMetricsScreen(
    metrics: List<BodyMetric>,
    currentUnit: String,
    onBack: () -> Unit,
    onAddMetric: (Double?, Double?) -> Unit,
    onDeleteMetric: (BodyMetric) -> Unit
) {
    var weightText by remember { mutableStateOf("") }
    var bodyFatText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) { Text("← 返回") }
            Spacer(Modifier.weight(1f))
            Text("身体数据", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.weight(1f))
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("添加记录", fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = weightText,
                                onValueChange = { weightText = it },
                                label = { Text("体重 ($currentUnit)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                            OutlinedTextField(
                                value = bodyFatText,
                                onValueChange = { bodyFatText = it },
                                label = { Text("体脂率 (%)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = {
                                val w = weightText.toDoubleOrNull()
                                val bf = bodyFatText.toDoubleOrNull()
                                if (w != null || bf != null) {
                                    onAddMetric(w, bf)
                                    weightText = ""
                                    bodyFatText = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            enabled = weightText.isNotBlank() || bodyFatText.isNotBlank()
                        ) {
                            Text("保存")
                        }
                    }
                }
            }

            if (metrics.isEmpty()) {
                item {
                    Text("暂无记录", color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 24.dp),
                        style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                items(metrics, key = { it.id }) { metric ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(DateUtils.formatDate(metric.date), fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    metric.weight?.let {
                                        Text("体重: ${String.format("%.1f", it)} $currentUnit",
                                            fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                    metric.bodyFat?.let {
                                        Text("体脂: ${String.format("%.1f", it)}%",
                                            fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                            TextButton(
                                onClick = { onDeleteMetric(metric) },
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
