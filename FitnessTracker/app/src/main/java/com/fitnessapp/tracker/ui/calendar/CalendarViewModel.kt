package com.fitnessapp.tracker.ui.calendar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fitnessapp.tracker.FitnessApp
import com.fitnessapp.tracker.data.model.*
import com.fitnessapp.tracker.data.db.entity.WorkoutSetWithExercise
import com.fitnessapp.tracker.data.repository.BodyMetricRepository
import com.fitnessapp.tracker.data.repository.WorkoutRepository
import com.fitnessapp.tracker.ui.progress.DayWorkoutDetail
import com.fitnessapp.tracker.ui.theme.ThemeManager
import com.fitnessapp.tracker.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

data class CalendarWorkoutBadge(
    val workoutId: Long,
    val note: String?,
    val isCardio: Boolean,
    val mainValue: String, // e.g., "5121" or "60:00"
    val subValue: String   // e.g., "背二头" or "有氧"
)

data class MonthlyAchievement(
    val month: Int, // 1-12
    val workoutCount: Int,
    val calories: Double,
    val volume: Double
)

data class CalendarUiState(
    val currentYear: Int = 0,
    val currentMonth: Int = 0, // 0-11
    val selectedDay: Long? = null,
    val dayWorkouts: List<DayWorkoutDetail> = emptyList(),
    val workoutBadges: Map<Long, List<CalendarWorkoutBadge>> = emptyMap(),
    val totalCount: Int = 0,
    val consecutiveStreak: Int = 0,
    val currentUnit: String = "kg",
    val yearlyAchievements: List<MonthlyAchievement> = emptyList(),
    val yearlyCalories: Double = 0.0,
    val yearlyVolume: Double = 0.0,
    val yearlyWorkoutCount: Int = 0
)

class CalendarViewModel(application: Application) : AndroidViewModel(application) {
    private val db = (application as FitnessApp).database
    private val workoutRepo = WorkoutRepository(db.workoutDao())
    private val bodyMetricRepo = BodyMetricRepository(db.bodyMetricDao())
    private val themeManager = ThemeManager(application)

    private val _currentYear = MutableStateFlow(0)
    private val _currentMonth = MutableStateFlow(0)
    
    private val _state = MutableStateFlow(CalendarUiState())
    val state: StateFlow<CalendarUiState> = _state.asStateFlow()

    init {
        val today = Calendar.getInstance()
        _currentYear.value = today.get(Calendar.YEAR)
        _currentMonth.value = today.get(Calendar.MONTH)

        observeUnit()
        loadCalendarData()
    }

    private fun observeUnit() {
        viewModelScope.launch {
            themeManager.unit.collect { unit ->
                _state.update { it.copy(currentUnit = unit) }
            }
        }
    }

    fun loadCalendarData() {
        viewModelScope.launch {
            updateCalendarData()
        }
    }

    private suspend fun updateCalendarData() {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, _currentYear.value)
        cal.set(Calendar.MONTH, _currentMonth.value)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val monthStart = DateUtils.getStartOfDay(cal.timeInMillis)
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        val monthEnd = DateUtils.getEndOfDay(cal.timeInMillis)

        val workouts = workoutRepo.getAllWorkouts().first() // List of completed workouts
        val allSets = workoutRepo.getAllWorkoutSetsWithExercise()

        // Filter workouts that occur near this month (with margins for calendar cell overlap)
        val startLimit = monthStart - 10 * 86400000L
        val endLimit = monthEnd + 10 * 86400000L
        val visibleWorkouts = workouts.filter { it.date in startLimit..endLimit }

        // Group sets by workoutId and map them to badges
        val badgesMap = mutableMapOf<Long, CalendarWorkoutBadge>()
        for (w in visibleWorkouts) {
            val wSets = allSets.filter { it.workoutId == w.id }
            if (wSets.isEmpty()) continue

            val isCardio = wSets.all { it.recordType == RecordType.DURATION.name } || 
                           wSets.any { it.bodyPart == BodyPart.CARDIO.name }

            val mainValue: String
            val subValue: String

            if (isCardio) {
                val totalSecs = wSets.sumOf { it.durationSeconds ?: 0 }
                val min = totalSecs / 60
                val sec = totalSecs % 60
                mainValue = if (min > 0) String.format("%d:%02d", min, sec) else "${totalSecs}秒"
                subValue = w.note ?: wSets.firstOrNull()?.exerciseName ?: "有氧"
            } else {
                val totalVolume = wSets.sumOf {
                    val weight = it.weight ?: 0.0
                    val reps = it.reps ?: 1
                    weight * reps
                }
                mainValue = if (totalVolume > 0) "${totalVolume.toInt()}" else "${wSets.size}组"
                subValue = w.note ?: wSets.map { it.bodyPart }.distinct()
                    .map {
                        try { BodyPart.valueOf(it).label } catch(e: Exception) { it }
                    }
                    .take(2)
                    .joinToString("+")
            }

            badgesMap[w.id] = CalendarWorkoutBadge(
                workoutId = w.id,
                note = w.note,
                isCardio = isCardio,
                mainValue = mainValue,
                subValue = subValue
            )
        }

        // Group badges by start of day timestamp
        val dailyBadges = visibleWorkouts.groupBy {
            DateUtils.getStartOfDay(it.date)
        }.mapValues { entry ->
            entry.value.mapNotNull { badgesMap[it.id] }
        }

        val streak = calculateStreak(workouts)

