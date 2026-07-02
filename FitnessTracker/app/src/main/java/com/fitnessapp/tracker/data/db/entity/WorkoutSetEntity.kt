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
    val restSeconds: Int? = null,
    val supersetId: String? = null
) {
    fun toModel() = WorkoutSet(
        id = id,
        workoutId = workoutId,
        exerciseId = exerciseId,
        setNumber = setNumber,
        recordType = RecordType.valueOf(recordType),
        weight = weight,
        reps = reps,
        durationSeconds = durationSeconds,
        restSeconds = restSeconds,
        supersetId = supersetId
    )
    companion object {
        fun fromModel(m: WorkoutSet) = WorkoutSetEntity(
            id = m.id,
            workoutId = m.workoutId,
            exerciseId = m.exerciseId,
            setNumber = m.setNumber,
            recordType = m.recordType.name,
            weight = m.weight,
            reps = m.reps,
            durationSeconds = m.durationSeconds,
            restSeconds = m.restSeconds,
            supersetId = m.supersetId
        )
    }
}
