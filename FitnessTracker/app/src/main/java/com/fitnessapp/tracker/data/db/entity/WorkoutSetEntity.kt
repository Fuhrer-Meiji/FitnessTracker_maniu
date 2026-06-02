package com.fitnessapp.tracker.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fitnessapp.tracker.data.model.RecordType
import com.fitnessapp.tracker.data.model.WorkoutSet

@Entity(
    tableName = "workout_sets",
    foreignKeys = [
        ForeignKey(entity = WorkoutEntity::class, parentColumns = ["id"], childColumns = ["workoutId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = ExerciseEntity::class, parentColumns = ["id"], childColumns = ["exerciseId"])
    ],
    indices = [Index("workoutId"), Index("exerciseId")]
)
data class WorkoutSetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workoutId: Long,
    val exerciseId: Long,
    val setNumber: Int,
    val recordType: String,
    val weight: Double? = null,
    val reps: Int? = null,
    val durationSeconds: Int? = null,
    val restSeconds: Int? = null
) {
    fun toModel() = WorkoutSet(id, workoutId, exerciseId, setNumber, RecordType.valueOf(recordType), weight, reps, durationSeconds, restSeconds)
    companion object {
        fun fromModel(m: WorkoutSet) = WorkoutSetEntity(m.id, m.workoutId, m.exerciseId, m.setNumber, m.recordType.name, m.weight, m.reps, m.durationSeconds, m.restSeconds)
    }
}
