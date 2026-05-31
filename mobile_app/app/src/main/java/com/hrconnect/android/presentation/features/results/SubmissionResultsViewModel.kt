package com.hrconnect.android.presentation.features.results

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hrconnect.android.domain.model.CheckResult
import com.hrconnect.android.domain.model.Submission
import com.hrconnect.android.domain.repository.SubmissionsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logcat.LogPriority.DEBUG
import logcat.LogPriority.ERROR
import logcat.logcat

/** Состояние секции AI-анализа на экране результатов. */
sealed interface AiReviewState {
    /** Анализ ещё не запрашивался. */
    data object Idle : AiReviewState

    /** Идёт запрос к бекенду. */
    data object Loading : AiReviewState

    /** Анализ получен. */
    data class Done(val text: String) : AiReviewState

    /** Ошибка при получении анализа. */
    data class Error(val message: String) : AiReviewState
}

data class SubmissionResultsUiState(
    val submission: Submission? = null,
    val checkResults: List<CheckResult> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val aiReviewState: AiReviewState = AiReviewState.Idle,
)

class SubmissionResultsViewModel(
    private val submissionsRepository: SubmissionsRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    companion object {
        private const val TAG = "SubmissionResultsViewModel"
    }

    private val submissionId: String = checkNotNull(savedStateHandle["submissionId"])

    private val _uiState = MutableStateFlow(SubmissionResultsUiState())
    val uiState: StateFlow<SubmissionResultsUiState> = _uiState.asStateFlow()

    private var aiJob: Job? = null

    init {
        logcat(TAG) { "Инициализация — submissionId=$submissionId" }
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val submissionDeferred = async { submissionsRepository.getSubmission(submissionId) }
            val resultsDeferred = async { submissionsRepository.getResults(submissionId) }

            val submissionResult = submissionDeferred.await()
            val resultsResult = resultsDeferred.await()

            submissionResult.fold(
                onSuccess = { submission ->
                    logcat(TAG, DEBUG) {
                        "Проверка загружена — status=${submission.status}, score=${submission.finalScore}"
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            submission = submission,
                            checkResults = resultsResult.getOrDefault(emptyList()),
                        )
                    }
                },
                onFailure = { e ->
                    logcat(TAG, ERROR) { "Ошибка загрузки: ${e.message}" }
                    _uiState.update {
                        it.copy(isLoading = false, error = e.message ?: "Ошибка загрузки")
                    }
                }
            )
        }
    }

    /** Запросить AI-анализ у бекенда (по кнопке пользователя). */
    fun requestAiReview() {
        aiJob?.cancel()
        aiJob = viewModelScope.launch {
            _uiState.update { it.copy(aiReviewState = AiReviewState.Loading) }
            logcat(TAG) { "Запрос AI-анализа — submissionId=$submissionId" }

            submissionsRepository.getAiReview(submissionId).fold(
                onSuccess = { text ->
                    logcat(TAG, DEBUG) { "AI-анализ получен — длина=${text.length}" }
                    _uiState.update { it.copy(aiReviewState = AiReviewState.Done(text)) }
                },
                onFailure = { e ->
                    logcat(TAG, ERROR) { "Ошибка AI-анализа: ${e.message}" }
                    _uiState.update {
                        it.copy(
                            aiReviewState = AiReviewState.Error(
                                e.message ?: "Не удалось получить анализ"
                            )
                        )
                    }
                }
            )
        }
    }
}
