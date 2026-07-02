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
    version = 4,
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
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private val SeedCallback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                seedDatabase(db)
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                seedDatabase(db)
            }

            private fun seedDatabase(db: SupportSQLiteDatabase) {
                db.beginTransaction()
                try {
                    val cursor = db.query("SELECT name FROM exercises")
                    val existingNames = HashSet<String>()
                    while (cursor.moveToNext()) {
                        existingNames.add(cursor.getString(0))
                    }
                    cursor.close()

                    val toInsert = PRESET_EXERCISES.filter { it.name !in existingNames }
                    if (toInsert.isNotEmpty()) {
                        val stmt = db.compileStatement(
                            "INSERT INTO exercises (name, bodyPart, equipment, recordType, iconName, isPreset, createdAt) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)"
                        )
                        for (ex in toInsert) {
                            stmt.bindString(1, ex.name)
                            stmt.bindString(2, ex.bodyPart)
                            stmt.bindString(3, ex.equipment)
                            stmt.bindString(4, ex.recordType)
                            stmt.bindString(5, ex.iconName)
                            stmt.bindLong(6, if (ex.isPreset) 1 else 0)
                            stmt.bindLong(7, ex.createdAt)
                            stmt.executeInsert()
                            stmt.clearBindings()
                        }
                    }
                    db.setTransactionSuccessful()
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    db.endTransaction()
                }
            }
        }
    }
}

