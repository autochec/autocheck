package com.hrconnect.netlib.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * DTO ответа AI-анализа.
 * Сервер возвращает: {"data":{"review":"..."},"error":null,"meta":null}
 */
@Serializable
data class AiReviewResponse(
    val review: String = "",
)
