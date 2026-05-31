package com.hrconnect.android.data.repository

import com.hrconnect.android.domain.model.CheckResult
import com.hrconnect.android.domain.model.CheckStatus
import com.hrconnect.android.domain.model.Submission
import com.hrconnect.android.domain.model.SubmissionStatus
import com.hrconnect.android.domain.model.Verdict
import com.hrconnect.android.domain.repository.SubmissionsRepository
import com.hrconnect.netlib.data.remote.SubmissionsApi
import com.hrconnect.netlib.data.remote.dto.CheckResultResponse
import com.hrconnect.netlib.data.remote.dto.SubmissionResponse
import com.hrconnect.netlib.data.remote.dto.SubmitGitRequest
import com.hrconnect.netlib.data.remote.dto.VerdictRequest
import logcat.LogPriority.DEBUG
import logcat.LogPriority.ERROR
import logcat.logcat
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Реализация репозитория проверок.
 *
 * Ответственность:
 * - Загрузка решений (ZIP-архив и Git URL) через SubmissionsApi.
 * - Получение статусов, результатов и AI-анализа проверок.
 * - Преобразование DTO в доменные модели.
 * - Логирование критических действий и ошибок.
 *
 * Дата создания: 31-05-2026
 * Автор: Команда №2
 */
class SubmissionsRepositoryImpl(
    private val submissionsApi: SubmissionsApi,
) : SubmissionsRepository {

    companion object {
        private const val TAG = "SubmissionsRepositoryImpl"
        // ISO 8601 — формат дат сервера
        private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
    }

    override suspend fun listSubmissions(): Result<List<Submission>> {
        logcat(TAG) { "[SubmissionsRepositoryImpl]: Запрос списка проверок" }
        return try {
            val response = submissionsApi.listSubmissions()
            if (response.error != null) {
                logcat(TAG, ERROR) {
                    "[SubmissionsRepositoryImpl]: Ошибка получения проверок — ${response.error}"
                }
                Result.failure(Exception(response.error))
            } else {
                val list = (response.data ?: emptyList()).map { it.toSubmission() }
                logcat(TAG, DEBUG) {
                    "[SubmissionsRepositoryImpl]: Получено проверок — count=${list.size}"
                }
                Result.success(list)
            }
        } catch (e: Exception) {
            logcat(TAG, ERROR) {
                "[SubmissionsRepositoryImpl]: Исключение при получении проверок — ${e.message}"
            }
            Result.failure(e)
        }
    }

    /**
     * Загружает решение через Git URL.
     * Критическое пользовательское действие — логируется INFO.
     */
    override suspend fun submitGit(
        assignmentId: String,
        candidateName: String,
        candidateEmail: String,
        gitUrl: String,
    ): Result<Submission> {
        logcat(TAG) {
            "[SubmissionsRepositoryImpl]: Загрузка решения через Git — assignmentId=$assignmentId"
        }
        return try {
            val response = submissionsApi.submitGit(
                SubmitGitRequest(
                    assignment_id = assignmentId,
                    candidate_name = candidateName,
                    candidate_email = candidateEmail,
                    git_url = gitUrl,
                )
            )
            if (response.error != null) {
                logcat(TAG, ERROR) {
                    "[SubmissionsRepositoryImpl]: Ошибка загрузки Git — ${response.error}"
                }
                Result.failure(Exception(response.error))
            } else {
                val submission = (response.data ?: throw Exception("Данные проверки отсутствуют"))
                    .toSubmission()
                logcat(TAG, DEBUG) {
                    "[SubmissionsRepositoryImpl]: Git загружен — submissionId=${submission.id}"
                }
                Result.success(submission)
            }
        } catch (e: Exception) {
            logcat(TAG, ERROR) {
                "[SubmissionsRepositoryImpl]: Исключение при загрузке Git — ${e.message}"
            }
            Result.failure(e)
        }
    }

    /**
     * Загружает ZIP-архив решения.
     * Критическое пользовательское действие — логируется INFO.
     */
    override suspend fun submitZip(assignmentId: String, zipBytes: ByteArray): Result<Submission> {
        logcat(TAG) {
            "[SubmissionsRepositoryImpl]: Загрузка ZIP — assignmentId=$assignmentId, size=${zipBytes.size}"
        }
        return try {
            val requestBody = zipBytes.toRequestBody("application/zip".toMediaType())
            val filePart = MultipartBody.Part.createFormData("file", "submission.zip", requestBody)
            val response = submissionsApi.submitZip(assignmentId = assignmentId, file = filePart)
            if (response.error != null) {
                logcat(TAG, ERROR) {
                    "[SubmissionsRepositoryImpl]: Ошибка загрузки ZIP — ${response.error}"
                }
                Result.failure(Exception(response.error))
            } else {
                val submission = (response.data ?: throw Exception("Данные проверки отсутствуют"))
                    .toSubmission()
                logcat(TAG, DEBUG) {
                    "[SubmissionsRepositoryImpl]: ZIP загружен — submissionId=${submission.id}"
                }
                Result.success(submission)
            }
        } catch (e: Exception) {
            logcat(TAG, ERROR) {
                "[SubmissionsRepositoryImpl]: Исключение при загрузке ZIP — ${e.message}"
            }
            Result.failure(e)
        }
    }

    override suspend fun getSubmission(id: String): Result<Submission> {
        logcat(TAG) { "[SubmissionsRepositoryImpl]: Запрос проверки — submissionId=$id" }
        return try {
            val response = submissionsApi.getSubmission(id)
            if (response.error != null) {
                logcat(TAG, ERROR) {
                    "[SubmissionsRepositoryImpl]: Ошибка получения проверки — ${response.error}"
                }
                Result.failure(Exception(response.error))
            } else {
                val submission = (response.data ?: throw Exception("Проверка не найдена: id=$id"))
                    .toSubmission()
                logcat(TAG, DEBUG) {
                    "[SubmissionsRepositoryImpl]: Проверка получена — status=${submission.status}"
                }
                Result.success(submission)
            }
        } catch (e: Exception) {
            logcat(TAG, ERROR) {
                "[SubmissionsRepositoryImpl]: Исключение при получении проверки — id=$id, ${e.message}"
            }
            Result.failure(e)
        }
    }

    override suspend fun getStatus(id: String): Result<SubmissionStatus> {
        logcat(TAG) { "[SubmissionsRepositoryImpl]: Запрос статуса — submissionId=$id" }
        return try {
            val response = submissionsApi.getStatus(id)
            val status = (response.data ?: "pending").toSubmissionStatus()
            logcat(TAG, DEBUG) {
                "[SubmissionsRepositoryImpl]: Статус получен — submissionId=$id, status=$status"
            }
            Result.success(status)
        } catch (e: Exception) {
            logcat(TAG, ERROR) {
                "[SubmissionsRepositoryImpl]: Исключение при получении статуса — id=$id, ${e.message}"
            }
            Result.failure(e)
        }
    }

    override suspend fun getResults(id: String): Result<List<CheckResult>> {
        logcat(TAG) { "[SubmissionsRepositoryImpl]: Запрос результатов — submissionId=$id" }
        return try {
            val response = submissionsApi.getResults(id)
            if (response.error != null) {
                logcat(TAG, ERROR) {
                    "[SubmissionsRepositoryImpl]: Ошибка получения результатов — ${response.error}"
                }
                Result.failure(Exception(response.error))
            } else {
                val results = (response.data ?: emptyList()).map { it.toCheckResult() }
                logcat(TAG, DEBUG) {
                    "[SubmissionsRepositoryImpl]: Результаты получены — checkers=${results.size}"
                }
                Result.success(results)
            }
        } catch (e: Exception) {
            logcat(TAG, ERROR) {
                "[SubmissionsRepositoryImpl]: Исключение при получении результатов — id=$id, ${e.message}"
            }
            Result.failure(e)
        }
    }

    override suspend fun rerun(id: String): Result<Submission> {
        logcat(TAG) { "[SubmissionsRepositoryImpl]: Повторная проверка — submissionId=$id" }
        return try {
            val response = submissionsApi.rerun(id)
            if (response.error != null) {
                Result.failure(Exception(response.error))
            } else {
                Result.success(
                    (response.data ?: throw Exception("Данные отсутствуют")).toSubmission()
                )
            }
        } catch (e: Exception) {
            logcat(TAG, ERROR) {
                "[SubmissionsRepositoryImpl]: Исключение при повторной проверке — id=$id, ${e.message}"
            }
            Result.failure(e)
        }
    }

    /**
     * Выносит вердикт по проверке.
     * Критическое пользовательское действие — логируется INFO.
     */
    override suspend fun verdict(id: String, verdict: String, comment: String): Result<Unit> {
        logcat(TAG) {
            "[SubmissionsRepositoryImpl]: Вынесение вердикта — submissionId=$id, verdict=$verdict"
        }
        return try {
            submissionsApi.verdict(id, VerdictRequest(verdict = verdict, comment = comment))
            logcat(TAG, DEBUG) {
                "[SubmissionsRepositoryImpl]: Вердикт сохранён — submissionId=$id"
            }
            Result.success(Unit)
        } catch (e: Exception) {
            logcat(TAG, ERROR) {
                "[SubmissionsRepositoryImpl]: Исключение при вынесении вердикта — id=$id, ${e.message}"
            }
            Result.failure(e)
        }
    }

    override suspend fun getAiReview(id: String): Result<String> {
        logcat(TAG) { "[SubmissionsRepositoryImpl]: Запрос AI-анализа — submissionId=$id" }
        return try {
            val response = submissionsApi.getAiReview(id)
            if (response.error != null) {
                Result.failure(Exception(response.error))
            } else {
                Result.success(response.data ?: "")
            }
        } catch (e: Exception) {
            logcat(TAG, ERROR) {
                "[SubmissionsRepositoryImpl]: Исключение при получении AI-анализа — id=$id, ${e.message}"
            }
            Result.failure(e)
        }
    }

    override suspend fun getReportUrl(id: String): Result<String> {
        logcat(TAG) { "[SubmissionsRepositoryImpl]: Запрос отчёта — submissionId=$id" }
        return try {
            val response = submissionsApi.getReportUrl(id)
            if (response.error != null) {
                Result.failure(Exception(response.error))
            } else {
                Result.success(response.data ?: "")
            }
        } catch (e: Exception) {
            logcat(TAG, ERROR) {
                "[SubmissionsRepositoryImpl]: Исключение при получении URL отчёта — id=$id, ${e.message}"
            }
            Result.failure(e)
        }
    }

    // --- Вспомогательные преобразования DTO → Domain ---

    private fun SubmissionResponse.toSubmission() = Submission(
        id = id,
        assignmentId = assignment_id,
        candidateName = candidate_name,
        candidateEmail = candidate_email,
        submittedAt = runCatching { DATE_FORMAT.parse(submitted_at) }.getOrDefault(Date()),
        status = status.toSubmissionStatus(),
        finalScore = final_score,
        verdict = verdict?.toVerdict(),
    )

    private fun CheckResultResponse.toCheckResult() = CheckResult(
        checker = checker,
        status = status.toCheckStatus(),
        score = score,
        message = message,
        details = details,
    )

    private fun String.toSubmissionStatus(): SubmissionStatus = when (lowercase()) {
        "running" -> SubmissionStatus.RUNNING
        "done" -> SubmissionStatus.DONE
        "error" -> SubmissionStatus.ERROR
        else -> SubmissionStatus.PENDING
    }

    private fun String.toCheckStatus(): CheckStatus = when (lowercase()) {
        "passed" -> CheckStatus.PASSED
        "failed" -> CheckStatus.FAILED
        else -> CheckStatus.ERROR
    }

    private fun String.toVerdict(): Verdict? = when (lowercase()) {
        "accepted" -> Verdict.ACCEPTED
        "rejected" -> Verdict.REJECTED
        else -> null
    }
}
