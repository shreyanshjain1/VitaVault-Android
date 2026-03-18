package com.vitavault.mobile.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "vitavault_mobile")

class SessionStore(private val context: Context) {
    private val tokenKey = stringPreferencesKey("auth_token")
    private val baseUrlKey = stringPreferencesKey("base_url")
    private val emailKey = stringPreferencesKey("last_email")
    private val lastSyncSummaryKey = stringPreferencesKey("last_sync_summary")

    val tokenFlow: Flow<String?> = context.dataStore.data.map { it[tokenKey] }
    val baseUrlFlow: Flow<String?> = context.dataStore.data.map { it[baseUrlKey] }
    val emailFlow: Flow<String?> = context.dataStore.data.map { it[emailKey] }
    val lastSyncSummaryFlow: Flow<String?> = context.dataStore.data.map { it[lastSyncSummaryKey] }

    suspend fun saveToken(token: String?) {
        context.dataStore.edit { prefs ->
            if (token == null) prefs.remove(tokenKey) else prefs[tokenKey] = token
        }
    }

    suspend fun saveBaseUrl(baseUrl: String) {
        context.dataStore.edit { prefs -> prefs[baseUrlKey] = baseUrl.trim() }
    }

    suspend fun saveLastEmail(email: String) {
        context.dataStore.edit { prefs -> prefs[emailKey] = email.trim() }
    }

    suspend fun saveLastSyncSummary(value: String?) {
        context.dataStore.edit { prefs ->
            if (value == null) prefs.remove(lastSyncSummaryKey) else prefs[lastSyncSummaryKey] = value
        }
    }
}
