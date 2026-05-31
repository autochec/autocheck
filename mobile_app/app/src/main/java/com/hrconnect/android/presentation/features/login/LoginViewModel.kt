package com.hrconnect.android.presentation.features.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hrconnect.android.domain.repository.AuthRepository
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
 * Состояние экрана входа.
 *
 * @property email текущий email в поле ввода
 * @property password текущий пароль в поле ввода
 * @property emailError сообщение ошибки для поля email (null = нет ошибки)
 * @property passwordError сообщение ошибки для поля пароля (null = нет ошибки)
 * @property isLoading идёт ли запрос входа
 * @property generalError общая ошибка (null = нет ошибки)
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val generalError: String? = null,
)

/** Одноразовые события экрана входа. */
sealed class LoginUiEvent {
    object NavigateToMySubmissions : LoginUiEvent()
}

/**
 * ViewModel экрана входа.
 *
 * Ответственность:
 * - Валидация полей email и пароля.
 * - Выполнение запроса входа через AuthRepository.
 * - Управление состоянием UI (загрузка, ошибки).
 * - Отправка одноразовых событий навигации.
 *
 * Дата создания: 31-05-2026
 * Автор: Команда №2
 */
class LoginViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    companion object {
        private const val TAG = "LoginViewModel"
    }

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = Channel<LoginUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        logcat(TAG) { "[LoginViewModel]: Инициализация" }
    }

    /**
     * Обновляет поле email в состоянии и сбрасывает ошибку.
     *
     * @param value новое значение email
     */
    fun onEmailChanged(value: String) {
        _uiState.update { it.copy(email = value, emailError = null, generalError = null) }
    }

    /**
     * Обновляет поле пароля в состоянии и сбрасывает ошибку.
     *
     * @param value новое значение пароля
     */
    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null, generalError = null) }
    }

    /**
     * Запускает процесс входа с валидацией полей.
     * Критическое пользовательское действие — логируется INFO.
     */
    fun login() {
        val state = _uiState.value
        logcat(TAG) { "[LoginViewModel]: Попытка входа — email=${state.email}" }

        // Валидация полей перед отправкой запроса
        if (!validateFields(state)) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }
            authRepository.login(state.email, state.password).fold(
                onSuccess = { (_, _) ->
                    logcat(TAG, DEBUG) { "[LoginViewModel]: Вход выполнен успешно" }
                    _uiState.update { it.copy(isLoading = false) }
                    _events.send(LoginUiEvent.NavigateToMySubmissions)
                },
                onFailure = { e ->
                    logcat(TAG, ERROR) { "[LoginViewModel]: Ошибка входа — ${e.message}" }
                    _uiState.update {
                        it.copy(isLoading = false, generalError = e.message ?: "Ошибка входа")
                    }
                }
            )
        }
    }

    /**
     * Валидирует поля email и пароль.
     * Обновляет состояние с ошибками при неверных данных.
     *
     * @param state текущее состояние экрана
     * @return true если все поля валидны
     */
    private fun validateFields(state: LoginUiState): Boolean {
        var isValid = true
        var emailError: String? = null
        var passwordError: String? = null

        if (state.email.isBlank()) {
            emailError = "Введите email"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            emailError = "Неверный формат email"
            isValid = false
        }

        if (state.password.isBlank()) {
            passwordError = "Введите пароль"
            isValid = false
        } else if (state.password.length < 4) {
            passwordError = "Пароль слишком короткий"
            isValid = false
        }

        if (!isValid) {
            logcat(TAG) {
                "[LoginViewModel]: Ошибка валидации — emailError=$emailError, passwordError=$passwordError"
            }
            _uiState.update { it.copy(emailError = emailError, passwordError = passwordError) }
        }
        return isValid
    }
}
