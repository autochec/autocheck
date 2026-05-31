package com.hrconnect.netlib.data.remote

import com.hrconnect.netlib.data.remote.dto.ResponseWrapper
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * API для работы с кандидатами.
 * Дата создания: 31-05-2026
 * Автор: 2
 */
interface CandidatesApi {

    @GET("/api/v1/candidates")
    suspend fun listCandidates(): ResponseWrapper<List<Any>>

    @GET("/api/v1/candidates/{candidate_id}")
    suspend fun getCandidate(@Path("candidate_id") id: String): ResponseWrapper<Any>
}