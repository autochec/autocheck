package com.hrconnect.netlib.data.remote

import com.hrconnect.netlib.data.remote.dto.AssignmentResponse
import com.hrconnect.netlib.data.remote.dto.ResponseWrapper
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * API для управления тестовыми заданиями (требует Bearer токен).
 * Дата создания: 31-05-2026
 * Автор: 2
 */
interface AssignmentsApi {

    @GET("/api/v1/assignments")
    suspend fun listAssignments(): ResponseWrapper<List<AssignmentResponse>>

    @POST("/api/v1/assignments")
    suspend fun createAssignment(): ResponseWrapper<AssignmentResponse>

    @GET("/api/v1/assignments/{assignment_id}")
    suspend fun getAssignment(@Path("assignment_id") id: String): ResponseWrapper<AssignmentResponse>

    @PUT("/api/v1/assignments/{assignment_id}")
    suspend fun updateAssignment(@Path("assignment_id") id: String): ResponseWrapper<AssignmentResponse>

    @DELETE("/api/v1/assignments/{assignment_id}")
    suspend fun deleteAssignment(@Path("assignment_id") id: String): ResponseWrapper<Unit>
}
