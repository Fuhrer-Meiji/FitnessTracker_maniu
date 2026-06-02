package com.fitnessapp.tracker.data.repository

import com.fitnessapp.tracker.data.db.dao.ExerciseDao
import com.fitnessapp.tracker.data.db.entity.ExerciseEntity
import com.fitnessapp.tracker.data.model.Exercise
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExerciseRepository(private val dao: ExerciseDao) {
    fun getAllExercises(): Flow<List<Exercise>> = dao.getAllExercises().map { list -> list.map { it.toModel() } }
    suspend fun getAllExercisesList(): List<Exercise> = dao.getAllExercisesList().map { it.toModel() }
    suspend fun getExerciseById(id: Long): Exercise? = dao.getExerciseById(id)?.toModel()
    suspend fun insert(exercise: Exercise): Long = dao.insert(ExerciseEntity.fromModel(exercise))
    suspend fun update(exercise: Exercise) = dao.update(ExerciseEntity.fromModel(exercise))
    suspend fun delete(exercise: Exercise) = dao.delete(ExerciseEntity.fromModel(exercise))
}
