package com.hrconnect.android.presentation.features.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hrconnect.android.common.util.ObserveAsEvents
import com.hrconnect.uikit.common.theme.HrTheme
import com.hrconnect.uikit.common.theme.Manrope
import com.hrconnect.uikit.presentation.components.buttons.PrimaryButton
import com.hrconnect.uikit.presentation.components.inputs.Input
import com.hrconnect.uikit.presentation.components.inputs.PasswordInput
import org.koin.compose.viewmodel.koinViewModel

/**
 * Экран авторизации кандидата.
 *
 * Ответственность:
 * - Отображение полей email и пароль.
 * - Валидация ввода и показ ошибок под полями.
 * - Кнопка «Войти» с индикатором загрузки.
 * - Навигация на экран «Мои задания» после успешного входа.
 *
 * Дата создания: 31-05-2026
 * Автор: Команда №2
 *
 * @param onNavigateToMySubmissions колбэк перехода на главный экран
 */
@Composable
fun LoginScreen(
    onNavigateToMySubmissions: () -> Unit,
    viewModel: LoginViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    // Обработка одноразовых событий навигации
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is LoginUiEvent.NavigateToMySubmissions -> onNavigateToMySubmissions()
        }
    }

    // Состояния текстовых полей
    val emailFieldState = rememberTextFieldState(uiState.email)
    val passwordFieldState = rememberTextFieldState()
    var isPasswordVisible by remember { mutableStateOf(false) }

    // Синхронизация состояния поля email с ViewModel
    LaunchedEffect(emailFieldState.text) {
        viewModel.onEmailChanged(emailFieldState.text.toString())
    }
    LaunchedEffect(passwordFieldState.text) {
        viewModel.onPasswordChanged(passwordFieldState.text.toString())
    }

    Scaffold(
        containerColor = HrTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.Center
        ) {
            // Заголовок экрана
            Text(
                text = "Вход в AutoCheckMobile",
                style = TextStyle(
                    fontFamily = Manrope,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    lineHeight = 32.sp,
                    color = HrTheme.colorScheme.onBackground
                )
            )
            Text(
                text = "Войдите, чтобы просматривать свои проверки",
                style = TextStyle(
                    fontFamily = Manrope,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = HrTheme.colorScheme.description
                ),
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Поле email
            Input(
                state = emailFieldState,
                label = "Email",
                placeholder = "example@mail.ru",
                isError = uiState.emailError != null,
                supportingText = uiState.emailError,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Поле пароля
            PasswordInput(
                state = passwordFieldState,
                isPasswordVisible = isPasswordVisible,
                onTogglePasswordVisibility = { isPasswordVisible = !isPasswordVisible },
                label = "Пароль",
                placeholder = "Введите пароль",
                isError = uiState.passwordError != null,
                supportingText = uiState.passwordError,
                modifier = Modifier.fillMaxWidth()
            )

            // Общая ошибка (например, неверные учётные данные)
            if (uiState.generalError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.generalError!!,
                    style = TextStyle(
                        fontFamily = Manrope,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        color = HrTheme.colorScheme.error
                    )
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Кнопка входа — неактивна во время загрузки
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = HrTheme.colorScheme.primary
                )
            } else {
                PrimaryButton(
                    label = "Войти",
                    onClick = { viewModel.login() },
                    enabled = uiState.email.isNotBlank() && uiState.password.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
