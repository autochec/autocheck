package com.hrconnect.android.domain.repository

import com.hrconnect.android.domain.model.CheckResult
import com.hrconnect.android.domain.model.Submission
import com.hrconnect.android.domain.model.SubmissionStatus

/**
 * Интерфейс репозитория проверок (Domain слой).
 * Дата создания: 26-05-2026
 * Автор: Team01
 */
interface SubmissionsRepository {
    suspend fun listSubmissions(): Result<List<Submission>>
    suspend fun submitGit(
        assignmentId: String,
        candidateName: String,
        candidateEmail: String,
        gitUrl: String,
    ): Result<Submission>

    suspend fun submitZip(assignmentId: String, zipBytes: ByteArray): Result<Submission>
    suspend fun getSubmission(id: String): Result<Submission>
    suspend fun getStatus(id: String): Result<SubmissionStatus>
    suspend fun getResults(id: String): Result<List<CheckResult>>
    suspend fun rerun(id: String): Result<Submission>
    suspend fun verdict(id: String, verdict: String, comment: String): Result<Unit>
    suspend fun getAiReview(id: String): Result<String>
    suspend fun getReportUrl(id: String): Result<String>
}