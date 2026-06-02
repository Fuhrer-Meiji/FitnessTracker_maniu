package com.fitnessapp.tracker.data.model

data class Exercise(
    val id: Long = 0,
    val name: String,
    val bodyPart: BodyPart,
    val recordType: RecordType,
    val iconName: String,
    val isPreset: Boolean,
    val createdAt: Long = System.currentTimeMillis()
)
