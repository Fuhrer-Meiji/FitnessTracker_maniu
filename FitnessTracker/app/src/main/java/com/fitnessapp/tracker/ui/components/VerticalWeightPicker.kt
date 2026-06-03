package com.fitnessapp.tracker.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import kotlin.math.abs

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VerticalWeightPicker(
    value: Double,
    onValueChange: (Double) -> Unit,
    step: Double = 2.5,
    label: String = "",
    modifier: Modifier = Modifier
) {
    val values = remember(step) {
        generateSequence(0.0) { String.format("%.1f", it + step).toDouble() }
            .takeWhile { it <= 150.0 }
            .toList()
    }

    WheelPicker(
        values = values.map { String.format("%.1f", it) },
        selectedValue = String.format("%.1f", value),
        label = label,
        onValueSelected = { s -> s.toDoubleOrNull()?.let { onValueChange(it) } },
        modifier = modifier
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RepsWheelPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    label: String = "",
    modifier: Modifier = Modifier
) {
    val values = remember { (1..99).toList() }

    WheelPicker(
        values = values.map { it.toString() },
        selectedValue = value.toString(),
        label = label,
        onValueSelected = { s -> s.toIntOrNull()?.let { onValueChange(it) } },
        modifier = modifier
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WheelPicker(
    values: List<String>,
    selectedValue: String,
    label: String,
    onValueSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val itemHeight = 36.dp
    val visibleCount = 5

    val initialIndex = remember(selectedValue, values) {
        val idx = values.indexOf(selectedValue)
        (if (idx >= 0) idx - visibleCount / 2 else 0).coerceAtLeast(0)
    }

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = initialIndex
    )
    val snapBehavior = rememberSnapFlingBehavior(listState)

    val selectedIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val center = layoutInfo.viewportSize.height / 2
            layoutInfo.visibleItemsInfo.minByOrNull {
                abs(it.offset + it.size / 2 - center)
            }?.index?.coerceIn(0, values.lastIndex) ?: 0
        }
    }

    LaunchedEffect(selectedIndex) {
        if (selectedIndex < values.size) {
            onValueSelected(values[selectedIndex])
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        if (label.isNotEmpty()) {
            Text(label, fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp))
        }

        Box(
            modifier = Modifier
                .width(80.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight)
                    .align(Alignment.Center)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        RoundedCornerShape(6.dp)
                    )
            )

            LazyColumn(
                state = listState,
                flingBehavior = snapBehavior,
                modifier = Modifier.height(itemHeight * visibleCount)
            ) {
                itemsIndexed(values) { index, v ->
                    val isCenter = index == selectedIndex
                    Text(
                        text = v,
                        fontSize = if (isCenter) 22.sp else 15.sp,
                        fontWeight = if (isCenter) FontWeight.Bold else FontWeight.Normal,
                        color = if (isCenter) MaterialTheme.colorScheme.onBackground
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(itemHeight),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
