package com.hrconnect.netlib.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * DTO результата выполнения чекера.
 *
 * @property checker название чекера
 * @property status статус: passed, failed, error
 * @property score балл (0..100)
 * @property message краткое сообщение
 * @property details детальный лог (текст/JSON)
 */
@Serializable
data class CheckResultResponse(
    val checker: String = "",
    val status: String = "error",
    val score: Float = 0f,
    val message: String = "",
    val details: String = "",
)
