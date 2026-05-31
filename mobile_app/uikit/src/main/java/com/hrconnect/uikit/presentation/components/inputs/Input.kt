package com.hrconnect.uikit.presentation.components.inputs

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.hrconnect.uikit.common.theme.HrTheme
import com.hrconnect.uikit.common.theme.Manrope

/**
 * Компонент текстового поля ввода с поддержкой лейбла, подсказки, иконок и валидации.
 *
 * Ответственность:
 * - Управление текстовым состоянием (TextFieldState).
 * - Отображение иконок (leading/trailing) с правильной стилизацией при ошибке.
 * - Отображение плейсхолдера поверх текста.
 * - Делегирование визуальной обёртки компоненту [InputLayout].
 * - Логирование пользовательского ввода (при потере фокуса) и ошибок.
 *
 * Дата создания: 31-05-2026
 * Автор: Команда №2
 *
 * @param state состояние текстового поля (содержит текст, выделение и т.д.)
 * @param modifier внешний модификатор для всего поля
 * @param inputModifier дополнительный модификатор для внутреннего BasicTextField
 * @param label текст заголовка (передаётся в InputLayout)
 * @param supportingText вспомогательный текст под полем
 * @param placeholder текст-заполнитель (пока поле пустое)
 * @param isError флаг ошибки (меняет цвета границы, иконок)
 * @param enabled доступно ли поле для ввода
 * @param focused принудительная фокусировка (внешнее управление)
 * @param singleLine однострочный режим (SingleLine) или многострочный (Default)
 * @param leadingIcon иконка слева (null = нет иконки)
 * @param trailingIcon иконка справа (null = нет иконки)
 */
@Composable
fun Input(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    inputModifier: Modifier = Modifier,
    label: String? = null,
    supportingText: String? = null,
    placeholder: String? = null,
    isError: Boolean = false,
    enabled: Boolean = true,
    focused: Boolean = false,
    singleLine: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
) {
    // Логирование ошибки (INFO) — дополнительно к логированию в InputLayout
    LaunchedEffect(isError) {
        if (isError) {
            Log.i("Input", "Ошибка валидации в поле — label=$label, supportingText=$supportingText")
        }
    }

    // Логирование завершения ввода при потере фокуса
    // Используем interactionSource из InputLayout, но он недоступен напрямую.
    // Поэтому создадим отдельный источник и передадим в InputLayout?
    // Проще получить фокус через remember и LaunchedEffect, но InputLayout уже содержит свой interactionSource.
    // Альтернатива: добавить обратный вызов onFocusLost. Но для чистоты — запомним предыдущее состояние фокуса.
    var wasFocused by remember { mutableStateOf(false) }
    // Здесь нет прямого доступа к фокусу, так как interactionSource создаётся внутри InputLayout.
    // Вместо этого используем хак: передадим callback через замыкание? Неэлегантно.
    // Поэтому лучше оставить логирование фокуса и текста на уровне InputLayout (уже есть).
    // А в Input добавим логирование при изменении текста (только если поле было отредактировано пользователем).
    // Для этого отслеживаем изменения state.text и логируем первое изменение.
    var initialText by remember { mutableStateOf(state.text.toString()) }
    LaunchedEffect(state.text) {
        val currentText = state.text.toString()
        if (currentText != initialText) {
            Log.i(
                "Input",
                "Пользователь изменил содержимое поля — label=$label, длина=${currentText.length}"
            )
            initialText = currentText
        }
    }

    InputLayout(
        modifier = modifier,
        label = label,
        supportingText = supportingText,
        isError = isError,
        enabled = enabled,
        focused = focused
    ) { styleModifier, interactionSource ->
        BasicTextField(
            modifier = styleModifier.then(inputModifier),
            state = state,
            interactionSource = interactionSource,
            enabled = enabled,
            lineLimits = if (singleLine) {
                TextFieldLineLimits.SingleLine
            } else {
                TextFieldLineLimits.Default
            },
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
            // Декоратор: добавляет иконки и плейсхолдер вокруг BasicTextField
            decorator = { innerTextField ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Левая иконка (leading) с изменением цвета при ошибке
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
                        // Показываем плейсхолдер, если поле пустое и задан placeholder
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
                        innerTextField() // Сам вводимый текст
                    }
                    // Правая иконка (trailing)
                    if (trailingIcon != null) {
                        Icon(
                            modifier = Modifier.size(22.dp),
                            imageVector = trailingIcon,
                            contentDescription = null,
                            tint = if (isError) HrTheme.colorScheme.error
                            else HrTheme.colorScheme.secondary
                        )
                    }
                }
            }
        )
    }
}

@Preview
@Composable
private fun InputPreview() {
    HrTheme {
        Input(
            state = rememberTextFieldState(),
            label = "Default Input",
            placeholder = "Enter text..."
        )
    }
}