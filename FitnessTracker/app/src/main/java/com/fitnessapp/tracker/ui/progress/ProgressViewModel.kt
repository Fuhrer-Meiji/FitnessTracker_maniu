package com.fitnessapp.tracker.ui.progress

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fitnessapp.tracker.FitnessApp
import com.fitnessapp.tracker.data.model.Exercise
import com.fitnessapp.tracker.data.model.RecordType
import com.fitnessapp.tracker.data.repository.ExerciseRepository
import com.fitnessapp.tracker.data.repository.WorkoutRepository
import com.fitnessapp.tracker.ui.theme.ThemeManager
import com.fitnessapp.tracker.util.DateUtils
import com.fitnessapp.tracker.util.UnitConverter
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

data class ProgressUiState(
    val weeklyCount: Int = 0,
    val monthlyCount: Int = 0,
    val totalCount: Int = 0,
    val totalDuration: Long = 0,
    val exercises: List<Exercise> = emptyList(),
    val selectedExercise: Exercise? = null,
    val strengthTrendData: List<Pair<String, Double>> = emptyList(),
    val dailyFrequency: Map<Int, Int> = emptyMap(),
    val workoutDates: Set<Long> = emptySet(),
    val currentUnit: String = "kg"
)

class ProgressViewModel(application: Application) : AndroidViewModel(application) {
    private val db = (application as FitnessApp).database
    private val workoutRepo = WorkoutRepository(db.workoutDao())
    private val exerciseRepo = ExerciseRepository(db.exerciseDao())
    private val themeManager = ThemeManager(application)

    private val _state = MutableStateFlow(ProgressUiState())
    val state: StateFlow<ProgressUiState> = _state.asStateFlow()

    init {
        loadStats()
        loadExercises()
        observeUnit()
    }

    private fun observeUnit() {
        viewModelScope.launch {
            themeManager.unit.collect { unit ->
                _state.update { it.copy(currentUnit = unit) }
                loadStrengthTrend()
            }
        }
    }

    private fun loadStats() {
        viewModelScope.launch {
            val cal = Calendar.getInstance()
            val now = System.currentTimeMillis()

            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            val weekStart = DateUtils.getStartOfDay(cal.timeInMillis)
            cal.set(Calendar.DAY_OF_MONTH, 1)
            val monthStart = DateUtils.getStartOfDay(cal.timeInMillis)

            _state.update {
                it.copy(
                    weeklyCount = workoutRepo.getWorkoutCountInRange(weekStart, now),
                    monthlyCount = workoutRepo.getWorkoutCountInRange(monthStart, now),
                    totalCount = workoutRepo.getTotalWorkoutCount(),
                    totalDuration = workoutRepo.getTotalDurationInRange(monthStart, now)
                )
            }

            loadDailyFrequency(monthStart)
        }
    }

    private fun loadExercises() {
        viewModelScope.launch {
            exerciseRepo.getAllExercises().collect { exercises ->
                val strengthExercises = exercises.filter { it.recordType == RecordType.STRENGTH }
                _state.update { it.copy(
                    exercises = strengthExercises,
                    selectedExercise = strengthExercises.firstOrNull()
                )}
                loadStrengthTrend()
            }
        }
    }

    private fun loadDailyFrequency(monthStart: Long) {
        viewModelScope.launch {
            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
            val monthEnd = DateUtils.getEndOfDay(cal.timeInMillis)

            val workouts = workoutRepo.getWorkoutsInRange(monthStart, monthEnd).first()
            val dayCounts = workouts.groupBy { w ->
                val c = Calendar.getInstance()
                c.timeInMillis = w.date
                c.get(Calendar.DAY_OF_MONTH)
            }.mapValues { it.value.size }

            _state.update { it.copy(
                dailyFrequency = dayCounts,
                workoutDates = workouts.map { DateUtils.getStartOfDay(it.date) }.toSet()
            )}
        }
    }

    private fun loadStrengthTrend() {
        val ex = _state.value.selectedExercise ?: return
        viewModelScope.launch {
            val cal = Calendar.getInstance()
            cal.add(Calendar.WEEK_OF_YEAR, -5)
            val start = DateUtils.getStartOfDay(cal.timeInMillis)
            val now = System.currentTimeMillis()
            val unit = _state.value.currentUnit

            val workouts = workoutRepo.getWorkoutsInRange(start, now).first()
            val data = mutableListOf<Pair<String, Double>>()

            for (w in workouts) {
                val sets = workoutRepo.getSetsForExercise(w.id, ex.id)
                val maxWeight = sets.maxOfOrNull { it.weight ?: 0.0 } ?: continue
                data.add(DateUtils.formatDate(w.date) to UnitConverter.displayWeight(maxWeight, unit))
            }

            _state.update { it.copy(strengthTrendData = data) }
        }
    }

    fun selectExercise(exercise: Exercise) {
        _state.update { it.copy(selectedExercise = exercise) }
        loadStrengthTrend()
    }
}
