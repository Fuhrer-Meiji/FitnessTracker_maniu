package com.fitnessapp.tracker.data.repository

import com.fitnessapp.tracker.data.db.dao.WorkoutDao
import com.fitnessapp.tracker.data.db.entity.WorkoutEntity
import com.fitnessapp.tracker.data.db.entity.WorkoutSetEntity
import com.fitnessapp.tracker.data.model.Workout
import com.fitnessapp.tracker.data.model.WorkoutSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WorkoutRepository(private val dao: WorkoutDao) {
    fun getAllWorkouts(): Flow<List<Workout>> = dao.getAllWorkouts().map { list -> list.map { it.toModel() } }
    fun getWorkoutsInRange(start: Long, end: Long): Flow<List<Workout>> = dao.getWorkoutsInRange(start, end).map { list -> list.map { it.toModel() } }
    suspend fun getWorkoutByDate(date: Long): Workout? = dao.getWorkoutByDate(date)?.toModel()
    suspend fun getWorkoutById(id: Long): Workout? = dao.getWorkoutById(id)?.toModel()
    suspend fun getSetsForWorkout(workoutId: Long): List<WorkoutSet> = dao.getSetsForWorkout(workoutId).map { it.toModel() }
    suspend fun getSetsForExercise(workoutId: Long, exerciseId: Long): List<WorkoutSet> = dao.getSetsForExercise(workoutId, exerciseId).map { it.toModel() }
    suspend fun getSetsInRange(start: Long, end: Long): List<WorkoutSet> = dao.getSetsInRange(start, end).map { it.toModel() }
    suspend fun getSetsForExerciseInRange(exerciseId: Long, start: Long, end: Long): List<WorkoutSet> = dao.getSetsForExerciseInRange(exerciseId, start, end).map { it.toModel() }
    suspend fun getDraftWorkout(): Workout? = dao.getDraftWorkout()?.toModel()
    suspend fun insertWorkout(workout: Workout): Long = dao.insertWorkout(WorkoutEntity.fromModel(workout))
    suspend fun insertSet(set: WorkoutSet): Long = dao.insertSet(WorkoutSetEntity.fromModel(set))
    suspend fun insertSets(sets: List<WorkoutSet>) = dao.insertSets(sets.map { WorkoutSetEntity.fromModel(it) })
    suspend fun updateWorkout(workout: Workout) = dao.updateWorkout(WorkoutEntity.fromModel(workout))
    suspend fun deleteWorkout(workout: Workout) = dao.deleteWorkout(WorkoutEntity.fromModel(workout))
    suspend fun deleteSetsForWorkout(workoutId: Long) = dao.deleteSetsForWorkout(workoutId)
    suspend fun getWorkoutCountInRange(start: Long, end: Long): Int = dao.getWorkoutCountInRange(start, end)
    suspend fun getTotalWorkoutCount(): Int = dao.getTotalWorkoutCount()
    suspend fun getTotalDurationInRange(start: Long, end: Long): Long = dao.getTotalDurationInRange(start, end)
    suspend fun getWorkoutsByDay(dayStart: Long, dayEnd: Long): List<Workout> = dao.getWorkoutsByDay(dayStart, dayEnd).map { it.toModel() }
}
