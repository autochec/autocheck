package com.hrconnect.uikit.presentation.components.buttons

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hrconnect.uikit.common.theme.HrTheme
import com.hrconnect.uikit.common.theme.Manrope
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

/**
 * Основная кнопка (Primary) с поддержкой enabled/disabled и эффектом размытия для неактивного состояния.
 *
 * Ответственность:
 * - Отображение кнопки с фоном primary и текстом onPrimary.
 * - Визуализация disabled состояния через haze-эффект (размытие с полупрозрачным оверлеем).
 * - Логирование критического действия при нажатии.
 *
 * Дата создания: 31-05-2026
 * Автор: Команда №2
 *
 * @param label текст на кнопке
 * @param onClick колбэк при нажатии (не вызывается, если enabled = false)
 * @param modifier внешний модификатор
 * @param enabled доступна ли кнопка для нажатия
 */
@Composable
fun PrimaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    // Логирование клика (INFO) — критическое пользовательское действие
    val handleClick = {
        if (enabled) {
            Log.i("PrimaryButton", "Пользователь нажал на основную кнопку — label=\"$label\"")
            onClick()
        }
    }

    val hazeState = rememberHazeState()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                enabled = enabled,
                onClick = handleClick
            )
    ) {
        // Базовый слой с основным цветом
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(HrTheme.colorScheme.primary)
                .hazeSource(hazeState)
        )
        // Для disabled-состояния накладываем эффект размытия с полупрозрачной тонировкой
        if (!enabled) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeEffect(
                        state = hazeState,
                        style = HazeStyle(
                            backgroundColor = HrTheme.colorScheme.primary,
                            tint = HazeTint(HrTheme.colorScheme.onPrimary.copy(alpha = 0.5f)),
                            blurRadius = 0.dp,      // без размытия, только тонировка
                            noiseFactor = 0.5f     // добавляет зернистость
                        )
                    )
            )
        }
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = label,
            style = TextStyle(
                fontFamily = Manrope,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.sp,
                textAlign = TextAlign.Center,
                color = HrTheme.colorScheme.onPrimary
            )
        )
    }
}

@Preview
@Composable
private fun PrimaryButtonPreview() {
    HrTheme {
        PrimaryButton(
            label = "Primary Disabled",
            onClick = {},
            enabled = false
        )
    }
}