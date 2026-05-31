package com.hrconnect.android.presentation.features.results

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hrconnect.android.data.ai.OpenRouterClient
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

sealed interface AiReviewState {
    data object Idle : AiReviewState
    data object Loading : AiReviewState
    data class Done(val text: String) : AiReviewState
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
    private val openRouterClient: OpenRouterClient,
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

            submissionDeferred.await().fold(
                onSuccess = { submission ->
                    logcat(TAG, DEBUG) {
                        "Проверка загружена — status=${submission.status}, score=${submission.finalScore}"
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            submission = submission,
                            checkResults = resultsDeferred.await().getOrDefault(emptyList()),
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

    /** Запрашивает AI-анализ напрямую через OpenRouter. */
    fun requestAiReview() {
        val state = _uiState.value
        val submission = state.submission ?: return

        aiJob?.cancel()
        aiJob = viewModelScope.launch {
            _uiState.update { it.copy(aiReviewState = AiReviewState.Loading) }
            logcat(TAG) { "Запрос AI-анализа через OpenRouter — submissionId=$submissionId" }

            try {
                val checkResultsText = state.checkResults.joinToString("\n") { r ->
                    "• ${r.checker}: ${r.score.toInt()}/100 — ${r.message.ifBlank { r.status.name }}"
                }
                val review = openRouterClient.reviewSubmission(
                    checkResults = checkResultsText,
                    candidateName = submission.candidateName,
                    finalScore = submission.finalScore,
                )
                logcat(TAG, DEBUG) { "AI-анализ получен — длина=${review.length}" }
                _uiState.update { it.copy(aiReviewState = AiReviewState.Done(review)) }
            } catch (e: Exception) {
                logcat(TAG, ERROR) { "Ошибка AI-анализа: ${e.message}" }
                _uiState.update {
                    it.copy(
                        aiReviewState = AiReviewState.Error(
                            e.message ?: "Не удалось получить анализ"
                        )
                    )
                }
            }
        }
    }
}
