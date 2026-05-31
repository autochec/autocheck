package com.hrconnect.android.domain.model

/**
 * Доменная модель тестового задания.
 *
 * Ответственность:
 * - Хранение данных тестового задания, созданного экспертом.
 * - Содержит конфигурацию активных чекеров и их весов.
 *
 * Дата создания: 31-05-2026
 * Автор: Команда №2
 *
 * @property id уникальный идентификатор задания
 * @property title название задания
 * @property description описание задания
 * @property technologies список технологий (Android, iOS, Flutter и т.д.)
 * @property activeCheckers список активных чекеров
 * @property weights веса чекеров (ключ — название чекера, значение — вес 0..100)
 * @property instructions инструкции для кандидата (markdown)
 * @property isPublished опубликовано ли задание
 */
data class Assignment(
    val id: String,
    val title: String,
    val description: String,
    val technologies: List<String>,
    val activeCheckers: List<String>,
    val weights: Map<String, Int>,
    val instructions: String,
    val isPublished: Boolean,
)
