package com.hrconnect.uikit.presentation.components.checkbox

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hrconnect.uikit.R
import com.hrconnect.uikit.common.theme.HrTheme

/**
 * Компонент чекбокса с кастомной стилизацией и поддержкой аннотированной метки.
 *
 * Ответственность:
 * - Отображение состояния "выбран/не выбран" (checked).
 * - Обработка клика с учётом enabled.
 * - Визуальная индикация состояния через цвет фона, границу и галку.
 *
 * Дата создания: 31-05-2026
 * Автор: Команда №2
 *
 * @param checked текущее состояние (true = выбран)
 * @param onCheckedChange колбэк, вызываемый при клике (передаёт новое состояние)
 * @param modifier внешний модификатор для Row
 * @param checkboxModifier модификатор для внутреннего Box (чекбокса)
 * @param label аннотированный текст метки (null = не отображается)
 * @param enabled доступен ли чекбокс для взаимодействия
 */
@Composable
fun HrCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    checkboxModifier: Modifier = Modifier,
    label: AnnotatedString? = null,
    enabled: Boolean = true,
) {
    // Логирование изменения состояния чекбокса (INFO) — критическое пользовательское действие
    LaunchedEffect(checked) {
        Log.i(
            "HrCheckbox",
            "Пользователь изменил состояние чекбокса — checked=$checked, label=${label?.text ?: "null"}"
        )
    }

    Row(
        modifier = modifier.alpha(
            alpha = if (enabled) 1f else 0.5f // Визуальное ослабление disabled компонента
        ),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = checkboxModifier
                .size(24.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(
                    color = when {
                        checked -> HrTheme.colorScheme.primary          // Выбран — заливка цветом primary
                        enabled -> HrTheme.colorScheme.background      // Не выбран, доступен — фон как у поля
                        else -> HrTheme.colorScheme.checkboxContainerDisabled // Не выбран, disabled — серый фон
                    }
                )
                .then(
                    // Добавляем границу только если чекбокс не выбран
                    if (!checked) {
                        Modifier.border(
                            width = 2.dp,
                            color = if (enabled) {
                                HrTheme.colorScheme.description
                            } else {
                                HrTheme.colorScheme.border
                            },
                            shape = RoundedCornerShape(4.dp)
                        )
                    } else Modifier
                )
                .clickable(
                    enabled = enabled,
                    onClick = {
                        onCheckedChange(!checked)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            // Отображаем галку только в выбранном состоянии
            if (checked) {
                Icon(
                    modifier = Modifier.size(9.51.dp),
                    imageVector = ImageVector.vectorResource(R.drawable.ic_check),
                    contentDescription = null,
                    tint = HrTheme.colorScheme.onPrimary
                )
            }
        }
        if (label != null) {
            Text(
                text = label,
                style = HrTheme.typography.bodyMedium
            )
        }
    }
}

@Preview
@Composable
private fun HrCheckboxPreview() {
    HrTheme {
        HrCheckbox(
            checked = true,
            onCheckedChange = {},
            label = AnnotatedString("Checked")
        )
    }
}