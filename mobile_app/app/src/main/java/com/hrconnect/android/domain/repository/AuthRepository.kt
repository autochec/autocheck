package com.hrconnect.android.domain.repository

import com.hrconnect.android.domain.model.User

/**
 * Интерфейс репозитория аутентификации (Domain слой).
 * Дата создания: 26-05-2026
 * Автор: Team01
 */
interface AuthRepository {
    /**
     * Вход пользователя.
     * @param email email
     * @param password пароль
     * @return Pair(token, user) или null при ошибке
     */
    suspend fun login(email: String, password: String): Result<Pair<String, User>>

    /**
     * Регистрация нового пользователя.
     */
    suspend fun register(email: String, fullName: String, password: String, role: String): Result<Unit>

    /**
     * Выход из системы.
     */
    suspend fun logout(): Result<Unit>

    /**
     * Получение профиля текущего пользователя.
     */
    suspend fun getProfile(): Result<User>
}