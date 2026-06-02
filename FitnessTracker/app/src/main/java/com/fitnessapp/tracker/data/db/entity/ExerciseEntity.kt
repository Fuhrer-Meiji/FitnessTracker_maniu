package com.fitnessapp.tracker.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fitnessapp.tracker.data.model.BodyPart
import com.fitnessapp.tracker.data.model.Exercise
import com.fitnessapp.tracker.data.model.RecordType

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val bodyPart: String,
    val recordType: String,
    val iconName: String,
    val isPreset: Boolean,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toModel() = Exercise(id, name, BodyPart.valueOf(bodyPart), RecordType.valueOf(recordType), iconName, isPreset, createdAt)
    companion object {
        fun fromModel(m: Exercise) = ExerciseEntity(m.id, m.name, m.bodyPart.name, m.recordType.name, m.iconName, m.isPreset, m.createdAt)
    }
}
