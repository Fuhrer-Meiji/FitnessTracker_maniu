package com.fitnessapp.tracker.ui.progress

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fitnessapp.tracker.FitnessApp
import com.fitnessapp.tracker.data.model.Exercise
import com.fitnessapp.tracker.data.model.RecordType
import com.fitnessapp.tracker.data.model.WorkoutSet
import com.fitnessapp.tracker.data.repository.ExerciseRepository
import com.fitnessapp.tracker.data.repository.WorkoutRepository
import com.fitnessapp.tracker.ui.theme.ThemeManager
import com.fitnessapp.tracker.util.DateUtils
import com.fitnessapp.tracker.util.UnitConverter
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

data class DayWorkoutDetail(
    val workoutId: Long,
    val exerciseName: String,
    val sets: List<WorkoutSet>
)

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
    val currentUnit: String = "kg",
    val selectedDay: Long? = null,
    val dayWorkouts: List<DayWorkoutDetail> = emptyList()
)

class ProgressViewModel(application: Application) : AndroidViewModel(application) {
    private val db = (application as FitnessApp).database
    private val workoutRepo = WorkoutRepository(db.workoutDao())
    private val exerciseRepo = ExerciseRepository(db.exerciseDao())
    private val themeManager = ThemeManager(application)

    private val _state = MutableStateFlow(ProgressUiState())
    val state: StateFlow<ProgressUiState> = _state.asStateFlow()

    private val _selectedExerciseId = MutableStateFlow<Long?>(null)

    init {
        loadStats()
        observeExercises()
        observeUnit()
        observeTrend()
    }

    private fun observeUnit() {
        viewModelScope.launch {
            themeManager.unit.collect { unit ->
                _state.update { it.copy(currentUnit = unit) }
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

    private fun observeExercises() {
        viewModelScope.launch {
            exerciseRepo.getAllExercises().collect { exercises ->
                val strengthExercises = exercises.filter { it.recordType == RecordType.STRENGTH }
                val currentSelected = _state.value.selectedExercise
                val newSelected = if (currentSelected in strengthExercises) currentSelected
                    else strengthExercises.firstOrNull()
                _state.update { it.copy(exercises = strengthExercises, selectedExercise = newSelected) }
                _selectedExerciseId.value = newSelected?.id
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

    private fun observeTrend() {
        viewModelScope.launch {
            combine(
                _selectedExerciseId,
                themeManager.unit,
                workoutRepo.getAllWorkouts()
            ) { exId, unit, _ ->
                exId to unit
            }.collect { (exId, unit) ->
                if (exId == null) {
                    _state.update { it.copy(strengthTrendData = emptyList()) }
                    return@collect
                }
                val cal = Calendar.getInstance()
                cal.add(Calendar.WEEK_OF_YEAR, -5)
                val start = DateUtils.getStartOfDay(cal.timeInMillis)

                val workouts = workoutRepo.getWorkoutsInRange(start, System.currentTimeMillis()).first()
                val data = mutableListOf<Pair<String, Double>>()
                for (w in workouts) {
                    val sets = workoutRepo.getSetsForExercise(w.id, exId)
                    val maxWeight = sets.maxOfOrNull { it.weight ?: 0.0 } ?: continue
                    data.add(DateUtils.formatDate(w.date) to UnitConverter.displayWeight(maxWeight, unit))
                }
                _state.update { it.copy(strengthTrendData = data) }
            }
        }
    }

    fun selectExercise(exercise: Exercise) {
        _state.update { it.copy(selectedExercise = exercise) }
        _selectedExerciseId.value = exercise.id
    }

    fun selectDay(dayStart: Long) {
        viewModelScope.launch {
            val dayEnd = dayStart + 86400000
            val workouts = workoutRepo.getWorkoutsByDay(dayStart, dayEnd)
            val details = mutableListOf<DayWorkoutDetail>()
            for (w in workouts) {
                val sets = workoutRepo.getSetsForWorkout(w.id)
                val setsByExercise = sets.groupBy { it.exerciseId }
                for ((exId, exSets) in setsByExercise) {
                    val ex = exerciseRepo.getExerciseById(exId)
                    details.add(DayWorkoutDetail(
                        workoutId = w.id,
                        exerciseName = ex?.name ?: "未知",
                        sets = exSets
                    ))
                }
            }
            _state.update { it.copy(selectedDay = dayStart, dayWorkouts = details) }
        }
    }
}
