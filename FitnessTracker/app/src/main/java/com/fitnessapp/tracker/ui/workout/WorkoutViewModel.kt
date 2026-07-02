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
    val isActive: Boolean = true,
    var historicalMaxSet: WorkoutSet? = null,
    var supersetGroupId: String? = null
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
    val showDraftRestoreDialog: Boolean = false,
    val restCountdownSeconds: Int = 0,
    val restCountdownActive: Boolean = false,
    val totalRestTimeSeconds: Int = 90
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

    private var restTimerJob: kotlinx.coroutines.Job? = null
    private var restTargetEndTime: Long = 0L

    fun startRestTimer(seconds: Int = 90) {
        restTimerJob?.cancel()
        restTargetEndTime = System.currentTimeMillis() + seconds * 1000L
        _state.update { it.copy(
            restCountdownSeconds = seconds,
            restCountdownActive = true,
            totalRestTimeSeconds = seconds
        )}
        restTimerJob = viewModelScope.launch {
            while (_state.value.restCountdownActive) {
                val remaining = ((restTargetEndTime - System.currentTimeMillis() + 999) / 1000L).coerceAtLeast(0L).toInt()
                _state.update { it.copy(
                    restCountdownSeconds = remaining,
                    restCountdownActive = remaining > 0
                )}
                if (remaining <= 0) break
                kotlinx.coroutines.delay(500)
            }
        }
    }

    fun pauseRestTimer() {
        restTimerJob?.cancel()
        _state.update { it.copy(restCountdownActive = false) }
    }

    fun resumeRestTimer() {
        if (_state.value.restCountdownSeconds <= 0) return
        _state.update { it.copy(restCountdownActive = true) }
        restTargetEndTime = System.currentTimeMillis() + _state.value.restCountdownSeconds * 1000L
        restTimerJob = viewModelScope.launch {
            while (_state.value.restCountdownActive) {
                val remaining = ((restTargetEndTime - System.currentTimeMillis() + 999) / 1000L).coerceAtLeast(0L).toInt()
                _state.update { it.copy(
                    restCountdownSeconds = remaining,
                    restCountdownActive = remaining > 0
                )}
                if (remaining <= 0) break
                kotlinx.coroutines.delay(500)
            }
        }
    }

    fun adjustRestTimer(deltaSeconds: Int) {
        val wasActive = _state.value.restCountdownActive
        restTimerJob?.cancel()
        _state.update { 
            val newSec = kotlin.math.max(0, it.restCountdownSeconds + deltaSeconds)
            restTargetEndTime = System.currentTimeMillis() + newSec * 1000L
            it.copy(
                restCountdownSeconds = newSec,
                restCountdownActive = newSec > 0
            )
        }
        if (wasActive && _state.value.restCountdownSeconds > 0) {
            resumeRestTimer()
        }
    }

    fun skipRestTimer() {
        restTimerJob?.cancel()
        _state.update { it.copy(restCountdownSeconds = 0, restCountdownActive = false) }
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
            skipRestTimer()
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
            durationSeconds = if (card.exercise.recordType == RecordType.DURATION) card.currentDuration else null,
            isCompleted = true,
            supersetId = card.supersetGroupId
        )
        
        viewModelScope.launch {
            val insertedId = repo.insertSet(set)
            card.sets.add(set.copy(id = insertedId, supersetId = card.supersetGroupId))
            card.setNumber++
            _state.update { it.copy(cards = cards) }
            startRestTimer()
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

    private suspend fun fetchHistoricalMaxSet(exerciseId: Long, recordType: RecordType): WorkoutSet? {
        return when (recordType) {
            RecordType.STRENGTH -> repo.getMaxWeightSetForExercise(exerciseId)
            RecordType.REPS -> repo.getMaxRepsSetForExercise(exerciseId)
            RecordType.DURATION -> repo.getMaxDurationSetForExercise(exerciseId)
        }
    }

    fun addExerciseCard(exercise: Exercise) {
        viewModelScope.launch {
            val lastSet = repo.getLastSetForExercise(exercise.id)
            val maxSet = fetchHistoricalMaxSet(exercise.id, exercise.recordType)
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
                    currentDuration = defaultDuration,
                    historicalMaxSet = maxSet
                )
            )
            _state.update { it.copy(cards = updated, showExercisePicker = false, pickerTargetCardIndex = null) }
        }
    }

    fun replaceCardExercise(cardIndex: Int, exercise: Exercise) {
        viewModelScope.launch {
            val lastSet = repo.getLastSetForExercise(exercise.id)
            val maxSet = fetchHistoricalMaxSet(exercise.id, exercise.recordType)
            val defaultWeight = lastSet?.weight ?: 60.0
            val defaultReps = lastSet?.reps ?: 10
            val defaultDuration = lastSet?.durationSeconds ?: 30

            val cards = _state.value.cards.toMutableList()
            if (cardIndex < cards.size) {
                val card = cards[cardIndex]
                
                // Delete previous sets from DB under this draft workout
                card.sets.forEach { set ->
                    if (set.id != 0L) {
                        repo.deleteSet(set)
                    }
                }

                val newCard = ActiveExerciseCard(
                    exercise = exercise,
                    sets = mutableListOf(),
                    currentWeight = defaultWeight,
                    currentReps = defaultReps,
                    currentDuration = defaultDuration,
                    isActive = card.isActive,
                    historicalMaxSet = maxSet
                )
                cards[cardIndex] = newCard
                _state.update { it.copy(cards = cards, showExercisePicker = false, pickerTargetCardIndex = null) }
            }
        }
    }

    fun activateCard(cardIndex: Int) {
        val s = _state.value
        val cards = s.cards.toMutableList()
        if (cardIndex < 0 || cardIndex >= cards.size) return
        
        val updated = cards.mapIndexed { index, card ->
            card.copy(isActive = (index == cardIndex))
        }
        _state.update { it.copy(cards = updated) }
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
        if (_state.value.isRecording) return
        viewModelScope.launch {
            val draft = repo.getDraftWorkout()
            if (draft != null) {
                _state.update { it.copy(currentWorkoutId = draft.id) }
                restoreDraftWorkout()
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
                    workoutSet.copy(setNumber = index + 1, isCompleted = true)
                }

                val lastSet = mappedSets.lastOrNull()
                val currentWeight = lastSet?.weight ?: 60.0
                val currentReps = lastSet?.reps ?: 10
                val currentDuration = lastSet?.durationSeconds ?: 30
                val maxSet = fetchHistoricalMaxSet(exercise.id, exercise.recordType)
                val supersetGroupId = exerciseSets.firstOrNull { it.supersetId != null }?.supersetId

                cards.add(
                    ActiveExerciseCard(
                        exercise = exercise,
                        sets = mappedSets.toMutableList(),
                        currentWeight = currentWeight,
                        currentReps = currentReps,
                        currentDuration = currentDuration,
                        setNumber = mappedSets.size + 1,
                        isActive = false,
                        historicalMaxSet = maxSet,
                        supersetGroupId = supersetGroupId
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

    fun discardCurrentWorkout() {
        viewModelScope.launch {
            val workoutId = _state.value.currentWorkoutId
            if (workoutId != null) {
                repo.deleteSetsForWorkout(workoutId)
                repo.deleteWorkoutById(workoutId)
            }
            timerJob?.cancel()
            skipRestTimer()
            _state.update { it.copy(
                isRecording = false,
                currentWorkoutId = null,
                cards = emptyList(),
                elapsedSeconds = 0,
                showEndConfirm = false
            )}
        }
    }

    fun discardDraftWorkout() {
        viewModelScope.launch {
            val draftId = _state.value.currentWorkoutId ?: return@launch
            repo.deleteSetsForWorkout(draftId)
            repo.deleteWorkoutById(draftId)
            skipRestTimer()
            _state.update { it.copy(showDraftRestoreDialog = false, currentWorkoutId = null) }
        }
    }

    fun linkCardsAsSuperset(cardIndex: Int) {
        val s = _state.value
        val cards = s.cards.toMutableList()
        if (cardIndex < 0 || cardIndex >= cards.size - 1) return
        
        val newSupersetId = cards[cardIndex].supersetGroupId 
            ?: cards[cardIndex + 1].supersetGroupId 
            ?: java.util.UUID.randomUUID().toString()

        val card1 = cards[cardIndex].copy(supersetGroupId = newSupersetId)
        val card2 = cards[cardIndex + 1].copy(supersetGroupId = newSupersetId)
        
        viewModelScope.launch {
            card1.sets.forEach { set ->
                val updatedSet = set.copy(supersetId = newSupersetId)
                repo.insertSet(updatedSet)
            }
            card2.sets.forEach { set ->
                val updatedSet = set.copy(supersetId = newSupersetId)
                repo.insertSet(updatedSet)
            }
        }

        cards[cardIndex] = card1
        cards[cardIndex + 1] = card2
        _state.update { it.copy(cards = cards) }
    }

    fun unlinkCardFromSuperset(cardIndex: Int) {
        val s = _state.value
        val cards = s.cards.toMutableList()
        if (cardIndex < 0 || cardIndex >= cards.size) return
        
        val oldCard = cards[cardIndex]
        val oldSupersetGroupId = oldCard.supersetGroupId ?: return
        
        val newCard = oldCard.copy(supersetGroupId = null)
        
        viewModelScope.launch {
            newCard.sets.forEach { set ->
                val updatedSet = set.copy(supersetId = null)
                repo.insertSet(updatedSet)
            }
        }
        
        cards[cardIndex] = newCard
        
        val remainingInGroup = cards.filter { it.supersetGroupId == oldSupersetGroupId }
        if (remainingInGroup.size == 1) {
            val lastCardIndex = cards.indexOfFirst { it.supersetGroupId == oldSupersetGroupId }
            if (lastCardIndex != -1) {
                val lastCard = cards[lastCardIndex].copy(supersetGroupId = null)
                viewModelScope.launch {
                    lastCard.sets.forEach { set ->
                        val updatedSet = set.copy(supersetId = null)
                        repo.insertSet(updatedSet)
                    }
                }
                cards[lastCardIndex] = lastCard
            }
        }

        _state.update { it.copy(cards = cards) }
    }
}
