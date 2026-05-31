package com.hrconnect.android.domain.model

/**
 * Доменная модель пользователя.
 * Дата создания: 26-05-2026
 * Автор: Team01
 */
data class User(
    val id: String,
    val email: String,
    val fullName: String,
    val role: String,   // "candidate", "expert", "hr"
)