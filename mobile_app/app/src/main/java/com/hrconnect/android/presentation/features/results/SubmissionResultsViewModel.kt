package com.hrconnect.android.presentation.features.results

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hrconnect.android.domain.model.CheckResult
import com.hrconnect.android.domain.model.Submission
import com.hrconnect.android.domain.repository.SubmissionsRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logcat.LogPriority.DEBUG
import logcat.LogPriority.ERROR
import logcat.logcat

/**
 * Состояние экрана «Результаты проверки».
 *
 * @property submission данные проверки (null во время загрузки)
 * @property checkResults список результатов чекеров
 * @property aiReview текст AI-анализа (null если недоступен)
 * @property isLoading идёт ли загрузка
 * @property error сообщение ошибки (null = нет ошибки)
 */
data class SubmissionResultsUiState(
    val submission: Submission? = null,
    val checkResults: List<CheckResult> = emptyList(),
    val aiReview: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

/**
 * ViewModel экрана «Результаты проверки».
 *
 * Ответственность:
 * - Загрузка данных проверки, результатов чекеров и AI-анализа.
 * - Параллельная загрузка данных для ускорения отображения.
 * - Обработка graceful degradation: AI-анализ недоступен — показываем плашку.
 *
 * Дата создания: 31-05-2026
 * Автор: Команда №2
 */
class SubmissionResultsViewModel(
    private val submissionsRepository: SubmissionsRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    companion object {
        private const val TAG = "SubmissionResultsViewModel"
    }

    // Извлекаем submissionId из аргументов навигации
    private val submissionId: String = checkNotNull(savedStateHandle["submissionId"])

    private val _uiState = MutableStateFlow(SubmissionResultsUiState())
    val uiState: StateFlow<SubmissionResultsUiState> = _uiState.asStateFlow()

    init {
        logcat(TAG) { "[SubmissionResultsViewModel]: Инициализация — submissionId=$submissionId" }
        loadData()
    }

    /**
     * Загружает все данные экрана параллельно:
     * - детали проверки (submission)
     * - результаты чекеров (checkResults)
     * - AI-анализ кода (aiReview, может быть недоступен)
     *
     * Если AI-анализ недоступен — продолжаем работу без него (graceful degradation).
     */
    fun loadData() {
        logcat(TAG) {
            "[SubmissionResultsViewModel]: Загрузка данных проверки — submissionId=$submissionId"
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Параллельная загрузка основных данных
            val submissionDeferred = async { submissionsRepository.getSubmission(submissionId) }
            val resultsDeferred = async { submissionsRepository.getResults(submissionId) }
            val aiDeferred = async { submissionsRepository.getAiReview(submissionId) }

            val submissionResult = submissionDeferred.await()
            val resultsResult = resultsDeferred.await()
            // AI-анализ — graceful degradation: ошибка не блокирует отображение
            val aiReview = aiDeferred.await().getOrNull()

            submissionResult.fold(
                onSuccess = { submission ->
                    logcat(TAG, DEBUG) {
                        "[SubmissionResultsViewModel]: Проверка загружена — status=${submission.status}, score=${submission.finalScore}"
                    }
                    val checkResults = resultsResult.getOrDefault(emptyList())
                    logcat(TAG, DEBUG) {
                        "[SubmissionResultsViewModel]: Результатов чекеров — count=${checkResults.size}"
                    }
                    if (aiReview == null) {
                        logcat(TAG) {
                            "[SubmissionResultsViewModel]: AI-анализ недоступен — submissionId=$submissionId"
                        }
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            submission = submission,
                            checkResults = checkResults,
                            aiReview = aiReview
                        )
                    }
                },
                onFailure = { e ->
                    logcat(TAG, ERROR) {
                        "[SubmissionResultsViewModel]: Ошибка загрузки проверки — ${e.message}"
                    }
                    _uiState.update {
                        it.copy(isLoading = false, error = e.message ?: "Ошибка загрузки")
                    }
                }
            )
        }
    }
}
