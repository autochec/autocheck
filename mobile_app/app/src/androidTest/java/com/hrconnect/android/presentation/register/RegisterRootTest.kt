package com.hrconnect.android.presentation.register

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.hrconnect.android.common.util.SupportingTextColor
import com.hrconnect.android.common.util.TestTags
import com.hrconnect.android.data.validator.RegisterValidatorImpl
import com.hrconnect.android.domain.repository.AuthRepository
import com.hrconnect.android.domain.validator.RegisterValidator
import com.hrconnect.uikit.common.theme.HrTheme
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Тесты экрана регистрации.
 *
 * Дата создания: 26-05-2026.
 * Автор создания: 1.
 */
class RegisterRootTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var registerValidator: RegisterValidator
    private lateinit var viewModel: RegisterViewModel
    private var errorColor: Color = Color.Unspecified

    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        authRepository = mockk()
        registerValidator = RegisterValidatorImpl()
        viewModel = RegisterViewModel(
            authRepository = authRepository,
            registerValidator = registerValidator
        )

        composeRule.setContent {
            HrTheme {
                errorColor = HrTheme.colorScheme.error

                RegisterRoot(
                    viewModel = viewModel,
                    onLoginClick = {},
                    onRegisterSuccess = {}
                )
            }
        }
    }

    @Test
    fun invalidEmail_showsError() {
        fillFormInputs(email = "NAME@DOMAIN.RU")
        composeRule.onNodeWithTag(TestTags.REGISTER_BUTTON)
            .performClick()

        composeRule.onNode(
            hasTestTag(TestTags.EMAIL_INPUT) and SemanticsMatcher.expectValue(
                key = SupportingTextColor,
                expectedValue = errorColor
            )
        ).assertIsDisplayed()
    }

    @Test
    fun invalidPassword_showsError() {
        fillFormInputs(password = "123")
        composeRule.onNodeWithTag(TestTags.REGISTER_BUTTON)
            .performClick()

        composeRule.onNode(
            hasTestTag(TestTags.PASSWORD_INPUT) and SemanticsMatcher.expectValue(
                key = SupportingTextColor,
                expectedValue = errorColor
            )
        ).assertIsDisplayed()
    }

    private fun fillFormInputs(
        firstName: String = "Джон",
        lastName: String = "Доу",
        email: String = "name@domain.ru",
        password: String = "12345678",
        confirmPassword: String = password,
        acceptedTerms: Boolean = true,
    ) {
        composeRule.onNodeWithTag(TestTags.FIRST_NAME_INPUT)
            .performTextInput(firstName)
        composeRule.onNodeWithTag(TestTags.LAST_NAME_INPUT)
            .performTextInput(lastName)
        composeRule.onNodeWithTag(TestTags.EMAIL_INPUT)
            .performTextInput(email)
        composeRule.onNodeWithTag(TestTags.PASSWORD_INPUT)
            .performTextInput(password)
        composeRule.onNodeWithTag(TestTags.CONFIRM_PASSWORD_INPUT)
            .performTextInput(confirmPassword)
        if (acceptedTerms) {
            composeRule.onNodeWithTag(TestTags.TERMS_CHECKBOX)
                .performClick()
        }
    }
}