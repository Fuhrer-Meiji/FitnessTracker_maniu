package com.fitnessapp.tracker.data.db.entity

data class WorkoutSetWithExercise(
    val id: Long,
    val workoutId: Long,
    val date: Long,
    val workoutNote: String?,
    val exerciseId: Long,
    val exerciseName: String,
    val bodyPart: String,
    val setNumber: Int,
    val recordType: String,
    val weight: Double?,
    val reps: Int?,
    val durationSeconds: Int?,
    val restSeconds: Int?
)
