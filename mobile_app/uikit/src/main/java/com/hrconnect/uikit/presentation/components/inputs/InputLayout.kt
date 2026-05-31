package com.hrconnect.uikit.presentation.components.inputs

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hrconnect.uikit.common.theme.HrTheme
import com.hrconnect.uikit.common.theme.Manrope

/**
 * Компонент обёртки для полей ввода (TextField, Dropdown и т.д.).
 *
 * Отвечает за:
 * - Отображение лейбла, вспомогательного текста и состояния ошибки.
 * - Визуальную индикацию фокуса, ошибки, enabled/disabled.
 * - Логирование взаимодействий: фокус/потеря фокуса, появление ошибки.
 *
 * Дата создания: 31-05-2026
 * Автор: Команда №2
 *
 * @param modifier внешний модификатор
 * @param label текст заголовка (null = скрыт)
 * @param supportingText вспомогательный текст под полем
 * @param isError флаг ошибки (меняет цвет и логирует переход)
 * @param enabled доступен ли ввод
 * @param focused принудительная фокусировка (внешнее управление)
 * @param input лямбда, рисующая сам элемент ввода
 */
@Composable
fun InputLayout(
    modifier: Modifier = Modifier,
    label: String? = null,
    supportingText: String? = null,
    isError: Boolean = false,
    enabled: Boolean = true,
    focused: Boolean = false,
    input: @Composable (Modifier, MutableInteractionSource) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    // Логирование фокуса (INFO)
    LaunchedEffect(isFocused) {
        if (isFocused) {
            Log.i("InputLayout", "Ввод активирован — поле получило фокус, label=$label")
        } else {
            Log.i("InputLayout", "Ввод деактивирован — поле потеряло фокус, label=$label")
        }
    }

    // Логирование ошибки (INFO при изменении)
    LaunchedEffect(isError) {
        if (isError) {
            Log.i(
                "InputLayout",
                "Пользовательское действие: ошибка валидации — label=$label, supportingText=$supportingText"
            )
        }
    }

    val inputStyleModifier = Modifier
        .fillMaxWidth()
        .background(
            color = if (enabled) HrTheme.colorScheme.background
            else HrTheme.colorScheme.inputContainerDisabled,
            shape = RoundedCornerShape(8.dp)
        )
        .border(
            width = if (focused || isFocused || isError) 2.dp else 1.dp,
            // Определение цвета границы: приоритет у ошибки, затем фокус, затем стандартный
            color = when {
                isError -> HrTheme.colorScheme.error
                focused || isFocused -> HrTheme.colorScheme.primary
                else -> HrTheme.colorScheme.border
            },
            shape = RoundedCornerShape(8.dp)
        )
        .padding(horizontal = 16.dp, vertical = 12.dp)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (label != null) {
            Text(
                text = label,
                style = HrTheme.typography.fieldLabel,
                color = when {
                    isError -> HrTheme.colorScheme.error
                    focused || isFocused -> HrTheme.colorScheme.primary
                    enabled -> HrTheme.colorScheme.fieldLabel
                    else -> HrTheme.colorScheme.description
                }
            )
        }
        input(inputStyleModifier, interactionSource)
        if (supportingText != null) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = supportingText,
                style = TextStyle(
                    fontFamily = Manrope,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    letterSpacing = 0.sp
                ),
                color = when {
                    isError -> HrTheme.colorScheme.error
                    enabled -> HrTheme.colorScheme.fieldLabel
                    else -> HrTheme.colorScheme.description
                }
            )
        }
    }
}