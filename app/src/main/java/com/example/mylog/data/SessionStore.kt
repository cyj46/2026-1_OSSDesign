package com.example.mylog.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore by preferencesDataStore(name = "session")

class SessionStore(private val context: Context) {
    private val userIdKey = longPreferencesKey("current_user_id")

    val currentUserId: Flow<Long?> = context.sessionDataStore.data.map { preferences ->
        preferences[userIdKey]
    }

    suspend fun setCurrentUser(userId: Long) {
        context.sessionDataStore.edit { preferences ->
            preferences[userIdKey] = userId
        }
    }

    suspend fun clear() {
        context.sessionDataStore.edit { preferences ->
            preferences.remove(userIdKey)
        }
    }
}
