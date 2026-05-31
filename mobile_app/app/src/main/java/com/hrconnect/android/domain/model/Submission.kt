package com.hrconnect.android.domain.model

import java.util.Date

/**
 * Доменная модель проверки (submission).
 *
 * Ответственность:
 * - Представление данных загруженного решения кандидата.
 * - Содержит статус, итоговый балл и вердикт эксперта.
 *
 * Дата создания: 31-05-2026
 * Автор: Команда №2
 *
 * @property id уникальный идентификатор проверки
 * @property assignmentId идентификатор тестового задания
 * @property candidateName имя кандидата
 * @property candidateEmail email кандидата
 * @property submittedAt дата загрузки решения (UTC)
 * @property status текущий статус проверки
 * @property finalScore итоговый балл (0..100), null если проверка ещё не завершена
 * @property verdict вердикт эксперта (null если не вынесен)
 */
data class Submission(
    val id: String,
    val assignmentId: String,
    val candidateName: String,
    val candidateEmail: String,
    val submittedAt: Date,
    val status: SubmissionStatus,
    val finalScore: Float?,
    val verdict: Verdict?,
)

/**
 * Статус проверки.
 */
enum class SubmissionStatus {
    PENDING,  // ожидает обработки
    RUNNING,  // выполняется
    DONE,     // завершена
    ERROR     // ошибка
}

/**
 * Вердикт эксперта по проверке.
 */
enum class Verdict {
    ACCEPTED,  // принят
    REJECTED   // отклонён
}
