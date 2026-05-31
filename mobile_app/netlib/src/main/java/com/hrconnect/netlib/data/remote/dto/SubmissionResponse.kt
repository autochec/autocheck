package com.hrconnect.netlib.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * DTO проверки (submission).
 *
 * @property id уникальный идентификатор проверки
 * @property assignment_id идентификатор задания
 * @property candidate_name имя кандидата
 * @property candidate_email email кандидата
 * @property submitted_at дата загрузки (ISO 8601, UTC)
 * @property status статус: pending, running, done, error
 * @property final_score итоговый балл (0..100), null если не завершена
 * @property verdict вердикт эксперта: accepted, rejected или null
 */
@Serializable
data class SubmissionResponse(
    val id: String = "",
    val assignment_id: String = "",
    val candidate_name: String = "",
    val candidate_email: String = "",
    val submitted_at: String = "",
    val status: String = "pending",
    val final_score: Float? = null,
    val verdict: String? = null,
)