        // Calculate Yearly achievements (Jan to Dec for current calendar year)
        val visibleYear = _currentYear.value
        val yearlyWorkouts = workouts.filter { w ->
            val tempCal = Calendar.getInstance()
            tempCal.timeInMillis = w.date
            tempCal.get(Calendar.YEAR) == visibleYear
        }

        val monthlyData = (1..12).map { m ->
            val wInMonth = yearlyWorkouts.filter { w ->
                val tempCal = Calendar.getInstance()
                tempCal.timeInMillis = w.date
                tempCal.get(Calendar.MONTH) == (m - 1)
            }

            var monthCalories = 0.0
            var monthVolume = 0.0

            for (w in wInMonth) {
                val wSets = allSets.filter { it.workoutId == w.id }
                if (wSets.isEmpty()) continue

                val avgMet = if (wSets.isEmpty()) 5.0 else wSets.map { getMetForBodyPart(it.bodyPart) }.average()
                val durationHours = ((w.endTime ?: w.startTime) - w.startTime) / 3600000.0
                val estimatedDurationHours = (wSets.size * 2.0) / 60.0
                val finalDurationHours = maxOf(durationHours, estimatedDurationHours).coerceIn(0.08, 4.0)
                val latestWeight = bodyMetricRepo.getLatestMetric()?.weight ?: 70.0
                
                monthCalories += if (wSets.isEmpty()) 0.0 else avgMet * latestWeight * finalDurationHours
                monthVolume += wSets.sumOf {
                    val weight = it.weight ?: 0.0
                    val reps = it.reps ?: 1
                    weight * reps
                }
            }

            MonthlyAchievement(
                month = m,
                workoutCount = wInMonth.size,
                calories = monthCalories,
                volume = monthVolume
            )
        }

        val yCalories = monthlyData.sumOf { it.calories }
        val yVolume = monthlyData.sumOf { it.volume }

        _state.update {
            it.copy(
                currentYear = _currentYear.value,
                currentMonth = _currentMonth.value,
                workoutBadges = dailyBadges,
                totalCount = workouts.size,
                consecutiveStreak = streak,
                yearlyAchievements = monthlyData,
                yearlyCalories = yCalories,
                yearlyVolume = yVolume,
                yearlyWorkoutCount = yearlyWorkouts.size
            )
        }
    }

    private fun getMetForBodyPart(bodyPartStr: String): Double {
        return try {
            val bodyPart = com.fitnessapp.tracker.data.model.BodyPart.valueOf(bodyPartStr)
            when (bodyPart) {
                com.fitnessapp.tracker.data.model.BodyPart.CARDIO -> 8.0
                com.fitnessapp.tracker.data.model.BodyPart.FULL_BODY -> 6.0
                com.fitnessapp.tracker.data.model.BodyPart.LEGS -> 5.5
                com.fitnessapp.tracker.data.model.BodyPart.CHEST,
                com.fitnessapp.tracker.data.model.BodyPart.BACK,
                com.fitnessapp.tracker.data.model.BodyPart.SHOULDERS -> 5.0
                com.fitnessapp.tracker.data.model.BodyPart.ARMS -> 4.5
                com.fitnessapp.tracker.data.model.BodyPart.CORE -> 4.0
            }
        } catch (e: Exception) {
            5.0
        }
    }

    private fun calculateStreak(workouts: List<Workout>): Int {
        if (workouts.isEmpty()) return 0
        val workoutDates = workouts.map { DateUtils.getStartOfDay(it.date) }.distinct().sortedDescending()
        val today = DateUtils.getStartOfDay(System.currentTimeMillis())
        val yesterday = today - 86400000L

        val latest = workoutDates.first()
        if (latest != today && latest != yesterday) {
            return 0
        }

        var streak = 1
        var currentDate = latest

        for (i in 1 until workoutDates.size) {
            val prevDate = workoutDates[i]
            if (currentDate - prevDate == 86400000L) {
                streak++
                currentDate = prevDate
            } else if (currentDate - prevDate > 86400000L) {
                break
            }
        }

        return streak
    }

    fun nextMonth() {
        if (_currentMonth.value == 11) {
            _currentMonth.value = 0
            _currentYear.value += 1
        } else {
            _currentMonth.value += 1
        }
        loadCalendarData()
    }

    fun prevMonth() {
        if (_currentMonth.value == 0) {
            _currentMonth.value = 11
            _currentYear.value -= 1
        } else {
            _currentMonth.value -= 1
        }
        loadCalendarData()
    }

    fun selectDay(dayStart: Long) {
        viewModelScope.launch {
            val dayEnd = dayStart + 86400000L
            val joinedList = workoutRepo.getWorkoutSetsWithExerciseByDay(dayStart, dayEnd)

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

            _state.update {
                it.copy(
                    selectedDay = if (it.selectedDay == dayStart) null else dayStart,
                    dayWorkouts = if (it.selectedDay == dayStart) emptyList() else details
                )
            }
        }
    }

    fun deleteWorkout(workoutId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            workoutRepo.deleteWorkoutById(workoutId)
            loadCalendarData()
            _state.value.selectedDay?.let { selectDay(it) }
        }
    }

    fun deleteSetFromWorkout(set: WorkoutSet) {
        viewModelScope.launch(Dispatchers.IO) {
            workoutRepo.deleteSet(set)
            loadCalendarData()
            _state.value.selectedDay?.let { selectDay(it) }
        }
    }
}
