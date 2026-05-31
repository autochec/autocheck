package com.hrconnect.netlib.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Ответ на запрос входа.
 *
 * @property access_token JWT Bearer-токен
 */
@Serializable
data class LoginResponse(
    val access_token: String,
)
