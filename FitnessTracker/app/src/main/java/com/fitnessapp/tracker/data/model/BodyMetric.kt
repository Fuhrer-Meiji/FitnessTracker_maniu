package com.fitnessapp.tracker.data.model

data class BodyMetric(
    val id: Long = 0,
    val date: Long,
    val weight: Double? = null,
    val bodyFat: Double? = null,
    val note: String? = null
)
