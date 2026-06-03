package com.fitnessapp.tracker.data.db.dao

import androidx.room.*
import com.fitnessapp.tracker.data.db.entity.WorkoutEntity
import com.fitnessapp.tracker.data.db.entity.WorkoutSetEntity
import com.fitnessapp.tracker.data.db.entity.WorkoutSetWithExercise
import com.fitnessapp.tracker.data.db.entity.SetTrendData
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workouts WHERE isDraft = 0 ORDER BY date DESC")
    fun getAllWorkouts(): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE isDraft = 0 AND date >= :start AND date <= :end ORDER BY date ASC")
    fun getWorkoutsInRange(start: Long, end: Long): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE isDraft = 0 AND date >= :dayStart AND date <= :dayEnd ORDER BY date ASC")
    suspend fun getWorkoutsByDay(dayStart: Long, dayEnd: Long): List<WorkoutEntity>

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

    @Delete
    suspend fun deleteSet(set: WorkoutSetEntity)

    @Query("DELETE FROM workouts WHERE id = :id")
    suspend fun deleteWorkoutById(id: Long)

    @Query("SELECT COUNT(*) FROM workouts WHERE isDraft = 0 AND date >= :start AND date <= :end")
    suspend fun getWorkoutCountInRange(start: Long, end: Long): Int

    @Query("SELECT COUNT(*) FROM workouts WHERE isDraft = 0")
    suspend fun getTotalWorkoutCount(): Int

    @Query("SELECT COALESCE(SUM(CAST((endTime - startTime) AS REAL)), 0) FROM workouts WHERE isDraft = 0 AND date >= :start AND date <= :end")
    suspend fun getTotalDurationInRange(start: Long, end: Long): Long

    @Query("SELECT * FROM workout_sets WHERE exerciseId = :exerciseId AND workoutId IN (SELECT id FROM workouts WHERE isDraft = 0) ORDER BY id DESC LIMIT 1")
    suspend fun getLastSetForExercise(exerciseId: Long): WorkoutSetEntity?

    @Query("SELECT ws.id, ws.workoutId, w.date, w.note as workoutNote, ws.exerciseId, e.name as exerciseName, ws.setNumber, ws.recordType, ws.weight, ws.reps, ws.durationSeconds, ws.restSeconds FROM workout_sets ws INNER JOIN workouts w ON ws.workoutId = w.id INNER JOIN exercises e ON ws.exerciseId = e.id WHERE w.isDraft = 0 AND w.date >= :dayStart AND w.date < :dayEnd ORDER BY w.date ASC, ws.workoutId ASC, ws.exerciseId ASC, ws.setNumber ASC")
    suspend fun getWorkoutSetsWithExerciseByDay(dayStart: Long, dayEnd: Long): List<WorkoutSetWithExercise>

    @Query("SELECT w.date, ws.weight, ws.reps, ws.durationSeconds FROM workout_sets ws INNER JOIN workouts w ON ws.workoutId = w.id WHERE w.isDraft = 0 AND ws.exerciseId = :exerciseId AND w.date >= :start ORDER BY w.date ASC, ws.setNumber ASC")
    suspend fun getSetsForExerciseFromDate(exerciseId: Long, start: Long): List<SetTrendData>
}
