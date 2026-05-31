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

data class SubmissionResultsUiState(
    val submission: Submission? = null,
    val checkResults: List<CheckResult> = emptyList(),
    val aiReview: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
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

    init {
        logcat(TAG) { "Инициализация — submissionId=$submissionId" }
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val submissionDeferred = async { submissionsRepository.getSubmission(submissionId) }
            val resultsDeferred = async { submissionsRepository.getResults(submissionId) }
            val aiDeferred = async { submissionsRepository.getAiReview(submissionId) }

            val submissionResult = submissionDeferred.await()
            val resultsResult = resultsDeferred.await()
            val aiReview = aiDeferred.await().getOrNull()

            submissionResult.fold(
                onSuccess = { submission ->
                    logcat(TAG, DEBUG) {
                        "Проверка загружена — status=${submission.status}, score=${submission.finalScore}"
                    }
                    val checkResults = resultsResult.getOrDefault(emptyList())
                    logcat(TAG, DEBUG) { "Результатов чекеров — count=${checkResults.size}" }
                    if (aiReview == null) {
                        logcat(TAG) { "AI-анализ недоступен — submissionId=$submissionId" }
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
                    logcat(TAG, ERROR) { "Ошибка загрузки: ${e.message}" }
                    _uiState.update {
                        it.copy(isLoading = false, error = e.message ?: "Ошибка загрузки")
                    }
                }
            )
        }
    }
}
