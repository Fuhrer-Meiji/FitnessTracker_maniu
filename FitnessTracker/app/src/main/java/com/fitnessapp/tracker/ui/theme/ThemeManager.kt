package com.fitnessapp.tracker.ui.theme

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class ThemeManager(private val context: Context) {
    companion object {
        private val THEME_INDEX_KEY = intPreferencesKey("theme_index")
        private val UNIT_KEY = intPreferencesKey("unit")
        private val BG_IMAGE_ENABLED_KEY = booleanPreferencesKey("bg_image_enabled")
        private val BG_IMAGE_PATH_KEY = stringPreferencesKey("bg_image_path")
    }

    val themeIndex: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[THEME_INDEX_KEY] ?: 0
    }

    val unit: Flow<String> = context.dataStore.data.map { preferences ->
        if (preferences[UNIT_KEY] == 1) "lb" else "kg"
    }

    val bgImageEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[BG_IMAGE_ENABLED_KEY] ?: false
    }

    val bgImagePath: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[BG_IMAGE_PATH_KEY]
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

    suspend fun setBgImageEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[BG_IMAGE_ENABLED_KEY] = enabled
        }
    }

    suspend fun setBgImagePath(path: String?) {
        context.dataStore.edit { preferences ->
            if (path == null) {
                preferences.remove(BG_IMAGE_PATH_KEY)
            } else {
                preferences[BG_IMAGE_PATH_KEY] = path
            }
        }
    }
}
