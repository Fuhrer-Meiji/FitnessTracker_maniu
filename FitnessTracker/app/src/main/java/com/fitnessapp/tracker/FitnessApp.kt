package com.fitnessapp.tracker

import android.app.Application
import com.fitnessapp.tracker.data.db.FitnessDatabase

class FitnessApp : Application() {
    val database by lazy { FitnessDatabase.getInstance(this) }
}
