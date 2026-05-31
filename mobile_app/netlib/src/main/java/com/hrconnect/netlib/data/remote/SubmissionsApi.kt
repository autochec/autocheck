package com.hrconnect.netlib.data.remote

import com.hrconnect.netlib.data.remote.dto.CheckResultResponse
import com.hrconnect.netlib.data.remote.dto.ResponseWrapper
import com.hrconnect.netlib.data.remote.dto.SubmissionResponse
import com.hrconnect.netlib.data.remote.dto.SubmitGitRequest
import com.hrconnect.netlib.data.remote.dto.VerdictRequest
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * API для отправки решений и получения результатов проверки.
 * Дата создания: 31-05-2026
 * Автор: 2
 */
interface SubmissionsApi {

    @GET("/api/v1/submissions")
    suspend fun listSubmissions(): ResponseWrapper<List<SubmissionResponse>>

    /**
     * Загрузить решение через Git URL.
     * @param request данные Git-репозитория
     */
    @POST("/api/v1/submissions")
    suspend fun submitGit(@Body request: SubmitGitRequest): ResponseWrapper<SubmissionResponse>

    /**
     * Загрузить ZIP-архив решения (multipart/form-data).
     * @param assignmentId ID задания
     * @param file ZIP-файл
     */
    @POST("/api/v1/submissions/upload")
    suspend fun submitZip(
        @Query("assignment_id") assignmentId: String,
        @Part file: MultipartBody.Part,
    ): ResponseWrapper<SubmissionResponse>

    @GET("/api/v1/submissions/{submission_id}")
    suspend fun getSubmission(@Path("submission_id") id: String): ResponseWrapper<SubmissionResponse>

    @GET("/api/v1/submissions/{submission_id}/status")
    suspend fun getStatus(@Path("submission_id") id: String): ResponseWrapper<String>

    @GET("/api/v1/submissions/{submission_id}/results")
    suspend fun getResults(@Path("submission_id") id: String): ResponseWrapper<List<CheckResultResponse>>

    @POST("/api/v1/submissions/{submission_id}/rerun")
    suspend fun rerun(@Path("submission_id") id: String): ResponseWrapper<SubmissionResponse>

    @PUT("/api/v1/submissions/{submission_id}/verdict")
    suspend fun verdict(
        @Path("submission_id") id: String,
        @Body request: VerdictRequest,
    ): ResponseWrapper<SubmissionResponse>

    @GET("/api/v1/submissions/{submission_id}/ai-review")
    suspend fun getAiReview(@Path("submission_id") id: String): ResponseWrapper<String>

    @GET("/api/v1/submissions/{submission_id}/report")
    suspend fun getReportUrl(@Path("submission_id") id: String): ResponseWrapper<String>
}
