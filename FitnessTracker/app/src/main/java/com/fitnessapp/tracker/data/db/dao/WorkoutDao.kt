package com.fitnessapp.tracker.data.db.dao

import androidx.room.*
import com.fitnessapp.tracker.data.db.entity.WorkoutEntity
import com.fitnessapp.tracker.data.db.entity.WorkoutSetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workouts WHERE isDraft = 0 ORDER BY date DESC")
    fun getAllWorkouts(): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE isDraft = 0 AND date >= :start AND date <= :end ORDER BY date ASC")
    fun getWorkoutsInRange(start: Long, end: Long): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE isDraft = 0 AND date = :date LIMIT 1")
    suspend fun getWorkoutByDate(date: Long): WorkoutEntity?

    @Query("SELECT * FROM workouts WHERE id = :id")
    suspend fun getWorkoutById(id: Long): WorkoutEntity?

    @Query("SELECT * FROM workout_sets WHERE workoutId = :workoutId ORDER BY setNumber ASC")
    suspend fun getSetsForWorkout(workoutId: Long): List<WorkoutSetEntity>

    @Query("SELECT * FROM workout_sets WHERE workoutId = :workoutId AND exerciseId = :exerciseId ORDER BY setNumber ASC")
    suspend fun getSetsForExercise(workoutId: Long, exerciseId: Long): List<WorkoutSetEntity>

    @Query("SELECT * FROM workout_sets WHERE workoutId IN (SELECT id FROM workouts WHERE isDraft = 0 AND date >= :start AND date <= :end) ORDER BY setNumber ASC")
    suspend fun getSetsInRange(start: Long, end: Long): List<WorkoutSetEntity>

    @Query("SELECT ws.* FROM workout_sets ws INNER JOIN workouts w ON w.id = ws.workoutId WHERE w.isDraft = 0 AND ws.exerciseId = :exerciseId AND w.date >= :start AND w.date <= :end ORDER BY w.date ASC, ws.setNumber ASC")
    suspend fun getSetsForExerciseInRange(exerciseId: Long, start: Long, end: Long): List<WorkoutSetEntity>

    @Query("SELECT * FROM workouts WHERE isDraft = 1 LIMIT 1")
    suspend fun getDraftWorkout(): WorkoutEntity?

    @Insert
    suspend fun insertWorkout(workout: WorkoutEntity): Long

    @Insert
    suspend fun insertSet(set: WorkoutSetEntity): Long

    @Insert
    suspend fun insertSets(sets: List<WorkoutSetEntity>)

    @Update
    suspend fun updateWorkout(workout: WorkoutEntity)

    @Delete
    suspend fun deleteWorkout(workout: WorkoutEntity)

    @Query("DELETE FROM workout_sets WHERE workoutId = :workoutId")
    suspend fun deleteSetsForWorkout(workoutId: Long)

    @Query("SELECT COUNT(*) FROM workouts WHERE isDraft = 0 AND date >= :start AND date <= :end")
    suspend fun getWorkoutCountInRange(start: Long, end: Long): Int

    @Query("SELECT COUNT(*) FROM workouts WHERE isDraft = 0")
    suspend fun getTotalWorkoutCount(): Int

    @Query("SELECT COALESCE(SUM(CAST((endTime - startTime) AS REAL)), 0) FROM workouts WHERE isDraft = 0 AND date >= :start AND date <= :end")
    suspend fun getTotalDurationInRange(start: Long, end: Long): Long
}
