package com.hrconnect.android.presentation.features.submit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hrconnect.android.domain.model.Assignment
import com.hrconnect.android.domain.repository.AssignmentsRepository
import com.hrconnect.android.domain.repository.SubmissionsRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logcat.LogPriority.DEBUG
import logcat.LogPriority.ERROR
import logcat.logcat

/**
 * Состояние экрана «Отправить задание».
 *
 * @property assignments список доступных тестовых заданий
 * @property selectedAssignment выбранное задание (null = не выбрано)
 * @property selectedFileName имя выбранного ZIP-файла (null = файл не выбран)
 * @property selectedFileBytes байты ZIP-архива (null = файл не выбран)
 * @property isLoadingAssignments загружается ли список заданий
 * @property isUploading идёт ли загрузка решения на сервер
 * @property error сообщение ошибки (null = нет ошибки)
 */
data class SubmitAssignmentUiState(
    val assignments: List<Assignment> = emptyList(),
    val selectedAssignment: Assignment? = null,
    val selectedFileName: String? = null,
    val selectedFileBytes: ByteArray? = null,
    val isLoadingAssignments: Boolean = false,
    val isUploading: Boolean = false,
    val error: String? = null,
) {
    /** Можно ли нажать кнопку «Отправить». */
    val canSubmit: Boolean
        get() = selectedAssignment != null && selectedFileBytes != null && !isUploading
}

/** Одноразовые события экрана «Отправить задание». */
sealed class SubmitAssignmentUiEvent {
    object NavigateToMySubmissions : SubmitAssignmentUiEvent()
    data class ShowError(val message: String) : SubmitAssignmentUiEvent()
}

/**
 * ViewModel экрана «Отправить задание».
 *
 * Ответственность:
 * - Загрузка списка тестовых заданий.
 * - Управление выбором задания и ZIP-файла.
 * - Отправка ZIP-архива на сервер через SubmissionsRepository.
 * - Навигация на «Мои задания» после успешной отправки.
 *
 * Дата создания: 31-05-2026
 * Автор: Команда №2
 */
class SubmitAssignmentViewModel(
    private val assignmentsRepository: AssignmentsRepository,
    private val submissionsRepository: SubmissionsRepository,
) : ViewModel() {

    companion object {
        private const val TAG = "SubmitAssignmentViewModel"
    }

    private val _uiState = MutableStateFlow(SubmitAssignmentUiState())
    val uiState: StateFlow<SubmitAssignmentUiState> = _uiState.asStateFlow()

    private val _events = Channel<SubmitAssignmentUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        logcat(TAG) { "[SubmitAssignmentViewModel]: Инициализация" }
        loadAssignments()
    }

    /**
     * Загружает список доступных тестовых заданий.
     */
    private fun loadAssignments() {
        logcat(TAG) { "[SubmitAssignmentViewModel]: Загрузка списка заданий" }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingAssignments = true, error = null) }
            assignmentsRepository.listAssignments().fold(
                onSuccess = { assignments ->
                    logcat(TAG, DEBUG) {
                        "[SubmitAssignmentViewModel]: Задания загружены — count=${assignments.size}"
                    }
                    _uiState.update {
                        it.copy(isLoadingAssignments = false, assignments = assignments)
                    }
                },
                onFailure = { e ->
                    logcat(TAG, ERROR) {
                        "[SubmitAssignmentViewModel]: Ошибка загрузки заданий — ${e.message}"
                    }
                    _uiState.update {
                        it.copy(isLoadingAssignments = false, error = e.message)
                    }
                }
            )
        }
    }

    /**
     * Обновляет выбранное задание.
     *
     * @param assignment выбранное задание из списка
     */
    fun onAssignmentSelected(assignment: Assignment) {
        logcat(TAG) {
            "[SubmitAssignmentViewModel]: Выбрано задание — assignmentId=${assignment.id}, title=${assignment.title}"
        }
        _uiState.update { it.copy(selectedAssignment = assignment, error = null) }
    }

    /**
     * Сохраняет выбранный ZIP-файл в состоянии.
     * Критическое пользовательское действие — логируется INFO.
     *
     * @param fileName имя файла
     * @param bytes байты ZIP-архива
     */
    fun onFileSelected(fileName: String, bytes: ByteArray) {
        logcat(TAG) {
            "[SubmitAssignmentViewModel]: Выбран ZIP-файл — fileName=$fileName, sizeBytes=${bytes.size}"
        }
        _uiState.update {
            it.copy(
                selectedFileName = fileName,
                selectedFileBytes = bytes,
                error = null
            )
        }
    }

    /**
     * Отправляет выбранный ZIP-архив на сервер для проверки.
     * Критическое пользовательское действие — логируется INFO.
     */
    fun submit() {
        val state = _uiState.value
        val assignment = state.selectedAssignment ?: return
        val bytes = state.selectedFileBytes ?: return

        logcat(TAG) {
            "[SubmitAssignmentViewModel]: Начало отправки задания — assignmentId=${assignment.id}, fileName=${state.selectedFileName}"
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, error = null) }
            submissionsRepository.submitZip(
                assignmentId = assignment.id,
                zipBytes = bytes
            ).fold(
                onSuccess = { submission ->
                    logcat(TAG, DEBUG) {
                        "[SubmitAssignmentViewModel]: Задание отправлено — submissionId=${submission.id}"
                    }
                    _uiState.update { it.copy(isUploading = false) }
                    _events.send(SubmitAssignmentUiEvent.NavigateToMySubmissions)
                },
                onFailure = { e ->
                    logcat(TAG, ERROR) {
                        "[SubmitAssignmentViewModel]: Ошибка отправки задания — ${e.message}"
                    }
                    _uiState.update {
                        it.copy(
                            isUploading = false,
                            error = e.message ?: "Ошибка загрузки файла"
                        )
                    }
                }
            )
        }
    }
}
