package com.fitnessapp.tracker.data.model

data class WorkoutSet(
    val id: Long = 0,
    val workoutId: Long,
    val exerciseId: Long,
    val setNumber: Int,
    val recordType: RecordType,
    val weight: Double? = null,
    val reps: Int? = null,
    val durationSeconds: Int? = null,
    val restSeconds: Int? = null,
    val isCompleted: Boolean = true,
    val supersetId: String? = null
)
