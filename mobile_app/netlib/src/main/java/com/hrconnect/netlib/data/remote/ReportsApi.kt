package com.hrconnect.netlib.data.remote

import com.hrconnect.netlib.data.remote.dto.ResponseWrapper
import retrofit2.http.GET

/**
 * API для получения статистики.
 * Дата создания: 31-05-2026
 * Автор: 2
 */
interface ReportsApi {

    @GET("/api/v1/reports/stats")
    suspend fun getStats(): ResponseWrapper<Any>
}