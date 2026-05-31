package com.hrconnect.netlib.data.remote

import com.hrconnect.netlib.data.remote.dto.LoginRequest
import com.hrconnect.netlib.data.remote.dto.LoginResponse
import com.hrconnect.netlib.data.remote.dto.RegisterRequest
import com.hrconnect.netlib.data.remote.dto.ResponseWrapper
import com.hrconnect.netlib.data.remote.dto.UserResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * API для аутентификации и управления профилем.
 * Дата создания: 31-05-2026
 * Автор: 2
 */
interface AuthApi {

    /**
     * Вход по email + пароль.
     * @param loginRequest данные для входа
     * @return ResponseWrapper с data = { access_token }
     */
    @POST("/api/v1/auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): ResponseWrapper<LoginResponse>

    /**
     * Регистрация нового пользователя.
     * @param registerRequest данные для регистрации
     */
    @POST("/api/v1/auth/register")
    suspend fun register(@Body registerRequest: RegisterRequest): ResponseWrapper<Unit>

    /**
     * Завершение текущей сессии (требует Bearer токен).
     */
    @POST("/api/v1/auth/logout")
    suspend fun logout(): ResponseWrapper<Unit>

    /**
     * Профиль текущего пользователя по JWT.
     * @return ResponseWrapper с UserResponse
     */
    @GET("/api/v1/auth/profile")
    suspend fun getProfile(): ResponseWrapper<UserResponse>
}
