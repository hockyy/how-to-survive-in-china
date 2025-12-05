package id.hocky.miteiru.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {
    
    companion object {
        private val API_KEY = stringPreferencesKey("openrouter_api_key")
        private val SELECTED_MODEL = stringPreferencesKey("selected_model")
        private val SOURCE_LANGUAGE = stringPreferencesKey("source_language")
    }

    val apiKey: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[API_KEY] ?: ""
    }

    val selectedModel: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SELECTED_MODEL] ?: "google/gemini-2.0-flash-001"
    }

    val sourceLanguage: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SOURCE_LANGUAGE] ?: "Cantonese"
    }

    suspend fun saveApiKey(apiKey: String) {
        context.dataStore.edit { preferences ->
            preferences[API_KEY] = apiKey
        }
    }

    suspend fun saveSelectedModel(model: String) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_MODEL] = model
        }
    }

    suspend fun saveSourceLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[SOURCE_LANGUAGE] = language
        }
    }
}

