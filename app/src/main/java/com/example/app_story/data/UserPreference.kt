package com.example.app_story.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: androidx.datastore.core.DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreference private constructor(private val dataStore: androidx.datastore.core.DataStore<Preferences>) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("token")
        private val NAME_KEY = stringPreferencesKey("name") // Key untuk nama pengguna
        private val LOGIN_STATE_KEY = booleanPreferencesKey("is_logged_in")

        @Volatile
        private var INSTANCE: UserPreference? = null

        fun getInstance(context: Context): UserPreference {
            return INSTANCE ?: synchronized(this) {
                val instance = UserPreference(context.dataStore)
                INSTANCE = instance
                instance
            }
        }
    }

    // Menyimpan token, nama pengguna, dan status login
    suspend fun saveUserData(token: String, name: String) {
        dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            preferences[NAME_KEY] = name
            preferences[LOGIN_STATE_KEY] = true // Menandakan pengguna sudah login
        }
    }

    // Mendapatkan token
    fun getToken(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[TOKEN_KEY]
        }
    }

    // Mendapatkan nama pengguna
    fun getName(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[NAME_KEY]
        }
    }

    // Menyimpan hanya token (misalnya untuk kasus token refresh)
    suspend fun saveToken(token: String) {
        dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            preferences[LOGIN_STATE_KEY] = true // Menandakan pengguna sudah login
        }
    }

    // Menghapus token, nama pengguna, dan status login (untuk logout)
    suspend fun clearUserData() {
        dataStore.edit { preferences ->
            preferences.clear() // Menghapus seluruh data, termasuk status login
        }
    }

    // Memeriksa apakah pengguna sudah login
    fun isLoggedIn(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[LOGIN_STATE_KEY] ?: false // Default false jika belum login
        }
    }
}
