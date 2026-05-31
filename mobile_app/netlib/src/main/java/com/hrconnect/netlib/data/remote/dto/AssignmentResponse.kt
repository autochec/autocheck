package com.hrconnect.netlib.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * DTO тестового задания.
 *
 * @property id уникальный идентификатор
 * @property title название задания
 * @property description описание
 * @property technologies список технологий
 * @property active_checkers активные чекеры
 * @property weights веса чекеров (ключ — название, значение — вес 0..100)
 * @property instructions инструкции для кандидата (markdown)
 * @property is_published опубликовано ли задание
 */
@Serializable
data class AssignmentResponse(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val technologies: List<String> = emptyList(),
    val active_checkers: List<String> = emptyList(),
    val weights: Map<String, Int> = emptyMap(),
    val instructions: String = "",
    val is_published: Boolean = false,
)
