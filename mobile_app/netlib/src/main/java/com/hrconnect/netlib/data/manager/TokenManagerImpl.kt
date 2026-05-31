package com.hrconnect.netlib.data.manager

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.hrconnect.netlib.common.util.Constants
import com.hrconnect.netlib.domain.manager.TokenManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(Constants.DATASTORE_FILE_NAME)

class TokenManagerImpl(
    private val context: Context,
) : TokenManager {

    private val tokenKey = stringPreferencesKey("token")

    override suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[tokenKey] = token
        }
    }

    override fun getToken(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[tokenKey]
        }
    }
}
