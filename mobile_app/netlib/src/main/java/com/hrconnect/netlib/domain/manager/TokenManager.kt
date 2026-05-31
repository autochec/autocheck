package com.hrconnect.netlib.domain.manager

import kotlinx.coroutines.flow.Flow

interface TokenManager {
    suspend fun saveToken(token: String)
    fun getToken(): Flow<String?>
}
