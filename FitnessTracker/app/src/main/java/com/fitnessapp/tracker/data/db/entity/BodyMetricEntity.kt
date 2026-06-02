package com.fitnessapp.tracker.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fitnessapp.tracker.data.model.BodyMetric

@Entity(tableName = "body_metrics")
data class BodyMetricEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long,
    val weight: Double? = null,
    val bodyFat: Double? = null,
    val note: String? = null
) {
    fun toModel() = BodyMetric(id, date, weight, bodyFat, note)
    companion object {
        fun fromModel(m: BodyMetric) = BodyMetricEntity(m.id, m.date, m.weight, m.bodyFat, m.note)
    }
}
