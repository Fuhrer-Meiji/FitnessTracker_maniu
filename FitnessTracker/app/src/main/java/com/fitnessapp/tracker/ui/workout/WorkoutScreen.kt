package com.fitnessapp.tracker.ui.workout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fitnessapp.tracker.ui.theme.LocalThemeColors
import com.fitnessapp.tracker.ui.theme.ThemeManager
import com.fitnessapp.tracker.util.DateUtils

private val MOTIVATIONAL_QUOTES = listOf(
    "汗水是脂肪的眼泪，坚持是蜕变的钥匙。",
    "今天你流下的汗水，都会成为明天坚实的铠甲。",
    "健身不是为了超越别人，而是为了成为更好的自己。",
    "自律给你自由，每一组都是对自我的重塑。",
    "打败你的不是重力，而是你心中的退缩。",
    "每一次力竭，都是身体在发出变强的信号。",
    "别在最能吃苦的年纪，选择安逸和软弱。",
    "雕刻线条，磨炼意志，今天也要全力以赴！",
    "既然已经开始，那就请坚持到最后。",
    "只要你想开始，任何时候都不晚。"
)

@Composable
fun WorkoutScreen(
    viewModel: WorkoutViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.checkForDraft() }

    if (state.isRecording) {
        RecordingScreen(viewModel = viewModel, state = state)
    } else {
        WorkoutHomeScreen(state = state, onStartWorkout = { viewModel.startWorkout() })
    }
}

@Composable
private fun WorkoutHomeScreen(
    state: WorkoutUiState,
    onStartWorkout: () -> Unit
) {
    val themeColors = LocalThemeColors.current
    val context = LocalContext.current
    val themeManager = remember { ThemeManager(context) }
    val bgImageEnabled by themeManager.bgImageEnabled.collectAsState(initial = false)
    val randomQuote = remember { MOTIVATIONAL_QUOTES.random() }

    val backgroundBrush = remember(bgImageEnabled, themeColors) {
        if (bgImageEnabled) {
            Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Transparent))
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF0F0F13),
                    themeColors.primary.copy(alpha = 0.12f),
                    Color(0xFF0F0F13)
                )
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("训练", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "${DateUtils.formatDay(System.currentTimeMillis())} · 本周 ${state.recentWorkouts.size} 次",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.weight(0.5f))

            Card(
                onClick = onStartWorkout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(32.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.05f)
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        // Floating glowing icon
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .background(themeColors.primary.copy(alpha = 0.15f), CircleShape)
                                .border(1.5.dp, themeColors.primary.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(themeColors.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "开始",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        Text(
                            "开始今日训练",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            "START WORKOUT NOW",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.4f),
                            letterSpacing = 1.sp
                        )

                        Spacer(Modifier.height(28.dp))

                        Divider(
                            color = Color.White.copy(alpha = 0.1f),
                            modifier = Modifier.width(140.dp)
                        )

                        Spacer(Modifier.height(18.dp))

                        Text(
                            text = "“ $randomQuote ”",
                            fontSize = 12.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1.0f))
        }
    }
}
