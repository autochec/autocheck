package com.hrconnect.android.domain.repository

import com.hrconnect.android.domain.model.Assignment

/**
 * Интерфейс репозитория тестовых заданий (Domain слой).
 *
 * Ответственность:
 * - Абстракция доступа к данным заданий.
 * - Отделяет бизнес-логику от деталей реализации (сеть, БД).
 *
 * Дата создания: 31-05-2026
 * Автор: Команда №2
 */
interface AssignmentsRepository {

    /**
     * Получить список всех тестовых заданий.
     * @return Result со списком заданий или ошибкой
     */
    suspend fun listAssignments(): Result<List<Assignment>>

    /**
     * Получить задание по ID.
     * @param id идентификатор задания
     * @return Result с заданием или ошибкой
     */
    suspend fun getAssignment(id: String): Result<Assignment>
}
