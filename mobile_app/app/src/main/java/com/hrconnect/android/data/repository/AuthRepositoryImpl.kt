package com.hrconnect.android.data.repository

import com.hrconnect.android.domain.model.User
import com.hrconnect.android.domain.repository.AuthRepository
import com.hrconnect.netlib.data.remote.AuthApi
import com.hrconnect.netlib.data.remote.dto.LoginRequest
import com.hrconnect.netlib.data.remote.dto.RegisterRequest
import com.hrconnect.netlib.data.remote.dto.UserResponse
import com.hrconnect.netlib.domain.manager.TokenManager
import logcat.LogPriority.DEBUG
import logcat.LogPriority.ERROR
import logcat.logcat

/**
 * Реализация репозитория аутентификации.
 *
 * Ответственность:
 * - Выполнение API-запросов для входа, регистрации, выхода и профиля.
 * - Сохранение и удаление JWT-токена через TokenManager.
 * - Оборачивание результатов в Result<T> с логированием ошибок.
 *
 * Дата создания: 31-05-2026
 * Автор: Команда №2
 */
class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager,
) : AuthRepository {

    companion object {
        private const val TAG = "AuthRepositoryImpl"
    }

    /**
     * Выполняет вход пользователя по email и паролю.
     * Сохраняет полученный JWT-токен и загружает профиль.
     *
     * @param email email пользователя
     * @param password пароль пользователя
     * @return Result с парой (токен, пользователь) или исключением
     */
    override suspend fun login(email: String, password: String): Result<Pair<String, User>> {
        logcat(TAG) { "[AuthRepositoryImpl]: Начало входа — email=$email" }
        return try {
            val response = authApi.login(LoginRequest(email = email, password = password))
            if (response.error != null) {
                logcat(TAG, ERROR) { "[AuthRepositoryImpl]: Ошибка входа — ${response.error}" }
                Result.failure(Exception(response.error))
            } else {
                val token = response.data?.access_token ?: throw Exception("Токен не получен")
                tokenManager.saveToken(token)
                val profileResult = getProfile()
                profileResult.fold(
                    onSuccess = { user ->
                        logcat(TAG, DEBUG) { "[AuthRepositoryImpl]: Успешный вход — userId=${user.id}" }
                        Result.success(Pair(token, user))
                    },
                    onFailure = { e -> Result.failure(e) }
                )
            }
        } catch (e: Exception) {
            logcat(TAG, ERROR) { "[AuthRepositoryImpl]: Исключение при входе — ${e.message}" }
            Result.failure(e)
        }
    }

    /**
     * Регистрирует нового пользователя.
     *
     * @param email email
     * @param fullName полное имя
     * @param password пароль
     * @param role роль (candidate, expert)
     * @return Result<Unit> или исключение
     */
    override suspend fun register(
        email: String,
        fullName: String,
        password: String,
        role: String,
    ): Result<Unit> {
        logcat(TAG) { "[AuthRepositoryImpl]: Начало регистрации — email=$email, role=$role" }
        return try {
            val response = authApi.register(
                RegisterRequest(email = email, full_name = fullName, password = password, role = role)
            )
            if (response.error != null) {
                logcat(TAG, ERROR) { "[AuthRepositoryImpl]: Ошибка регистрации — ${response.error}" }
                Result.failure(Exception(response.error))
            } else {
                logcat(TAG, DEBUG) { "[AuthRepositoryImpl]: Регистрация успешна — email=$email" }
                Result.success(Unit)
            }
        } catch (e: Exception) {
            logcat(TAG, ERROR) { "[AuthRepositoryImpl]: Исключение при регистрации — ${e.message}" }
            Result.failure(e)
        }
    }

    /**
     * Завершает текущую сессию и удаляет сохранённый токен.
     *
     * @return Result<Unit> или исключение
     */
    override suspend fun logout(): Result<Unit> {
        logcat(TAG) { "[AuthRepositoryImpl]: Начало выхода из системы" }
        return try {
            authApi.logout()
            tokenManager.saveToken("")
            logcat(TAG, DEBUG) { "[AuthRepositoryImpl]: Выход выполнен успешно" }
            Result.success(Unit)
        } catch (e: Exception) {
            logcat(TAG, ERROR) { "[AuthRepositoryImpl]: Исключение при выходе — ${e.message}" }
            Result.failure(e)
        }
    }

    /**
     * Получает профиль текущего аутентифицированного пользователя.
     *
     * @return Result с User или исключением
     */
    override suspend fun getProfile(): Result<User> {
        logcat(TAG) { "[AuthRepositoryImpl]: Запрос профиля пользователя" }
        return try {
            val response = authApi.getProfile()
            if (response.error != null) {
                logcat(TAG, ERROR) { "[AuthRepositoryImpl]: Ошибка получения профиля — ${response.error}" }
                Result.failure(Exception(response.error))
            } else {
                val dto = response.data ?: throw Exception("Данные профиля отсутствуют")
                val user = dto.toUser()
                logcat(TAG, DEBUG) { "[AuthRepositoryImpl]: Профиль получен — userId=${user.id}" }
                Result.success(user)
            }
        } catch (e: Exception) {
            logcat(TAG, ERROR) { "[AuthRepositoryImpl]: Исключение при получении профиля — ${e.message}" }
            Result.failure(e)
        }
    }

    private fun UserResponse.toUser() = User(
        id = id,
        email = email,
        fullName = full_name,
        role = role,
    )
}
