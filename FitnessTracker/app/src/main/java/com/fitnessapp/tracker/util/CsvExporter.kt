package com.fitnessapp.tracker.util

import android.content.Context
import android.net.Uri
import com.fitnessapp.tracker.data.model.Workout
import com.fitnessapp.tracker.data.model.WorkoutSet
import java.io.OutputStreamWriter

object CsvExporter {
    fun exportWorkouts(context: Context, uri: Uri, workouts: List<Workout>, allSets: Map<Long, List<WorkoutSet>>) {
        OutputStreamWriter(context.contentResolver.openOutputStream(uri)).use { writer ->
            writer.write("日期,开始时间,结束时间,动作ID,组号,重量(kg),次数,时长(秒)\n")
            workouts.forEach { workout ->
                val sets = allSets[workout.id] ?: emptyList()
                if (sets.isEmpty()) {
                    writer.write("${DateUtils.formatDate(workout.date)},${DateUtils.formatTime(workout.startTime)},${workout.endTime?.let { DateUtils.formatTime(it) } ?: ""},,,,\n")
                } else {
                    sets.forEach { set ->
                        writer.write(
                            "${DateUtils.formatDate(workout.date)}," +
                            "${DateUtils.formatTime(workout.startTime)}," +
                            "${workout.endTime?.let { DateUtils.formatTime(it) } ?: ""}," +
                            "${set.exerciseId},${set.setNumber}," +
                            "${set.weight ?: ""},${set.reps ?: ""},${set.durationSeconds ?: ""}\n"
                        )
                    }
                }
            }
        }
    }
}
