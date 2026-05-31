package com.hrconnect.android.domain.validator

import com.google.common.truth.Truth.assertThat
import com.hrconnect.android.data.validator.RegisterValidatorImpl
import org.junit.Before
import org.junit.Test

/**
 * Тесты валидвалидатора регистрации.
 *
 * Дата создания: 26-05-2026.
 * Автор создания: 1.
 */
class RegisterValidatorImplTest {

    private lateinit var validator: RegisterValidator

    @Before
    fun setUp() {
        validator = RegisterValidatorImpl()
    }

    @Test
    fun `корректный формат Email возвращает true`() {
        val isEmailValid = validator.validateEmail("name@domain.ru")
        assertThat(isEmailValid).isTrue()
    }

    @Test
    fun `некорректный формат Email возвращает false`() {
        val isEmailValid = validator.validateEmail("NAME@DOMAIN.RU")
        assertThat(isEmailValid).isFalse()
    }

    @Test
    fun `пароль длиной менее 8 символов возвращает false`() {
        val isPasswordValid = validator.validatePassword("123")
        assertThat(isPasswordValid).isFalse()
    }

    @Test
    fun `корректный пароль возвращает true`() {
        val isPasswordValid = validator.validatePassword("12345678")
        assertThat(isPasswordValid).isTrue()
    }

    @Test
    fun `несовпадение пароля и подтверждения пароля возвращается false`() {
        val isConfirmPasswordValid = validator.validateConfirmPassword(
            password = "12345678",
            confirmPassword = "123"
        )
        assertThat(isConfirmPasswordValid).isFalse()
    }
}