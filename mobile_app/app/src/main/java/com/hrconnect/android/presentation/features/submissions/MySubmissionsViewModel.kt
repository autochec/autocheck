package com.hrconnect.android.presentation.features.submissions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hrconnect.android.domain.model.Submission
import com.hrconnect.android.domain.repository.AuthRepository
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

/** Одноразовые события экрана «Мои задания». */
sealed class MySubmissionsUiEvent {
    object NavigateToLogin : MySubmissionsUiEvent()
}

/**
 * Состояние экрана «Мои задания».
 *
 * @property submissions список загруженных проверок
 * @property isLoading идёт ли загрузка
 * @property error сообщение ошибки (null = нет ошибки)
 */
data class MySubmissionsUiState(
    val submissions: List<Submission> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

/**
 * ViewModel экрана «Мои задания».
 *
 * Ответственность:
 * - Загрузка списка проверок текущего кандидата.
 * - Управление состоянием загрузки и ошибок.
 * - Поддержка обновления списка (pull-to-refresh).
 *
 * Дата создания: 31-05-2026
 * Автор: Команда №2
 */
class MySubmissionsViewModel(
    private val submissionsRepository: SubmissionsRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    companion object {
        private const val TAG = "MySubmissionsViewModel"
    }

    private val _uiState = MutableStateFlow(MySubmissionsUiState())
    val uiState: StateFlow<MySubmissionsUiState> = _uiState.asStateFlow()

    private val _events = Channel<MySubmissionsUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        logcat(TAG) { "[MySubmissionsViewModel]: Инициализация" }
        loadSubmissions()
    }

    /**
     * Загружает список проверок с сервера.
     * Вызывается при старте и при обновлении.
     */
    fun loadSubmissions() {
        logcat(TAG) { "[MySubmissionsViewModel]: Начало загрузки проверок" }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            submissionsRepository.listSubmissions().fold(
                onSuccess = { submissions ->
                    logcat(TAG, DEBUG) {
                        "[MySubmissionsViewModel]: Проверки загружены — count=${submissions.size}"
                    }
                    _uiState.update {
                        it.copy(isLoading = false, submissions = submissions)
                    }
                },
                onFailure = { e ->
                    logcat(TAG, ERROR) {
                        "[MySubmissionsViewModel]: Ошибка загрузки проверок — ${e.message}"
                    }
                    _uiState.update {
                        it.copy(isLoading = false, error = e.message ?: "Ошибка загрузки")
                    }
                }
            )
        }
    }

    /**
     * Выполняет выход из системы: удаляет токен и отправляет событие навигации на Login.
     */
    fun logout() {
        logcat(TAG) { "[MySubmissionsViewModel]: Выход из системы" }
        viewModelScope.launch {
            authRepository.logout()
            _events.send(MySubmissionsUiEvent.NavigateToLogin)
        }
    }

    /**
     * Принудительно обновляет список проверок (pull-to-refresh).
     */
    fun refresh() {
        logcat(TAG) { "[MySubmissionsViewModel]: Обновление списка проверок" }
        loadSubmissions()
    }
}
