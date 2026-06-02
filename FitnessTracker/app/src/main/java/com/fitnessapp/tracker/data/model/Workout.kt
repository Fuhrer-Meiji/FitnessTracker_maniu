package com.fitnessapp.tracker.data.model

data class Workout(
    val id: Long = 0,
    val date: Long,
    val startTime: Long,
    val endTime: Long? = null,
    val note: String? = null,
    val isDraft: Boolean = false
)