private val PRESET_EXERCISES = listOf(
    // 胸部
    ExerciseEntity(name = "杠铃平卧推", bodyPart = "CHEST", equipment = "BARBELL", recordType = "STRENGTH", iconName = "chest_press", isPreset = true),
    ExerciseEntity(name = "杠铃上斜卧推", bodyPart = "CHEST", equipment = "BARBELL", recordType = "STRENGTH", iconName = "incline_press", isPreset = true),
    ExerciseEntity(name = "杠铃下斜卧推", bodyPart = "CHEST", equipment = "BARBELL", recordType = "STRENGTH", iconName = "decline_press", isPreset = true),
    ExerciseEntity(name = "哑铃平卧推", bodyPart = "CHEST", equipment = "DUMBBELL", recordType = "STRENGTH", iconName = "chest_press", isPreset = true),
    ExerciseEntity(name = "哑铃上斜卧推", bodyPart = "CHEST", equipment = "DUMBBELL", recordType = "STRENGTH", iconName = "incline_press", isPreset = true),
    ExerciseEntity(name = "哑铃飞鸟", bodyPart = "CHEST", equipment = "DUMBBELL", recordType = "STRENGTH", iconName = "fly", isPreset = true),
    ExerciseEntity(name = "哑铃上斜飞鸟", bodyPart = "CHEST", equipment = "DUMBBELL", recordType = "STRENGTH", iconName = "incline_fly", isPreset = true),
    ExerciseEntity(name = "蝴蝶机夹胸", bodyPart = "CHEST", equipment = "MACHINE", recordType = "STRENGTH", iconName = "fly", isPreset = true),
    ExerciseEntity(name = "坐姿推胸机", bodyPart = "CHEST", equipment = "MACHINE", recordType = "STRENGTH", iconName = "chest_press", isPreset = true),
    ExerciseEntity(name = "史密斯卧推", bodyPart = "CHEST", equipment = "SMITH_MACHINE", recordType = "STRENGTH", iconName = "chest_press", isPreset = true),
    ExerciseEntity(name = "绳索夹胸", bodyPart = "CHEST", equipment = "CABLE", recordType = "STRENGTH", iconName = "cable_fly", isPreset = true),
    ExerciseEntity(name = "弹力带夹胸", bodyPart = "CHEST", equipment = "RESISTANCE_BAND", recordType = "STRENGTH", iconName = "cable_fly", isPreset = true),
    ExerciseEntity(name = "双杠臂屈伸", bodyPart = "CHEST", equipment = "BODYWEIGHT", recordType = "REPS", iconName = "dips", isPreset = true),
    ExerciseEntity(name = "俯卧撑", bodyPart = "CHEST", equipment = "BODYWEIGHT", recordType = "REPS", iconName = "pushups", isPreset = true),

    // 背部
    ExerciseEntity(name = "引体向上", bodyPart = "BACK", equipment = "BODYWEIGHT", recordType = "REPS", iconName = "pullup", isPreset = true),
    ExerciseEntity(name = "杠铃划船", bodyPart = "BACK", equipment = "BARBELL", recordType = "STRENGTH", iconName = "barbell_row", isPreset = true),
    ExerciseEntity(name = "哑铃划船", bodyPart = "BACK", equipment = "DUMBBELL", recordType = "STRENGTH", iconName = "dumbbell_row", isPreset = true),
    ExerciseEntity(name = "单臂哑铃划船", bodyPart = "BACK", equipment = "DUMBBELL", recordType = "STRENGTH", iconName = "dumbbell_row", isPreset = true),
    ExerciseEntity(name = "高位下拉", bodyPart = "BACK", equipment = "MACHINE", recordType = "STRENGTH", iconName = "lat_pulldown", isPreset = true),
    ExerciseEntity(name = "坐姿划船", bodyPart = "BACK", equipment = "CABLE", recordType = "STRENGTH", iconName = "seated_row", isPreset = true),
    ExerciseEntity(name = "硬拉", bodyPart = "BACK", equipment = "BARBELL", recordType = "STRENGTH", iconName = "deadlift", isPreset = true),
    ExerciseEntity(name = "直臂下拉", bodyPart = "BACK", equipment = "CABLE", recordType = "STRENGTH", iconName = "straight_arm_pulldown", isPreset = true),
    ExerciseEntity(name = "山羊挺身", bodyPart = "BACK", equipment = "BODYWEIGHT", recordType = "REPS", iconName = "back_extension", isPreset = true),
    ExerciseEntity(name = "T杠划船", bodyPart = "BACK", equipment = "BARBELL", recordType = "STRENGTH", iconName = "barbell_row", isPreset = true),
    ExerciseEntity(name = "史密斯划船", bodyPart = "BACK", equipment = "SMITH_MACHINE", recordType = "STRENGTH", iconName = "barbell_row", isPreset = true),
    ExerciseEntity(name = "弹力带辅助引体", bodyPart = "BACK", equipment = "RESISTANCE_BAND", recordType = "REPS", iconName = "pullup", isPreset = true),
    ExerciseEntity(name = "壶铃单臂划船", bodyPart = "BACK", equipment = "KETTLEBELL", recordType = "STRENGTH", iconName = "dumbbell_row", isPreset = true),

    // 腿部
    ExerciseEntity(name = "杠铃深蹲", bodyPart = "LEGS", equipment = "BARBELL", recordType = "STRENGTH", iconName = "squat", isPreset = true),
    ExerciseEntity(name = "杠铃前蹲", bodyPart = "LEGS", equipment = "BARBELL", recordType = "STRENGTH", iconName = "front_squat", isPreset = true),
    ExerciseEntity(name = "腿举", bodyPart = "LEGS", equipment = "MACHINE", recordType = "STRENGTH", iconName = "leg_press", isPreset = true),
    ExerciseEntity(name = "腿弯举", bodyPart = "LEGS", equipment = "MACHINE", recordType = "STRENGTH", iconName = "leg_curl", isPreset = true),
    ExerciseEntity(name = "腿屈伸", bodyPart = "LEGS", equipment = "MACHINE", recordType = "STRENGTH", iconName = "leg_extension", isPreset = true),
    ExerciseEntity(name = "哑铃弓步蹲", bodyPart = "LEGS", equipment = "DUMBBELL", recordType = "STRENGTH", iconName = "lunge", isPreset = true),
    ExerciseEntity(name = "罗马尼亚硬拉", bodyPart = "LEGS", equipment = "BARBELL", recordType = "STRENGTH", iconName = "romanian_deadlift", isPreset = true),
    ExerciseEntity(name = "杠铃臀推", bodyPart = "LEGS", equipment = "BARBELL", recordType = "STRENGTH", iconName = "hip_thrust", isPreset = true),
    ExerciseEntity(name = "哑铃提踵", bodyPart = "LEGS", equipment = "DUMBBELL", recordType = "STRENGTH", iconName = "calf_raise", isPreset = true),
    ExerciseEntity(name = "哑铃高加索深蹲", bodyPart = "LEGS", equipment = "DUMBBELL", recordType = "STRENGTH", iconName = "squat", isPreset = true),
    ExerciseEntity(name = "史密斯深蹲", bodyPart = "LEGS", equipment = "SMITH_MACHINE", recordType = "STRENGTH", iconName = "squat", isPreset = true),
    ExerciseEntity(name = "弹力带侧步走", bodyPart = "LEGS", equipment = "RESISTANCE_BAND", recordType = "REPS", iconName = "lunge", isPreset = true),
    ExerciseEntity(name = "壶铃摇摆", bodyPart = "LEGS", equipment = "KETTLEBELL", recordType = "STRENGTH", iconName = "kettlebell_swing", isPreset = true),
    ExerciseEntity(name = "哑铃保加利亚单腿蹲", bodyPart = "LEGS", equipment = "DUMBBELL", recordType = "STRENGTH", iconName = "lunge", isPreset = true),

    // 肩部
    ExerciseEntity(name = "杠铃推举", bodyPart = "SHOULDERS", equipment = "BARBELL", recordType = "STRENGTH", iconName = "overhead_press", isPreset = true),
    ExerciseEntity(name = "哑铃侧平举", bodyPart = "SHOULDERS", equipment = "DUMBBELL", recordType = "STRENGTH", iconName = "lateral_raise", isPreset = true),
    ExerciseEntity(name = "哑铃前平举", bodyPart = "SHOULDERS", equipment = "DUMBBELL", recordType = "STRENGTH", iconName = "front_raise", isPreset = true),
    ExerciseEntity(name = "绳索面拉", bodyPart = "SHOULDERS", equipment = "CABLE", recordType = "STRENGTH", iconName = "face_pull", isPreset = true),
    ExerciseEntity(name = "哑铃反向飞鸟", bodyPart = "SHOULDERS", equipment = "DUMBBELL", recordType = "STRENGTH", iconName = "reverse_fly", isPreset = true),
    ExerciseEntity(name = "阿诺德推举", bodyPart = "SHOULDERS", equipment = "DUMBBELL", recordType = "STRENGTH", iconName = "arnold_press", isPreset = true),
    ExerciseEntity(name = "杠铃耸肩", bodyPart = "SHOULDERS", equipment = "BARBELL", recordType = "STRENGTH", iconName = "shrugs", isPreset = true),
    ExerciseEntity(name = "哑铃推举", bodyPart = "SHOULDERS", equipment = "DUMBBELL", recordType = "STRENGTH", iconName = "overhead_press", isPreset = true),
    ExerciseEntity(name = "绳索侧平举", bodyPart = "SHOULDERS", equipment = "CABLE", recordType = "STRENGTH", iconName = "lateral_raise", isPreset = true),
    ExerciseEntity(name = "史密斯推举", bodyPart = "SHOULDERS", equipment = "SMITH_MACHINE", recordType = "STRENGTH", iconName = "overhead_press", isPreset = true),
    ExerciseEntity(name = "弹力带面拉", bodyPart = "SHOULDERS", equipment = "RESISTANCE_BAND", recordType = "STRENGTH", iconName = "face_pull", isPreset = true),
    ExerciseEntity(name = "壶铃单臂推举", bodyPart = "SHOULDERS", equipment = "KETTLEBELL", recordType = "STRENGTH", iconName = "overhead_press", isPreset = true),

    // 手臂
    ExerciseEntity(name = "哑铃二头弯举", bodyPart = "ARMS", equipment = "DUMBBELL", recordType = "STRENGTH", iconName = "bicep_curl", isPreset = true),
    ExerciseEntity(name = "哑铃锤式弯举", bodyPart = "ARMS", equipment = "DUMBBELL", recordType = "STRENGTH", iconName = "hammer_curl", isPreset = true),
    ExerciseEntity(name = "绳索三头下压", bodyPart = "ARMS", equipment = "CABLE", recordType = "STRENGTH", iconName = "tricep_pushdown", isPreset = true),
    ExerciseEntity(name = "杠铃窄距卧推", bodyPart = "ARMS", equipment = "BARBELL", recordType = "STRENGTH", iconName = "close_grip_press", isPreset = true),
    ExerciseEntity(name = "杠铃法式弯举", bodyPart = "ARMS", equipment = "BARBELL", recordType = "STRENGTH", iconName = "skull_crusher", isPreset = true),
    ExerciseEntity(name = "杠铃牧师椅弯举", bodyPart = "ARMS", equipment = "BARBELL", recordType = "STRENGTH", iconName = "preacher_curl", isPreset = true),
    ExerciseEntity(name = "哑铃俯身臂屈伸", bodyPart = "ARMS", equipment = "DUMBBELL", recordType = "STRENGTH", iconName = "tricep_kickback", isPreset = true),
    ExerciseEntity(name = "杠铃弯举", bodyPart = "ARMS", equipment = "BARBELL", recordType = "STRENGTH", iconName = "bicep_curl", isPreset = true),
    ExerciseEntity(name = "绳索二头弯举", bodyPart = "ARMS", equipment = "CABLE", recordType = "STRENGTH", iconName = "bicep_curl", isPreset = true),
    ExerciseEntity(name = "仰卧后撑", bodyPart = "ARMS", equipment = "BODYWEIGHT", recordType = "REPS", iconName = "dips", isPreset = true),
    ExerciseEntity(name = "弹力带二头弯举", bodyPart = "ARMS", equipment = "RESISTANCE_BAND", recordType = "STRENGTH", iconName = "bicep_curl", isPreset = true),

    // 核心
    ExerciseEntity(name = "卷腹", bodyPart = "CORE", equipment = "BODYWEIGHT", recordType = "REPS", iconName = "crunch", isPreset = true),
    ExerciseEntity(name = "平板支撑", bodyPart = "CORE", equipment = "BODYWEIGHT", recordType = "DURATION", iconName = "plank", isPreset = true),
    ExerciseEntity(name = "躺姿举腿", bodyPart = "CORE", equipment = "BODYWEIGHT", recordType = "REPS", iconName = "leg_raise", isPreset = true),
    ExerciseEntity(name = "俄罗斯转体", bodyPart = "CORE", equipment = "BODYWEIGHT", recordType = "REPS", iconName = "russian_twist", isPreset = true),
    ExerciseEntity(name = "悬挂举腿", bodyPart = "CORE", equipment = "BODYWEIGHT", recordType = "REPS", iconName = "hanging_leg_raise", isPreset = true),
    ExerciseEntity(name = "悬挂抬膝", bodyPart = "CORE", equipment = "BODYWEIGHT", recordType = "REPS", iconName = "hanging_leg_raise", isPreset = true),
    ExerciseEntity(name = "跪姿健腹轮", bodyPart = "CORE", equipment = "OTHER", recordType = "REPS", iconName = "ab_roller", isPreset = true),
    ExerciseEntity(name = "负重俄罗斯转体", bodyPart = "CORE", equipment = "DUMBBELL", recordType = "REPS", iconName = "russian_twist", isPreset = true),
    ExerciseEntity(name = "绳索负重卷腹", bodyPart = "CORE", equipment = "CABLE", recordType = "STRENGTH", iconName = "crunch", isPreset = true),

    // 全身
    ExerciseEntity(name = "波比跳", bodyPart = "FULL_BODY", equipment = "BODYWEIGHT", recordType = "REPS", iconName = "burpee", isPreset = true),
    ExerciseEntity(name = "深蹲推举", bodyPart = "FULL_BODY", equipment = "BARBELL", recordType = "STRENGTH", iconName = "thruster", isPreset = true),
    ExerciseEntity(name = "壶铃单臂抓举", bodyPart = "FULL_BODY", equipment = "KETTLEBELL", recordType = "STRENGTH", iconName = "kettlebell_swing", isPreset = true),
    ExerciseEntity(name = "药球砸墙", bodyPart = "FULL_BODY", equipment = "OTHER", recordType = "REPS", iconName = "burpee", isPreset = true),

    // 有氧
    ExerciseEntity(name = "跑步机跑步", bodyPart = "CARDIO", equipment = "CARDIO_MACHINE", recordType = "DURATION", iconName = "treadmill", isPreset = true),
    ExerciseEntity(name = "户外跑步", bodyPart = "CARDIO", equipment = "OTHER", recordType = "DURATION", iconName = "treadmill", isPreset = true),
    ExerciseEntity(name = "椭圆机", bodyPart = "CARDIO", equipment = "CARDIO_MACHINE", recordType = "DURATION", iconName = "cycling", isPreset = true),
    ExerciseEntity(name = "爬楼机", bodyPart = "CARDIO", equipment = "CARDIO_MACHINE", recordType = "DURATION", iconName = "cycling", isPreset = true),
    ExerciseEntity(name = "划船机", bodyPart = "CARDIO", equipment = "CARDIO_MACHINE", recordType = "DURATION", iconName = "rower", isPreset = true),
    ExerciseEntity(name = "动感单车", bodyPart = "CARDIO", equipment = "CARDIO_MACHINE", recordType = "DURATION", iconName = "cycling", isPreset = true),
    ExerciseEntity(name = "户外骑行", bodyPart = "CARDIO", equipment = "OTHER", recordType = "DURATION", iconName = "cycling", isPreset = true),
    ExerciseEntity(name = "跳绳", bodyPart = "CARDIO", equipment = "OTHER", recordType = "DURATION", iconName = "jump_rope", isPreset = true),
)
