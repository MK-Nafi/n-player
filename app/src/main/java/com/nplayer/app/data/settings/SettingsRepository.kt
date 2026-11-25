package com.nplayer.app.data.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "nplayer_settings")

enum class MainSection { VIDEO, AUDIO, PLAYLISTS, BROWSE;

    companion object {
        fun fromName(name: String?): MainSection =
            entries.firstOrNull { it.name == name } ?: VIDEO
    }
}

/**
 * Lightweight local settings persistence using DataStore.
 * Currently stores the last-used main navigation section.
 */
class SettingsRepository(private val context: Context) {

    private val lastSectionKey = stringPreferencesKey("last_section")

    val lastSection: Flow<MainSection> = context.dataStore.data
        .map { preferences ->
            MainSection.fromName(preferences[lastSectionKey])
        }

    suspend fun setLastSection(section: MainSection) {
        context.dataStore.edit { preferences ->
            preferences[lastSectionKey] = section.name
        }
    }
}
