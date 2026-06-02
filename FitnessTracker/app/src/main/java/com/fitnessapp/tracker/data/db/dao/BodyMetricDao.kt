package com.fitnessapp.tracker.data.db.dao

import androidx.room.*
import com.fitnessapp.tracker.data.db.entity.BodyMetricEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BodyMetricDao {
    @Query("SELECT * FROM body_metrics ORDER BY date DESC")
    fun getAllMetrics(): Flow<List<BodyMetricEntity>>

    @Query("SELECT * FROM body_metrics ORDER BY date DESC LIMIT 1")
    suspend fun getLatestMetric(): BodyMetricEntity?

    @Insert
    suspend fun insert(metric: BodyMetricEntity): Long

    @Delete
    suspend fun delete(metric: BodyMetricEntity)
}
