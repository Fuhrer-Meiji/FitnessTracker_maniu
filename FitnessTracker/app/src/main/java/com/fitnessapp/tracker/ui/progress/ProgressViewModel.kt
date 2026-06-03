package com.fitnessapp.tracker.ui.progress

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fitnessapp.tracker.FitnessApp
import com.fitnessapp.tracker.data.model.Exercise
import com.fitnessapp.tracker.data.model.RecordType
import com.fitnessapp.tracker.data.model.WorkoutSet
import com.fitnessapp.tracker.data.db.entity.WorkoutSetWithExercise
import com.fitnessapp.tracker.data.db.entity.SetTrendData
import com.fitnessapp.tracker.data.repository.ExerciseRepository
import com.fitnessapp.tracker.data.repository.WorkoutRepository
import com.fitnessapp.tracker.ui.theme.ThemeManager
import com.fitnessapp.tracker.util.DateUtils
import com.fitnessapp.tracker.util.UnitConverter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

enum class TrendType(val label: String) {
    MAX_WEIGHT("最大重量"),
    ESTIMATED_1RM("估算1RM"),
    TOTAL_VOLUME("总容量")
}

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
    val dayWorkouts: List<DayWorkoutDetail> = emptyList(),
    val selectedTrendType: TrendType = TrendType.MAX_WEIGHT,
    val showExercisePicker: Boolean = false
)

class ProgressViewModel(application: Application) : AndroidViewModel(application) {
    private val db = (application as FitnessApp).database
    private val workoutRepo = WorkoutRepository(db.workoutDao())
    private val exerciseRepo = ExerciseRepository(db.exerciseDao())
    private val themeManager = ThemeManager(application)

    private val _state = MutableStateFlow(ProgressUiState())
    val state: StateFlow<ProgressUiState> = _state.asStateFlow()

    private val _selectedExerciseId = MutableStateFlow<Long?>(null)
    private val _selectedTrendType = MutableStateFlow(TrendType.MAX_WEIGHT)

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
            refreshStats()
        }
    }

    private suspend fun refreshStats() {
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

        refreshDailyFrequency(monthStart)
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
            refreshDailyFrequency(monthStart)
        }
    }

    private suspend fun refreshDailyFrequency(monthStart: Long) {
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

    private fun observeTrend() {
        viewModelScope.launch {
            combine(
                _selectedExerciseId,
                _selectedTrendType,
                themeManager.unit,
                workoutRepo.getAllWorkouts()
            ) { exId, trendType, unit, _ ->
                Triple(exId, trendType, unit)
            }.collect { (exId, trendType, unit) ->
                if (exId == null) {
                    _state.update { it.copy(strengthTrendData = emptyList()) }
                    return@collect
                }
                val cal = Calendar.getInstance()
                cal.add(Calendar.WEEK_OF_YEAR, -5)
                val start = DateUtils.getStartOfDay(cal.timeInMillis)

                // Single query fetching all sets trend data
                val setsTrendData = workoutRepo.getSetsForExerciseFromDate(exId, start)
                val data = mutableListOf<Pair<String, Double>>()

                // Group by workout date in-memory
                val groupedByDate = setsTrendData.groupBy { it.date }
                for ((date, sets) in groupedByDate) {
                    if (sets.isEmpty()) continue
                    
                    val value = when (trendType) {
                        TrendType.MAX_WEIGHT -> {
                            val maxWeight = sets.maxOfOrNull { it.weight ?: 0.0 } ?: 0.0
                            UnitConverter.displayWeight(maxWeight, unit)
                        }
                        TrendType.ESTIMATED_1RM -> {
                            val max1RM = sets.maxOfOrNull { 
                                val weight = it.weight ?: 0.0
                                val reps = it.reps ?: 0
                                weight * (1.0 + reps / 30.0)
                            } ?: 0.0
                            UnitConverter.displayWeight(max1RM, unit)
                        }
                        TrendType.TOTAL_VOLUME -> {
                            val totalVolume = sets.sumOf { 
                                val weight = it.weight ?: 0.0
                                val reps = it.reps ?: 1
                                weight * reps
                            }
                            UnitConverter.displayWeight(totalVolume, unit)
                        }
                    }
                    data.add(DateUtils.formatDate(date) to value)
                }
                _state.update { it.copy(strengthTrendData = data) }
            }
        }
    }

    fun selectExercise(exercise: Exercise) {
        _state.update { it.copy(selectedExercise = exercise, showExercisePicker = false) }
        _selectedExerciseId.value = exercise.id
    }

    fun selectTrendType(trendType: TrendType) {
        _selectedTrendType.value = trendType
        _state.update { it.copy(selectedTrendType = trendType) }
    }

    fun showExercisePicker() {
        _state.update { it.copy(showExercisePicker = true) }
    }

    fun hideExercisePicker() {
        _state.update { it.copy(showExercisePicker = false) }
    }

    fun selectDay(dayStart: Long) {
        viewModelScope.launch {
            val dayEnd = dayStart + 86400000
            
            // Single query fetching all sets with exercise names in 1 call
            val joinedList = workoutRepo.getWorkoutSetsWithExerciseByDay(dayStart, dayEnd)
            
            // Group in-memory by workoutId and exerciseName
            val details = joinedList.groupBy { it.workoutId to it.exerciseName }
                .map { (key, sets) ->
                    val (workoutId, exerciseName) = key
                    DayWorkoutDetail(
                        workoutId = workoutId,
                        exerciseName = exerciseName,
                        sets = sets.map { s ->
                            WorkoutSet(
                                id = s.id,
                                workoutId = s.workoutId,
                                exerciseId = s.exerciseId,
                                setNumber = s.setNumber,
                                recordType = RecordType.valueOf(s.recordType),
                                weight = s.weight,
                                reps = s.reps,
                                durationSeconds = s.durationSeconds,
                                restSeconds = s.restSeconds
                            )
                        }
                    )
                }
            _state.update { it.copy(selectedDay = dayStart, dayWorkouts = details) }
        }
    }

    fun deleteSetFromWorkout(set: com.fitnessapp.tracker.data.model.WorkoutSet) {
        viewModelScope.launch(Dispatchers.IO) {
            workoutRepo.deleteSet(set)
            refreshStats()
            _state.value.selectedDay?.let { selectDay(it) }
        }
    }

    fun deleteWorkout(workoutId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            workoutRepo.deleteWorkoutById(workoutId)
            refreshStats()
            _state.value.selectedDay?.let { selectDay(it) }
        }
    }
}
