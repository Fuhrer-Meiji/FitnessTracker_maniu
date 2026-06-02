package com.fitnessapp.tracker.data.repository

import com.fitnessapp.tracker.data.db.dao.BodyMetricDao
import com.fitnessapp.tracker.data.db.entity.BodyMetricEntity
import com.fitnessapp.tracker.data.model.BodyMetric
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BodyMetricRepository(private val dao: BodyMetricDao) {
    fun getAllMetrics(): Flow<List<BodyMetric>> = dao.getAllMetrics().map { list -> list.map { it.toModel() } }
    suspend fun getLatestMetric(): BodyMetric? = dao.getLatestMetric()?.toModel()
    suspend fun insert(metric: BodyMetric): Long = dao.insert(BodyMetricEntity.fromModel(metric))
    suspend fun delete(metric: BodyMetric) = dao.delete(BodyMetricEntity.fromModel(metric))
}
