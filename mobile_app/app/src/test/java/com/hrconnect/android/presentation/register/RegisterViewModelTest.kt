package com.hrconnect.android.presentation.register

import com.google.common.truth.Truth.assertThat
import com.hrconnect.android.data.validator.RegisterValidatorImpl
import com.hrconnect.android.domain.repository.AuthRepository
import com.hrconnect.android.domain.validator.RegisterValidator
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException

class RegisterViewModelTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var registerValidator: RegisterValidator
    private lateinit var viewModel: RegisterViewModel

    @Before
    fun setUp() {
        authRepository = mockk()
        registerValidator = RegisterValidatorImpl()
        viewModel = RegisterViewModel(
            authRepository = authRepository,
            registerValidator = registerValidator
        )
    }

    @Test
    fun `при успешной регистрации осуществляется переход на экран авторизации`() = runTest {
        fillValidFormInputs()
        coEvery { authRepository.register() } returns Result.success(Unit)
        viewModel.onAction(RegisterAction.OnRegisterClick)
        assertThat(viewModel.events.first()).isEqualTo(RegisterEvent.OnRegisterSuccess)
    }

    @Test
    fun `при ответе сервера с ошибкой отображается соответствующее сообщение об ошибке`() =
        runTest {
            fillValidFormInputs()
            val errorMessage = "409 Conflict — email уже зарегистрирован"
            coEvery { authRepository.register() } returns Result.failure(
                mockk<HttpException> {
                    every { code() } returns 409
                    every { message() } returns errorMessage
                }
            )
            viewModel.onAction(RegisterAction.OnRegisterClick)
            assertThat(viewModel.events.first()).isEqualTo(RegisterEvent.OnError(errorMessage))
        }

    private fun fillValidFormInputs() {
        val currentState = viewModel.state.value
        currentState.firstNameState.edit {
            replace(0, length, "Джон")
        }
        currentState.lastNameState.edit {
            replace(0, length, "Доу")
        }
        currentState.emailState.edit {
            replace(0, length, "name@domain.ru")
        }
        currentState.passwordState.edit {
            replace(0, length, "12345678")
        }
        currentState.confirmPasswordState.edit {
            replace(0, length, "12345678")
        }
    }
}