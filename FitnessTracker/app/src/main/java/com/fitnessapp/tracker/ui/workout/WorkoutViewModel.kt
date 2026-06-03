package com.fitnessapp.tracker.ui.workout

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fitnessapp.tracker.FitnessApp
import com.fitnessapp.tracker.data.model.*
import com.fitnessapp.tracker.data.repository.ExerciseRepository
import com.fitnessapp.tracker.data.repository.WorkoutRepository
import com.fitnessapp.tracker.ui.theme.ThemeManager
import com.fitnessapp.tracker.util.DateUtils
import java.util.Calendar
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ActiveExerciseCard(
    val exercise: Exercise,
    val sets: MutableList<WorkoutSet> = mutableListOf(),
    var currentWeight: Double = 60.0,
    var currentReps: Int = 10,
    var currentDuration: Int = 30,
    var setNumber: Int = 1,
    val isActive: Boolean = true
)

data class WorkoutUiState(
    val isRecording: Boolean = false,
    val currentWorkoutId: Long? = null,
    val startTime: Long = 0,
    val elapsedSeconds: Long = 0,
    val cards: List<ActiveExerciseCard> = emptyList(),
    val recentWorkouts: List<Workout> = emptyList(),
    val workoutDates: Set<Long> = emptySet(),
    val dailyFrequency: Map<Int, Int> = emptyMap(),
    val exercises: List<Exercise> = emptyList(),
    val showExercisePicker: Boolean = false,
    val pickerTargetCardIndex: Int? = null,
    val showEndConfirm: Boolean = false,
    val currentUnit: String = "kg",
    val showDraftRestoreDialog: Boolean = false
)

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {
    private val db = (application as FitnessApp).database
    private val repo = WorkoutRepository(db.workoutDao())
    private val exerciseRepo = ExerciseRepository(db.exerciseDao())

    private val _state = MutableStateFlow(WorkoutUiState())
    val state: StateFlow<WorkoutUiState> = _state.asStateFlow()

    private var timerJob: kotlinx.coroutines.Job? = null
    private val themeManager = ThemeManager(application)

    init {
        loadRecentWorkouts()
        loadExercises()
        observeUnit()
    }

    private fun observeUnit() {
        viewModelScope.launch {
            themeManager.unit.collect { unit ->
                _state.update { it.copy(currentUnit = unit) }
            }
        }
    }

    private fun loadExercises() {
        viewModelScope.launch {
            exerciseRepo.getAllExercises().collect { exercises ->
                _state.update { it.copy(exercises = exercises) }
            }
        }
    }

    private fun loadRecentWorkouts() {
        viewModelScope.launch {
            repo.getAllWorkouts().collect { workouts ->
                val cal = Calendar.getInstance()
                cal.set(Calendar.DAY_OF_MONTH, 1)
                val monthStart = DateUtils.getStartOfDay(cal.timeInMillis)
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                val monthEnd = DateUtils.getEndOfDay(cal.timeInMillis)

                val monthWorkouts = workouts.filter { it.date in monthStart..monthEnd }
                val dayCounts = monthWorkouts.groupBy { w ->
                    val c = Calendar.getInstance()
                    c.timeInMillis = w.date
                    c.get(Calendar.DAY_OF_MONTH)
                }.mapValues { it.value.size }

                _state.update { it.copy(
                    recentWorkouts = workouts.take(5),
                    workoutDates = workouts.map { w -> DateUtils.getStartOfDay(w.date) }.toSet(),
                    dailyFrequency = dayCounts
                )}
            }
        }
    }

    fun startWorkout() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val workout = Workout(date = now, startTime = now, isDraft = true)
            val id = repo.insertWorkout(workout)
            _state.update { it.copy(
                isRecording = true,
                currentWorkoutId = id,
                startTime = now,
                cards = emptyList(),
                showEndConfirm = false
            )}
            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(1000)
                _state.update { it.copy(elapsedSeconds = (System.currentTimeMillis() - _state.value.startTime) / 1000) }
            }
        }
    }

    fun endWorkout() {
        viewModelScope.launch {
            val s = _state.value
            val now = System.currentTimeMillis()
            repo.getWorkoutById(s.currentWorkoutId ?: return@launch)?.let { w ->
                repo.updateWorkout(w.copy(endTime = now, isDraft = false))
                // Note: s.cards' sets are already inserted in real-time,
                // so we don't insert them again.
            }
            timerJob?.cancel()
            _state.update { it.copy(isRecording = false, currentWorkoutId = null, cards = emptyList(), elapsedSeconds = 0, showEndConfirm = false) }
        }
    }

    fun addSetToCard(cardIndex: Int) {
        val s = _state.value
        val cards = s.cards.toMutableList()
        if (cardIndex >= cards.size) return
        val card = cards[cardIndex]
        val set = WorkoutSet(
            workoutId = s.currentWorkoutId ?: return,
            exerciseId = card.exercise.id,
            setNumber = card.setNumber,
            recordType = card.exercise.recordType,
            weight = if (card.exercise.recordType == RecordType.STRENGTH) card.currentWeight else null,
            reps = if (card.exercise.recordType != RecordType.DURATION) card.currentReps else null,
            durationSeconds = if (card.exercise.recordType == RecordType.DURATION) card.currentDuration else null
        )
        
        viewModelScope.launch {
            val insertedId = repo.insertSet(set)
            card.sets.add(set.copy(id = insertedId))
            card.setNumber++
            _state.update { it.copy(cards = cards) }
        }
    }

    fun deleteSet(cardIndex: Int, setIndex: Int) {
        val cards = _state.value.cards.toMutableList()
        if (cardIndex < cards.size) {
            val card = cards[cardIndex]
            if (setIndex < card.sets.size) {
                val set = card.sets[setIndex]
                viewModelScope.launch {
                    repo.deleteSet(set)
                    card.sets.removeAt(setIndex)
                    
                    // Re-index remaining sets sequentially in memory
                    val reindexedSets = card.sets.mapIndexed { index, s ->
                        s.copy(setNumber = index + 1)
                    }
                    card.sets.clear()
                    card.sets.addAll(reindexedSets)
                    card.setNumber = card.sets.size + 1
                    
                    _state.update { it.copy(cards = cards) }
                }
            }
        }
    }

    fun deleteCard(cardIndex: Int) {
        val cards = _state.value.cards.toMutableList()
        if (cardIndex < cards.size) {
            val card = cards[cardIndex]
            viewModelScope.launch {
                card.sets.forEach { set ->
                    repo.deleteSet(set)
                }
                cards.removeAt(cardIndex)
                _state.update { it.copy(cards = cards) }
            }
        }
    }

    fun adjustField(cardIndex: Int, field: String, delta: Double) {
        val cards = _state.value.cards.toMutableList()
        if (cardIndex >= cards.size) return
        val card = cards[cardIndex]
        when (field) {
            "weight" -> card.currentWeight = kotlin.math.max(0.0, kotlin.math.round((card.currentWeight + delta) * 10) / 10.0)
            "reps" -> card.currentReps = kotlin.math.max(1, card.currentReps + delta.toInt())
            "duration" -> card.currentDuration = kotlin.math.max(1, card.currentDuration + delta.toInt())
        }
        _state.update { it.copy(cards = cards) }
    }

    fun addExerciseCard(exercise: Exercise) {
        viewModelScope.launch {
            val lastSet = repo.getLastSetForExercise(exercise.id)
            val defaultWeight = lastSet?.weight ?: 60.0
            val defaultReps = lastSet?.reps ?: 10
            val defaultDuration = lastSet?.durationSeconds ?: 30

            val cards = _state.value.cards.toMutableList()
            val updated = cards.map { it.copy(isActive = false) }.toMutableList()
            updated.add(
                ActiveExerciseCard(
                    exercise = exercise,
                    currentWeight = defaultWeight,
                    currentReps = defaultReps,
                    currentDuration = defaultDuration
                )
            )
            _state.update { it.copy(cards = updated, showExercisePicker = false, pickerTargetCardIndex = null) }
        }
    }

    fun replaceCardExercise(cardIndex: Int, exercise: Exercise) {
        viewModelScope.launch {
            val lastSet = repo.getLastSetForExercise(exercise.id)
            val defaultWeight = lastSet?.weight ?: 60.0
            val defaultReps = lastSet?.reps ?: 10
            val defaultDuration = lastSet?.durationSeconds ?: 30

            val cards = _state.value.cards.toMutableList()
            if (cardIndex < cards.size) {
                val card = cards[cardIndex]
                
                // Delete previous sets from DB under this draft workout
                card.sets.forEach { set ->
                    repo.deleteSet(set)
                }

                val newCard = ActiveExerciseCard(
                    exercise = exercise,
                    sets = mutableListOf(),
                    currentWeight = defaultWeight,
                    currentReps = defaultReps,
                    currentDuration = defaultDuration,
                    isActive = card.isActive
                )
                cards[cardIndex] = newCard
                _state.update { it.copy(cards = cards, showExercisePicker = false, pickerTargetCardIndex = null) }
            }
        }
    }

    fun showExercisePicker(targetCardIndex: Int? = null) {
        _state.update { it.copy(showExercisePicker = true, pickerTargetCardIndex = targetCardIndex) }
    }

    fun hideExercisePicker() {
        _state.update { it.copy(showExercisePicker = false, pickerTargetCardIndex = null) }
    }

    fun showEndConfirm() {
        _state.update { it.copy(showEndConfirm = true) }
    }

    fun hideEndConfirm() {
        _state.update { it.copy(showEndConfirm = false) }
    }

    fun checkForDraft() {
        viewModelScope.launch {
            val draft = repo.getDraftWorkout()
            if (draft != null) {
                _state.update { it.copy(showDraftRestoreDialog = true, currentWorkoutId = draft.id) }
            }
        }
    }

    fun restoreDraftWorkout() {
        viewModelScope.launch {
            val draftId = _state.value.currentWorkoutId ?: return@launch
            val draft = repo.getWorkoutById(draftId) ?: return@launch
            val sets = repo.getSetsForWorkout(draftId)
            val cards = mutableListOf<ActiveExerciseCard>()
            val uniqueExerciseIds = sets.map { it.exerciseId }.distinct()

            for (exId in uniqueExerciseIds) {
                val exercise = exerciseRepo.getExerciseById(exId) ?: continue
                val exerciseSets = sets.filter { it.exerciseId == exId }
                val mappedSets = exerciseSets.mapIndexed { index, workoutSet ->
                    workoutSet.copy(setNumber = index + 1)
                }

                val lastSet = mappedSets.lastOrNull()
                val currentWeight = lastSet?.weight ?: 60.0
                val currentReps = lastSet?.reps ?: 10
                val currentDuration = lastSet?.durationSeconds ?: 30

                cards.add(
                    ActiveExerciseCard(
                        exercise = exercise,
                        sets = mappedSets.toMutableList(),
                        currentWeight = currentWeight,
                        currentReps = currentReps,
                        currentDuration = currentDuration,
                        setNumber = mappedSets.size + 1,
                        isActive = false
                    )
                )
            }

            if (cards.isNotEmpty()) {
                cards[cards.lastIndex] = cards.last().copy(isActive = true)
            }

            _state.update { it.copy(
                isRecording = true,
                startTime = draft.startTime,
                elapsedSeconds = (System.currentTimeMillis() - draft.startTime) / 1000,
                cards = cards,
                showDraftRestoreDialog = false
            )}
            startTimer()
        }
    }

    fun discardDraftWorkout() {
        viewModelScope.launch {
            val draftId = _state.value.currentWorkoutId ?: return@launch
            repo.deleteSetsForWorkout(draftId)
            repo.deleteWorkoutById(draftId)
            _state.update { it.copy(showDraftRestoreDialog = false, currentWorkoutId = null) }
        }
    }
}
