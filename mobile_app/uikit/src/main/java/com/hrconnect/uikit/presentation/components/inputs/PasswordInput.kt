package com.hrconnect.uikit.presentation.components.inputs

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicSecureTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.hrconnect.uikit.R
import com.hrconnect.uikit.common.theme.HrTheme
import com.hrconnect.uikit.common.theme.Manrope

/**
 * Компонент поля ввода пароля с поддержкой переключения видимости.
 *
 * Ответственность:
 * - Управление отображением пароля (скрыто/показано) через иконку "глаз".
 * - Обработка переключения видимости (onTogglePasswordVisibility).
 * - Отображение лейбла, подсказки, ошибки, левой иконки.
 * - Логирование критических действий пользователя (переключение видимости, ошибки).
 *
 * Дата создания: 31-05-2026
 * Автор: Команда №2
 *
 * @param state состояние текстового поля (содержит пароль)
 * @param isPasswordVisible текущее состояние видимости пароля (true = показан)
 * @param onTogglePasswordVisibility колбэк для переключения видимости
 * @param modifier внешний модификатор для всего поля
 * @param inputModifier дополнительный модификатор для внутреннего BasicSecureTextField
 * @param label текст заголовка
 * @param supportingText вспомогательный текст под полем
 * @param placeholder текст-заполнитель
 * @param isError флаг ошибки (меняет цвета)
 * @param enabled доступно ли поле для ввода
 * @param focused принудительная фокусировка
 * @param leadingIcon иконка слева (null = нет иконки)
 */
@Composable
fun PasswordInput(
    state: TextFieldState,
    isPasswordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    modifier: Modifier = Modifier,
    inputModifier: Modifier = Modifier,
    label: String? = null,
    supportingText: String? = null,
    placeholder: String? = null,
    isError: Boolean = false,
    enabled: Boolean = true,
    focused: Boolean = false,
    leadingIcon: ImageVector? = null,
) {
    // Логирование ошибки (INFO) — критическое состояние поля
    LaunchedEffect(isError) {
        if (isError) {
            Log.i(
                "PasswordInput",
                "Ошибка валидации пароля — label=$label, supportingText=$supportingText"
            )
        }
    }

    // Логирование переключения видимости пароля (INFO) — критическое пользовательское действие
    LaunchedEffect(isPasswordVisible) {
        Log.i(
            "PasswordInput",
            "Пользователь переключил видимость пароля — видимость=${if (isPasswordVisible) "показан" else "скрыт"}, label=$label"
        )
    }

    InputLayout(
        modifier = modifier,
        label = label,
        supportingText = supportingText,
        isError = isError,
        enabled = enabled,
        focused = focused
    ) { styleModifier, interactionSource ->
        BasicSecureTextField(
            modifier = styleModifier.then(inputModifier),
            state = state,
            textObfuscationMode = if (isPasswordVisible) {
                TextObfuscationMode.Visible
            } else {
                TextObfuscationMode.Hidden
            },
            textObfuscationCharacter = '•', // Символ маскирования пароля
            enabled = enabled,
            interactionSource = interactionSource,
            textStyle = TextStyle(
                fontFamily = Manrope,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 1.em,
                letterSpacing = 0.sp,
                color = when {
                    enabled -> HrTheme.colorScheme.onBackground
                    else -> HrTheme.colorScheme.secondary
                }
            ),
            cursorBrush = SolidColor(HrTheme.colorScheme.primary),
            // Декоратор: добавляет иконки и плейсхолдер вокруг поля
            decorator = { innerTextField ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Левая иконка (например, замок) с изменением цвета при ошибке
                    if (leadingIcon != null) {
                        Icon(
                            modifier = Modifier.size(22.dp),
                            imageVector = leadingIcon,
                            contentDescription = null,
                            tint = if (isError) HrTheme.colorScheme.error
                            else HrTheme.colorScheme.secondary
                        )
                    }
                    // Контейнер для текста и плейсхолдера — занимает всё оставшееся пространство
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        // Показываем плейсхолдер, если поле пустое
                        if (state.text.isEmpty() && placeholder != null) {
                            Text(
                                text = placeholder,
                                style = TextStyle(
                                    fontFamily = Manrope,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 16.sp,
                                    lineHeight = 1.em,
                                    letterSpacing = 0.sp,
                                    color = HrTheme.colorScheme.placeholder
                                )
                            )
                        }
                        innerTextField()
                    }
                    // Иконка переключения видимости пароля ("глаз")
                    Icon(
                        modifier = Modifier
                            .size(22.dp)
                            .clickable(
                                interactionSource = null,
                                indication = ripple(bounded = false),
                                onClick = onTogglePasswordVisibility
                            ),
                        imageVector = if (isPasswordVisible) {
                            ImageVector.vectorResource(R.drawable.ic_visibility)
                        } else {
                            ImageVector.vectorResource(R.drawable.ic_visibility_off)
                        },
                        contentDescription = null,
                        tint = if (isError) HrTheme.colorScheme.error
                        else HrTheme.colorScheme.secondary
                    )
                }
            }
        )
    }
}

@Preview
@Composable
private fun InputPreview() {
    HrTheme {
        PasswordInput(
            state = rememberTextFieldState("secret123"),
            isPasswordVisible = true,
            onTogglePasswordVisibility = {}
        )
    }
}