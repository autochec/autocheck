package com.hrconnect.android.presentation.features.results

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hrconnect.android.data.llm.GemmaReportAnalyzer
import com.hrconnect.android.data.llm.ModelDownloadManager
import com.hrconnect.android.domain.llm.ReportPromptBuilder
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

/** Состояние локального AI-анализа. */
sealed interface LocalAiState {
    /** Кнопка ещё не нажата. */
    data object Idle : LocalAiState

    /** Модель не скачана. */
    data object ModelNotDownloaded : LocalAiState

    /** Идёт скачивание модели, [progress] 0..100. */
    data class Downloading(val progress: Int) : LocalAiState

    /** Модель инициализируется. */
    data object Initializing : LocalAiState

    /** Идёт генерация, [text] — накопленный текст. */
    data class Generating(val text: String) : LocalAiState

    /** Генерация завершена. */
    data class Done(val text: String) : LocalAiState

    /** Ошибка. */
    data class Error(val message: String) : LocalAiState
}

/**
 * Состояние экрана «Результаты проверки».
 */
data class SubmissionResultsUiState(
    val submission: Submission? = null,
    val checkResults: List<CheckResult> = emptyList(),
    val aiReview: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val localAiState: LocalAiState = LocalAiState.Idle,
)

/**
 * ViewModel экрана «Результаты проверки».
 */
class SubmissionResultsViewModel(
    private val submissionsRepository: SubmissionsRepository,
    private val gemmaAnalyzer: GemmaReportAnalyzer,
    private val modelDownloadManager: ModelDownloadManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    companion object {
        private const val TAG = "SubmissionResultsViewModel"
    }

    private val submissionId: String = checkNotNull(savedStateHandle["submissionId"])

    private val _uiState = MutableStateFlow(SubmissionResultsUiState())
    val uiState: StateFlow<SubmissionResultsUiState> = _uiState.asStateFlow()

    private var downloadJob: Job? = null
    private var analysisJob: Job? = null

    init {
        logcat(TAG) { "Инициализация — submissionId=$submissionId" }
        // Инициализируем модель если уже скачана
        gemmaAnalyzer.initIfReady()
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
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            submission = submission,
                            checkResults = resultsResult.getOrDefault(emptyList()),
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

    /** Пользователь нажал «Анализировать локально». */
    fun requestLocalAnalysis() {
        when {
            gemmaAnalyzer.isModelReady -> startAnalysis()
            modelDownloadManager.isModelDownloaded() -> initAndAnalyze()
            else -> _uiState.update { it.copy(localAiState = LocalAiState.ModelNotDownloaded) }
        }
    }

    /** Начать скачивание модели. */
    fun startModelDownload() {
        downloadJob?.cancel()
        downloadJob = viewModelScope.launch {
            val downloadId = modelDownloadManager.startDownload()
            modelDownloadManager.downloadProgress(downloadId).collect { progress ->
                if (progress == -1) {
                    _uiState.update {
                        it.copy(localAiState = LocalAiState.Error("Ошибка загрузки модели"))
                    }
                } else {
                    _uiState.update { it.copy(localAiState = LocalAiState.Downloading(progress)) }
                    if (progress == 100) {
                        initAndAnalyze()
                    }
                }
            }
        }
    }

    private fun initAndAnalyze() {
        _uiState.update { it.copy(localAiState = LocalAiState.Initializing) }
        val ok = gemmaAnalyzer.initIfReady()
        if (ok) {
            startAnalysis()
        } else {
            _uiState.update {
                it.copy(localAiState = LocalAiState.Error("Не удалось инициализировать модель"))
            }
        }
    }

    private fun startAnalysis() {
        val state = _uiState.value
        val submission = state.submission ?: return
        analysisJob?.cancel()
        analysisJob = viewModelScope.launch {
            _uiState.update { it.copy(localAiState = LocalAiState.Generating("")) }
            try {
                val prompt = ReportPromptBuilder.build(submission, state.checkResults)
                gemmaAnalyzer.analyze(prompt).collect { token ->
                    val current = (_uiState.value.localAiState as? LocalAiState.Generating)?.text ?: ""
                    _uiState.update { it.copy(localAiState = LocalAiState.Generating(current + token)) }
                }
                val finalText = (_uiState.value.localAiState as? LocalAiState.Generating)?.text ?: ""
                _uiState.update { it.copy(localAiState = LocalAiState.Done(finalText)) }
            } catch (e: Exception) {
                logcat(TAG, ERROR) { "Ошибка генерации: ${e.message}" }
                _uiState.update {
                    it.copy(localAiState = LocalAiState.Error(e.message ?: "Ошибка генерации"))
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        gemmaAnalyzer.release()
    }
}
