package com.hrconnect.uikit.presentation.components.progress_bar

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hrconnect.uikit.common.theme.HrTheme

/**
 * Компонент индикатора прогресса (шаги/страницы).
 *
 * Ответственность:
 * - Отображение текущего прогресса в виде последовательности точек/сегментов.
 * - Визуализация пройденных (primary), текущего (primary с alpha 0.5) и будущих (indicatorTrack) шагов.
 *
 * Дата создания: 31-05-2026
 * Автор: Команда №2
 *
 * @param currentPage текущая страница (индекс от 0 до pageCount-1)
 * @param pageCount общее количество страниц
 * @param modifier внешний модификатор
 */
@Composable
fun ProgressBar(
    currentPage: Int,
    pageCount: Int,
    modifier: Modifier = Modifier,
) {
    // Логирование изменения прогресса (INFO) — критическое пользовательское действие
    LaunchedEffect(currentPage, pageCount) {
        Log.i(
            "ProgressBar",
            "Прогресс обновлён — текущая страница=$currentPage, всего страниц=$pageCount"
        )
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(pageCount) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp)
                    .background(
                        color = when {
                            index < currentPage -> HrTheme.colorScheme.primary                // пройденный шаг
                            index == currentPage -> HrTheme.colorScheme.primary.copy(alpha = 0.5f) // текущий шаг (полупрозрачный)
                            else -> HrTheme.colorScheme.indicatorTrack                        // будущий шаг
                        },
                        shape = CircleShape
                    )
            )
        }
    }
}