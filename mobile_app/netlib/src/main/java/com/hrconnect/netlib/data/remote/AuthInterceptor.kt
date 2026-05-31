package com.hrconnect.netlib.data.remote

import com.hrconnect.netlib.domain.manager.TokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Интерсептор для автоматического добавления заголовка Authorization с токеном в запросы.
 *
 * Дата создания: 31-05-2026.
 * Автор создания: 1.
 */
class AuthInterceptor(
    private val tokenManager: TokenManager,
) : Interceptor {

    /**
     * Функция для перехвата исходящего запроса и добавления в него заголовка Authorization с токеном.
     *
     * @param chain цепочка обработки запроса.
     *
     * @return ответ сервера после выполнения измененного запроса.
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking {
            tokenManager.getToken().first()
        }
        val request = chain.request()
            .newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()
        return chain.proceed(request)
    }
}