package com.hrconnect.netlib.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Стандартная обёртка ответа API.
 * @param data полезная нагрузка (при успехе)
 * @param error текст ошибки (при сбое)
 * @param meta метаданные (пагинация, версия и т.д.)
 */
@Serializable
data class ResponseWrapper<T>(
    val data: T? = null,
    val error: String? = null,
    val meta: Map<String, String>? = null,
)