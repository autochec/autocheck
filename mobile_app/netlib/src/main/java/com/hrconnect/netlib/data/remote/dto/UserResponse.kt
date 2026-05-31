package com.hrconnect.netlib.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * DTO профиля пользователя.
 *
 * @property id уникальный идентификатор
 * @property email email пользователя
 * @property full_name полное имя
 * @property role роль: candidate, expert, hr
 */
@Serializable
data class UserResponse(
    val id: String = "",
    val email: String = "",
    val full_name: String = "",
    val role: String = "candidate",
)
