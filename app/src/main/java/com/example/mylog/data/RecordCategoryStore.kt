package com.example.mylog.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.recordCategoryDataStore by preferencesDataStore(name = "record_categories")

class RecordCategoryStore(private val context: Context) {
    private val customTypesKey = stringSetPreferencesKey("custom_record_types")

    val customTypes: Flow<List<String>> = context.recordCategoryDataStore.data.map { preferences ->
        preferences[customTypesKey]
            .orEmpty()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
            .sorted()
    }

    suspend fun add(type: String) {
        val cleaned = type.trim()
        context.recordCategoryDataStore.edit { preferences ->
            preferences[customTypesKey] = preferences[customTypesKey].orEmpty() + cleaned
        }
    }
}
