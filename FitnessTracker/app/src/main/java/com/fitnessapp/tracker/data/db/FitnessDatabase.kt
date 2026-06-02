package com.fitnessapp.tracker.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.fitnessapp.tracker.data.db.dao.BodyMetricDao
import com.fitnessapp.tracker.data.db.dao.ExerciseDao
import com.fitnessapp.tracker.data.db.dao.WorkoutDao
import com.fitnessapp.tracker.data.db.entity.BodyMetricEntity
import com.fitnessapp.tracker.data.db.entity.ExerciseEntity
import com.fitnessapp.tracker.data.db.entity.WorkoutEntity
import com.fitnessapp.tracker.data.db.entity.WorkoutSetEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [ExerciseEntity::class, WorkoutEntity::class, WorkoutSetEntity::class, BodyMetricEntity::class],
    version = 1,
    exportSchema = false
)
abstract class FitnessDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun bodyMetricDao(): BodyMetricDao

    companion object {
        @Volatile
        private var INSTANCE: FitnessDatabase? = null

        fun getInstance(context: Context): FitnessDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FitnessDatabase::class.java,
                    "fitness_tracker_db"
                )
                    .addCallback(SeedCallback)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private val SeedCallback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    INSTANCE?.exerciseDao()?.insertAll(PRESET_EXERCISES)
                }
            }
        }
    }
}

private val PRESET_EXERCISES = listOf(
    // 胸部
    ExerciseEntity(name = "卧推", bodyPart = "CHEST", recordType = "STRENGTH", iconName = "chest_press", isPreset = true),
    ExerciseEntity(name = "上斜卧推", bodyPart = "CHEST", recordType = "STRENGTH", iconName = "incline_press", isPreset = true),
    ExerciseEntity(name = "下斜卧推", bodyPart = "CHEST", recordType = "STRENGTH", iconName = "decline_press", isPreset = true),
    ExerciseEntity(name = "飞鸟", bodyPart = "CHEST", recordType = "STRENGTH", iconName = "fly", isPreset = true),
    ExerciseEntity(name = "上斜飞鸟", bodyPart = "CHEST", recordType = "STRENGTH", iconName = "incline_fly", isPreset = true),
    ExerciseEntity(name = "绳索夹胸", bodyPart = "CHEST", recordType = "STRENGTH", iconName = "cable_fly", isPreset = true),
    // 背部
    ExerciseEntity(name = "引体向上", bodyPart = "BACK", recordType = "REPS", iconName = "pullup", isPreset = true),
    ExerciseEntity(name = "杠铃划船", bodyPart = "BACK", recordType = "STRENGTH", iconName = "barbell_row", isPreset = true),
    ExerciseEntity(name = "哑铃划船", bodyPart = "BACK", recordType = "STRENGTH", iconName = "dumbbell_row", isPreset = true),
    ExerciseEntity(name = "高位下拉", bodyPart = "BACK", recordType = "STRENGTH", iconName = "lat_pulldown", isPreset = true),
    ExerciseEntity(name = "坐姿划船", bodyPart = "BACK", recordType = "STRENGTH", iconName = "seated_row", isPreset = true),
    ExerciseEntity(name = "硬拉", bodyPart = "BACK", recordType = "STRENGTH", iconName = "deadlift", isPreset = true),
    // 腿部
    ExerciseEntity(name = "深蹲", bodyPart = "LEGS", recordType = "STRENGTH", iconName = "squat", isPreset = true),
    ExerciseEntity(name = "前蹲", bodyPart = "LEGS", recordType = "STRENGTH", iconName = "front_squat", isPreset = true),
    ExerciseEntity(name = "腿举", bodyPart = "LEGS", recordType = "STRENGTH", iconName = "leg_press", isPreset = true),
    ExerciseEntity(name = "腿弯举", bodyPart = "LEGS", recordType = "STRENGTH", iconName = "leg_curl", isPreset = true),
    ExerciseEntity(name = "腿屈伸", bodyPart = "LEGS", recordType = "STRENGTH", iconName = "leg_extension", isPreset = true),
    ExerciseEntity(name = "弓步蹲", bodyPart = "LEGS", recordType = "STRENGTH", iconName = "lunge", isPreset = true),
    ExerciseEntity(name = "罗马尼亚硬拉", bodyPart = "LEGS", recordType = "STRENGTH", iconName = "romanian_deadlift", isPreset = true),
    // 肩部
    ExerciseEntity(name = "推举", bodyPart = "SHOULDERS", recordType = "STRENGTH", iconName = "overhead_press", isPreset = true),
    ExerciseEntity(name = "侧平举", bodyPart = "SHOULDERS", recordType = "STRENGTH", iconName = "lateral_raise", isPreset = true),
    ExerciseEntity(name = "前平举", bodyPart = "SHOULDERS", recordType = "STRENGTH", iconName = "front_raise", isPreset = true),
    ExerciseEntity(name = "面拉", bodyPart = "SHOULDERS", recordType = "STRENGTH", iconName = "face_pull", isPreset = true),
    ExerciseEntity(name = "反向飞鸟", bodyPart = "SHOULDERS", recordType = "STRENGTH", iconName = "reverse_fly", isPreset = true),
    // 手臂
    ExerciseEntity(name = "二头弯举", bodyPart = "ARMS", recordType = "STRENGTH", iconName = "bicep_curl", isPreset = true),
    ExerciseEntity(name = "锤式弯举", bodyPart = "ARMS", recordType = "STRENGTH", iconName = "hammer_curl", isPreset = true),
    ExerciseEntity(name = "三头下压", bodyPart = "ARMS", recordType = "STRENGTH", iconName = "tricep_pushdown", isPreset = true),
    ExerciseEntity(name = "窄距卧推", bodyPart = "ARMS", recordType = "STRENGTH", iconName = "close_grip_press", isPreset = true),
    ExerciseEntity(name = "法式弯举", bodyPart = "ARMS", recordType = "STRENGTH", iconName = "skull_crusher", isPreset = true),
    // 核心
    ExerciseEntity(name = "卷腹", bodyPart = "CORE", recordType = "REPS", iconName = "crunch", isPreset = true),
    ExerciseEntity(name = "平板支撑", bodyPart = "CORE", recordType = "DURATION", iconName = "plank", isPreset = true),
    ExerciseEntity(name = "举腿", bodyPart = "CORE", recordType = "REPS", iconName = "leg_raise", isPreset = true),
    ExerciseEntity(name = "俄罗斯转体", bodyPart = "CORE", recordType = "REPS", iconName = "russian_twist", isPreset = true),
)
