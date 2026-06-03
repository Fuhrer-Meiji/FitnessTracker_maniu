package com.fitnessapp.tracker.data.db.entity

data class SetTrendData(
    val date: Long,
    val weight: Double?,
    val reps: Int?,
    val durationSeconds: Int?
)
