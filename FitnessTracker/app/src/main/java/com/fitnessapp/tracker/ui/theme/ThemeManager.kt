package com.fitnessapp.tracker.ui.theme

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class ThemeManager(private val context: Context) {
    companion object {
        private val THEME_INDEX_KEY = intPreferencesKey("theme_index")
        private val UNIT_KEY = intPreferencesKey("unit")
    }

    val themeIndex: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[THEME_INDEX_KEY] ?: 0
    }

    val unit: Flow<String> = context.dataStore.data.map { preferences ->
        if (preferences[UNIT_KEY] == 1) "lb" else "kg"
    }

    suspend fun setThemeIndex(index: Int) {
        context.dataStore.edit { preferences ->
            preferences[THEME_INDEX_KEY] = index
        }
    }

    suspend fun setUnit(unit: String) {
        context.dataStore.edit { preferences ->
            preferences[UNIT_KEY] = if (unit == "lb") 1 else 0
        }
    }
}
