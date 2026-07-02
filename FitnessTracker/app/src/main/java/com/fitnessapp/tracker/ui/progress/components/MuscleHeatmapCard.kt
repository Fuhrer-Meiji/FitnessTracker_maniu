package com.fitnessapp.tracker.ui.progress.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitnessapp.tracker.data.model.BodyPart
import com.fitnessapp.tracker.ui.theme.LocalThemeColors
import androidx.compose.foundation.Canvas

@Composable
fun MuscleHeatmapCard(
    bodyPartSetsCount: Map<BodyPart, Int>,
    modifier: Modifier = Modifier
) {
    val themeColors = LocalThemeColors.current
    val borderGradient = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.18f),
            themeColors.primary.copy(alpha = 0.35f),
            Color.White.copy(alpha = 0.05f)
        )
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, borderGradient)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(18.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                )
                Spacer(Modifier.width(8.dp))
                Text("肌肉锻炼分布 (近7天组数)", style = MaterialTheme.typography.titleMedium)
            }

            // Body Visualization Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Front Body Card
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("正面", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Canvas(modifier = Modifier.size(width = 110.dp, height = 170.dp)) {
                        drawHumanFigure(isFront = true, counts = bodyPartSetsCount, themeColors = themeColors)
                    }
                }

                // Divider line
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight(0.8f)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                // Back Body Card
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("背面", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Canvas(modifier = Modifier.size(width = 110.dp, height = 170.dp)) {
                        drawHumanFigure(isFront = false, counts = bodyPartSetsCount, themeColors = themeColors)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Text Legend grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val leftParts = listOf(BodyPart.CHEST, BodyPart.SHOULDERS, BodyPart.ARMS)
                val rightParts = listOf(BodyPart.BACK, BodyPart.LEGS, BodyPart.CORE)
                
                Column(modifier = Modifier.weight(1f)) {
                    leftParts.forEach { part ->
                        val count = bodyPartSetsCount[part] ?: 0
                        LegendItem(part.label, count, getPartColor(count, themeColors.primary))
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    rightParts.forEach { part ->
                        val count = bodyPartSetsCount[part] ?: 0
                        LegendItem(part.label, count, getPartColor(count, themeColors.primary))
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(label: String, count: Int, color: Color) {
    Row(
        modifier = Modifier.padding(vertical = 3.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "$label: ",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "$count 组",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun DrawScope.drawHumanFigure(
    isFront: Boolean,
    counts: Map<BodyPart, Int>,
    themeColors: com.fitnessapp.tracker.ui.theme.ThemeColors
) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    
    // Draw Head
    drawCircle(
        color = Color.Gray.copy(alpha = 0.3f),
        radius = 12f,
        center = Offset(cx, cy - 60f)
    )

    // Delts (Shoulders) - Left/Right
    val shoulderColor = getPartColor(counts[BodyPart.SHOULDERS] ?: 0, themeColors.primary)
    drawRoundRect(
        color = shoulderColor,
        topLeft = Offset(cx - 32f, cy - 43f),
        size = Size(14f, 18f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
    )
    drawRoundRect(
        color = shoulderColor,
        topLeft = Offset(cx + 18f, cy - 43f),
        size = Size(14f, 18f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
    )

    if (isFront) {
        // CHEST (Chest)
        val chestColor = getPartColor(counts[BodyPart.CHEST] ?: 0, themeColors.primary)
        drawRoundRect(
            color = chestColor,
            topLeft = Offset(cx - 17f, cy - 40f),
            size = Size(16f, 18f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
        )
        drawRoundRect(
            color = chestColor,
            topLeft = Offset(cx + 1f, cy - 40f),
            size = Size(16f, 18f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
        )

        // CORE (Abs)
        val coreColor = getPartColor(counts[BodyPart.CORE] ?: 0, themeColors.primary)
        drawRoundRect(
            color = coreColor,
            topLeft = Offset(cx - 13f, cy - 20f),
            size = Size(26f, 32f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
        )

        // Biceps (Arms)
        val armColor = getPartColor(counts[BodyPart.ARMS] ?: 0, themeColors.primary)
        drawRoundRect(
            color = armColor,
            topLeft = Offset(cx - 31f, cy - 23f),
            size = Size(10f, 22f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
        )
        drawRoundRect(
            color = armColor,
            topLeft = Offset(cx + 21f, cy - 23f),
            size = Size(10f, 22f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
        )
        
        // Forearms (Arms)
        drawRoundRect(
            color = armColor,
            topLeft = Offset(cx - 29f, cy + 1f),
            size = Size(8f, 22f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
        )
        drawRoundRect(
            color = armColor,
            topLeft = Offset(cx + 21f, cy + 1f),
            size = Size(8f, 22f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
        )

        // Quads (Legs)
        val legColor = getPartColor(counts[BodyPart.LEGS] ?: 0, themeColors.primary)
        drawRoundRect(
            color = legColor,
            topLeft = Offset(cx - 15f, cy + 15f),
            size = Size(13f, 38f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
        )
        drawRoundRect(
            color = legColor,
            topLeft = Offset(cx + 2f, cy + 15f),
            size = Size(13f, 38f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
        )
        
        // Front Calves (Legs)
        drawRoundRect(
            color = legColor,
            topLeft = Offset(cx - 13f, cy + 56f),
            size = Size(10f, 32f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
        )
        drawRoundRect(
            color = legColor,
            topLeft = Offset(cx + 3f, cy + 56f),
            size = Size(10f, 32f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
        )
    } else {
        // BACK (Upper/Lats)
        val backColor = getPartColor(counts[BodyPart.BACK] ?: 0, themeColors.primary)
        // Upper back/Traps
        drawRoundRect(
            color = backColor,
            topLeft = Offset(cx - 18f, cy - 40f),
            size = Size(36f, 16f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
        )
        // Lats
        drawRoundRect(
            color = backColor,
            topLeft = Offset(cx - 16f, cy - 22f),
            size = Size(32f, 18f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
        )
        // Lower Back
        drawRoundRect(
            color = backColor,
            topLeft = Offset(cx - 10f, cy - 2f),
            size = Size(20f, 14f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
        )

        // Triceps (Arms)
        val armColor = getPartColor(counts[BodyPart.ARMS] ?: 0, themeColors.primary)
        drawRoundRect(
            color = armColor,
            topLeft = Offset(cx - 31f, cy - 23f),
            size = Size(10f, 22f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
        )
        drawRoundRect(
            color = armColor,
            topLeft = Offset(cx + 21f, cy - 23f),
            size = Size(10f, 22f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
        )
        
        // Forearms (Arms)
        drawRoundRect(
            color = armColor,
            topLeft = Offset(cx - 29f, cy + 1f),
            size = Size(8f, 22f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
        )
        drawRoundRect(
            color = armColor,
            topLeft = Offset(cx + 21f, cy + 1f),
            size = Size(8f, 22f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
        )

        // Glutes/Hamstrings (Legs)
        val legColor = getPartColor(counts[BodyPart.LEGS] ?: 0, themeColors.primary)
        drawRoundRect(
            color = legColor,
            topLeft = Offset(cx - 15f, cy + 15f),
            size = Size(13f, 38f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
        )
        drawRoundRect(
            color = legColor,
            topLeft = Offset(cx + 2f, cy + 15f),
            size = Size(13f, 38f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
        )
        
        // Calves (Legs)
        drawRoundRect(
            color = legColor,
            topLeft = Offset(cx - 13f, cy + 56f),
            size = Size(10f, 32f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
        )
        drawRoundRect(
            color = legColor,
            topLeft = Offset(cx + 3f, cy + 56f),
            size = Size(10f, 32f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
        )
    }
}

private fun getPartColor(count: Int, primaryColor: Color): Color {
    return when {
        count == 0 -> Color(0xFFE5E7EB).copy(alpha = 0.2f) // Empty: transparent grey
        count <= 3 -> primaryColor.copy(alpha = 0.35f)      // Low volume
        count <= 8 -> primaryColor.copy(alpha = 0.7f)       // Medium volume
        else -> primaryColor                                // High volume
    }
}
