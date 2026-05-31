package com.hrconnect.android.domain.model

/**
 * Результат выполнения отдельного чекера.
 *
 * Ответственность:
 * - Хранение результата одного вида автоматической проверки.
 * - Содержит статус, балл, сообщение и детальный лог.
 *
 * Дата создания: 31-05-2026
 * Автор: Команда №2
 *
 * @property checker название чекера (StaticAnalysis, Architecture, Build и т.д.)
 * @property status статус выполнения чекера
 * @property score балл (0..100)
 * @property message краткое сообщение о результате
 * @property details детальный лог (текст/JSON)
 */
data class CheckResult(
    val checker: String,
    val status: CheckStatus,
    val score: Float,
    val message: String,
    val details: String,
)

/**
 * Статус выполнения чекера.
 */
enum class CheckStatus {
    PASSED,  // проверка пройдена
    FAILED,  // проверка не пройдена
    ERROR    // ошибка при выполнении
}
