package com.fitnessapp.tracker.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fitnessapp.tracker.FitnessApp
import com.fitnessapp.tracker.data.model.BodyMetric
import com.fitnessapp.tracker.data.model.BodyPart
import com.fitnessapp.tracker.data.model.Equipment
import com.fitnessapp.tracker.data.model.Exercise
import com.fitnessapp.tracker.data.model.RecordType
import com.fitnessapp.tracker.data.repository.BodyMetricRepository
import com.fitnessapp.tracker.data.repository.ExerciseRepository
import com.fitnessapp.tracker.ui.theme.ThemeManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SettingsUiState(
    val latestBodyWeight: Double? = null,
    val latestBodyFat: Double? = null,
    val bodyMetricCount: Int = 0,
    val bodyMetrics: List<BodyMetric> = emptyList(),
    val exerciseCount: Int = 0,
    val exercises: List<Exercise> = emptyList(),
    val currentThemeIndex: Int = 0,
    val currentUnit: String = "kg",
    val bgImageEnabled: Boolean = false,
    val bgImagePath: String? = null
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as FitnessApp
    private val bodyMetricRepo = BodyMetricRepository(app.database.bodyMetricDao())
    private val exerciseRepo = ExerciseRepository(app.database.exerciseDao())
    val themeManager = ThemeManager(application)

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        loadData()
        observeTheme()
        observeUnit()
        observeBackground()
    }

    private fun loadData() {
        viewModelScope.launch {
            val latest = bodyMetricRepo.getLatestMetric()
            _state.update { it.copy(
                latestBodyWeight = latest?.weight,
                latestBodyFat = latest?.bodyFat
            )}
        }
        viewModelScope.launch {
            bodyMetricRepo.getAllMetrics().collect { metrics ->
                _state.update { it.copy(
                    bodyMetrics = metrics,
                    bodyMetricCount = metrics.size
                )}
            }
        }
        viewModelScope.launch {
            exerciseRepo.getAllExercises().collect { exercises ->
                _state.update { it.copy(
                    exercises = exercises,
                    exerciseCount = exercises.size
                )}
            }
        }
    }

    private fun observeTheme() {
        viewModelScope.launch {
            themeManager.themeIndex.collect { index ->
                _state.update { it.copy(currentThemeIndex = index) }
            }
        }
    }

    private fun observeUnit() {
        viewModelScope.launch {
            themeManager.unit.collect { unit ->
                _state.update { it.copy(currentUnit = unit) }
            }
        }
    }

    private fun observeBackground() {
        viewModelScope.launch {
            themeManager.bgImageEnabled.collect { enabled ->
                _state.update { it.copy(bgImageEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            themeManager.bgImagePath.collect { path ->
                _state.update { it.copy(bgImagePath = path) }
            }
        }
    }

    fun setThemeIndex(index: Int) {
        viewModelScope.launch { themeManager.setThemeIndex(index) }
    }

    fun setUnit(unit: String) {
        viewModelScope.launch { themeManager.setUnit(unit) }
    }

    fun setBgImageEnabled(enabled: Boolean) {
        viewModelScope.launch { themeManager.setBgImageEnabled(enabled) }
    }

    fun setBgImagePath(path: String?) {
        viewModelScope.launch { themeManager.setBgImagePath(path) }
    }

    fun addBodyMetric(weight: Double?, bodyFat: Double?) {
        viewModelScope.launch {
            bodyMetricRepo.insert(BodyMetric(date = System.currentTimeMillis(), weight = weight, bodyFat = bodyFat))
            loadData()
        }
    }

    fun deleteBodyMetric(metric: BodyMetric) {
        viewModelScope.launch { bodyMetricRepo.delete(metric) }
    }

    fun addExercise(name: String, bodyPart: BodyPart, equipment: Equipment, recordType: RecordType, iconName: String) {
        viewModelScope.launch {
            exerciseRepo.insert(Exercise(
                name = name, bodyPart = bodyPart, equipment = equipment, recordType = recordType,
                iconName = iconName, isPreset = false
            ))
        }
    }

    fun deleteExercise(exercise: Exercise) {
        viewModelScope.launch {
            if (!exercise.isPreset) exerciseRepo.delete(exercise)
        }
    }
}
