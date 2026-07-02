package com.fitnessapp.tracker.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fitnessapp.tracker.data.model.BodyPart
import com.fitnessapp.tracker.data.model.Exercise
import com.fitnessapp.tracker.data.model.RecordType
import com.fitnessapp.tracker.data.model.Equipment

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val bodyPart: String,
    val equipment: String = "BARBELL",
    val recordType: String,
    val iconName: String,
    val isPreset: Boolean,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toModel() = Exercise(
        id = id,
        name = name,
        bodyPart = BodyPart.valueOf(bodyPart),
        equipment = Equipment.valueOf(equipment),
        recordType = RecordType.valueOf(recordType),
        iconName = iconName,
        isPreset = isPreset,
        createdAt = createdAt
    )

    companion object {
        fun fromModel(m: Exercise) = ExerciseEntity(
            id = m.id,
            name = m.name,
            bodyPart = m.bodyPart.name,
            equipment = m.equipment.name,
            recordType = m.recordType.name,
            iconName = m.iconName,
            isPreset = m.isPreset,
            createdAt = m.createdAt
        )
    }
}
