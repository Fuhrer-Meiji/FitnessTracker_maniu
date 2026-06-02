package com.fitnessapp.tracker.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fitnessapp.tracker.data.model.Workout

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long,
    val startTime: Long,
    val endTime: Long? = null,
    val note: String? = null,
    val isDraft: Boolean = false
) {
    fun toModel() = Workout(id, date, startTime, endTime, note, isDraft)
    companion object {
        fun fromModel(m: Workout) = WorkoutEntity(m.id, m.date, m.startTime, m.endTime, m.note, m.isDraft)
    }
}
