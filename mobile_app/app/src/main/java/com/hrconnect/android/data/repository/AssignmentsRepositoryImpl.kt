package com.hrconnect.android.data.repository

import com.hrconnect.android.domain.model.Assignment
import com.hrconnect.android.domain.repository.AssignmentsRepository
import com.hrconnect.netlib.data.remote.AssignmentsApi
import com.hrconnect.netlib.data.remote.dto.AssignmentResponse
import logcat.LogPriority.DEBUG
import logcat.LogPriority.ERROR
import logcat.logcat

/**
 * Реализация репозитория тестовых заданий.
 *
 * Ответственность:
 * - Получение списка и деталей тестовых заданий через AssignmentsApi.
 * - Преобразование DTO в доменные модели.
 * - Оборачивание результатов в Result<T>.
 *
 * Дата создания: 31-05-2026
 * Автор: Команда №2
 */
class AssignmentsRepositoryImpl(
    private val assignmentsApi: AssignmentsApi,
) : AssignmentsRepository {

    companion object {
        private const val TAG = "AssignmentsRepositoryImpl"
    }

    /**
     * Получает список всех доступных тестовых заданий.
     *
     * @return Result со списком Assignment или исключением
     */
    override suspend fun listAssignments(): Result<List<Assignment>> {
        logcat(TAG) { "[AssignmentsRepositoryImpl]: Запрос списка заданий" }
        return try {
            val response = assignmentsApi.listAssignments()
            if (response.error != null) {
                logcat(TAG, ERROR) {
                    "[AssignmentsRepositoryImpl]: Ошибка получения заданий — ${response.error}"
                }
                Result.failure(Exception(response.error))
            } else {
                val list = (response.data ?: emptyList()).map { it.toAssignment() }
                logcat(TAG, DEBUG) {
                    "[AssignmentsRepositoryImpl]: Получено заданий — count=${list.size}"
                }
                Result.success(list)
            }
        } catch (e: Exception) {
            logcat(TAG, ERROR) {
                "[AssignmentsRepositoryImpl]: Исключение при получении заданий — ${e.message}"
            }
            Result.failure(e)
        }
    }

    /**
     * Получает задание по идентификатору.
     *
     * @param id идентификатор задания
     * @return Result с Assignment или исключением
     */
    override suspend fun getAssignment(id: String): Result<Assignment> {
        logcat(TAG) { "[AssignmentsRepositoryImpl]: Запрос задания — assignmentId=$id" }
        return try {
            val response = assignmentsApi.getAssignment(id)
            if (response.error != null) {
                logcat(TAG, ERROR) {
                    "[AssignmentsRepositoryImpl]: Ошибка получения задания — id=$id, ${response.error}"
                }
                Result.failure(Exception(response.error))
            } else {
                val assignment = (response.data ?: throw Exception("Задание не найдено: id=$id"))
                    .toAssignment()
                logcat(TAG, DEBUG) {
                    "[AssignmentsRepositoryImpl]: Задание получено — assignmentId=$id"
                }
                Result.success(assignment)
            }
        } catch (e: Exception) {
            logcat(TAG, ERROR) {
                "[AssignmentsRepositoryImpl]: Исключение при получении задания — id=$id, ${e.message}"
            }
            Result.failure(e)
        }
    }

    private fun AssignmentResponse.toAssignment() = Assignment(
        id = id,
        title = title,
        description = description,
        technologies = technologies,
        activeCheckers = active_checkers,
        weights = weights,
        instructions = instructions,
        isPublished = is_published,
    )
}
