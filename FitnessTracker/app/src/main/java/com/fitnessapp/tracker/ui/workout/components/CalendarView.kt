package com.fitnessapp.tracker.ui.workout.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.*

private data class YearMonth(val year: Int, val month: Int)

@Composable
fun CalendarView(
    workoutDates: Set<Long>,
    dailyFrequency: Map<Int, Int> = emptyMap(),
    modifier: Modifier = Modifier
) {
    val today = Calendar.getInstance(Locale.CHINESE)
    val initial = YearMonth(today.get(Calendar.YEAR), today.get(Calendar.MONTH))
    var current by remember { mutableStateOf(initial) }

    val calendar = remember(current) {
        Calendar.getInstance(Locale.CHINESE).apply {
            set(Calendar.YEAR, current.year)
            set(Calendar.MONTH, current.month)
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    val dayOffset = (firstDayOfWeek - Calendar.MONDAY + 7) % 7
    val maxFreq = dailyFrequency.values.maxOrNull() ?: 1

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${current.year} 年 ${current.month + 1} 月",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("<", color = MaterialTheme.colorScheme.primary, fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable {
                            val m = current.month - 1
                            current = if (m < 0) YearMonth(current.year - 1, 11) else YearMonth(current.year, m)
                        }
                        .padding(8.dp))
                Spacer(Modifier.width(4.dp))
                Text("今天", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { current = YearMonth(today.get(Calendar.YEAR), today.get(Calendar.MONTH)) }
                        .padding(horizontal = 8.dp, vertical = 6.dp))
                Spacer(Modifier.width(4.dp))
                Text(">", color = MaterialTheme.colorScheme.primary, fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable {
                            val m = current.month + 1
                            current = if (m > 11) YearMonth(current.year + 1, 0) else YearMonth(current.year, m)
                        }
                        .padding(8.dp))
            }
        }

        val dayLabels = listOf("一", "二", "三", "四", "五", "六", "日")
        Row(modifier = Modifier.fillMaxWidth()) {
            dayLabels.forEach { label ->
                Text(label, modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                    fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(4.dp))

        val totalCells = dayOffset + daysInMonth
        val rows = (totalCells + 6) / 7

        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    val day = cellIndex - dayOffset + 1
                    val isInMonth = day in 1..daysInMonth

                    Box(modifier = Modifier.weight(1f).aspectRatio(1f), contentAlignment = Alignment.Center) {
                        if (isInMonth) {
                            val isToday = today.get(Calendar.YEAR) == current.year &&
                                    today.get(Calendar.MONTH) == current.month &&
                                    today.get(Calendar.DAY_OF_MONTH) == day
                            val freq = dailyFrequency[day] ?: 0
                            val hasWorkout = freq > 0

                            val bgAlpha = if (hasWorkout) (freq.toFloat() / maxFreq).coerceIn(0.15f, 0.9f) else 0f

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (isToday) MaterialTheme.colorScheme.primary
                                        else if (hasWorkout) MaterialTheme.colorScheme.primary.copy(alpha = bgAlpha)
                                        else androidx.compose.ui.graphics.Color.Transparent
                                    )
                            ) {
                                Text(
                                    day.toString(),
                                    fontSize = 12.sp,
                                    fontWeight = if (hasWorkout) FontWeight.SemiBold else FontWeight.Normal,
                                    color = when {
                                        isToday -> MaterialTheme.colorScheme.onPrimary
                                        hasWorkout -> MaterialTheme.colorScheme.surface
                                        else -> MaterialTheme.colorScheme.onBackground
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Legend
        if (maxFreq > 0) {
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("少", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(4.dp))
                listOf(0.15f, 0.3f, 0.5f, 0.75f, 1.0f).forEach { intensity ->
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .padding(1.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = intensity))
                    )
                    Spacer(Modifier.width(2.dp))
                }
                Spacer(Modifier.width(4.dp))
                Text("多", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
