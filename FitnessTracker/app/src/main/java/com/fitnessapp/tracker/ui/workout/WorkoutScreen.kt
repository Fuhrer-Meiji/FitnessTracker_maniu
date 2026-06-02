package com.fitnessapp.tracker.ui.workout

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fitnessapp.tracker.util.DateUtils

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
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("训练", style = MaterialTheme.typography.titleLarge)
                Text("${DateUtils.formatDay(System.currentTimeMillis())} · 本周 ${state.recentWorkouts.size} 次",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(Modifier.weight(0.6f))

        Card(
            onClick = onStartWorkout,
            modifier = Modifier.size(200.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("+", fontSize = 36.sp, fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onPrimary)
                Spacer(Modifier.height(8.dp))
                Text("开始训练", fontSize = 18.sp, fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimary)
            }
        }

        Spacer(Modifier.weight(1f))
    }
}
