package com.hrconnect.android.presentation.navigation

/**
 * Маршруты навигации приложения.
 *
 * Ответственность:
 * - Хранение констант маршрутов для Compose Navigation.
 * - Обеспечивает единый источник истины для имён маршрутов.
 *
 * Дата создания: 31-05-2026
 * Автор: Команда №2
 */
object Routes {
    const val LOGIN = "login"
    const val MY_SUBMISSIONS = "submissions"
    const val SUBMIT_ASSIGNMENT = "submit"
    const val SUBMISSION_RESULTS = "results/{submissionId}"

    /** Формирует маршрут к экрану результатов с конкретным ID. */
    fun submissionResults(submissionId: String) = "results/$submissionId"
}
