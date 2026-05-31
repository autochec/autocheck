package com.hrconnect.uikit.presentation.components.progress_bar

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hrconnect.uikit.common.theme.HrTheme
import com.hrconnect.uikit.common.theme.Manrope

/**
 * Линейный прогресс-бар с процентным отображением и цветовой индикацией.
 *
 * Ответственность:
 * - Отображение прогресса в процентах (0..100).
 * - Автоматическое изменение цвета в зависимости от значения:
 *   красный (<50%), жёлтый (50-80%), зелёный (>80%).
 * - Логирование достижения критических значений (50%, 80%, 100%).
 *
 * Дата создания: 31-05-2026
 * Автор: Команда №2
 *
 * @param progress процент выполнения (0..100)
 * @param showLabel показывать ли числовое значение справа
 * @param modifier внешний модификатор
 */
@Composable
fun LinearProgressBar(
    progress: Int,
    showLabel: Boolean = true,
    modifier: Modifier = Modifier,
) {
    // Ограничиваем прогресс диапазоном 0..100
    val clampedProgress = progress.coerceIn(0, 100)
    val fraction = clampedProgress / 100f

    // Определяем цвет в зависимости от прогресса
    val barColor = when {
        clampedProgress < 50 -> HrTheme.colorScheme.error
        clampedProgress <= 80 -> HrTheme.colorScheme.warning
        else -> HrTheme.colorScheme.success
    }

    // Логирование критических изменений прогресса (INFO)
    LaunchedEffect(clampedProgress) {
        when {
            clampedProgress == 100 -> Log.i("LinearProgressBar", "Прогресс завершён — 100%")
            clampedProgress in 80..99 -> Log.i(
                "LinearProgressBar",
                "Прогресс достиг 80%+ — значение=$clampedProgress%"
            )
            clampedProgress in 50..79 -> Log.i(
                "LinearProgressBar",
                "Прогресс пересёк 50% — значение=$clampedProgress%"
            )
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Фон трека (серый)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(HrTheme.colorScheme.border)
            ) {
                // Заполненная часть
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(barColor)
                )
            }
            // Процентный лейбл (опционально)
            if (showLabel) {
                Text(
                    text = "$clampedProgress%",
                    style = TextStyle(
                        fontFamily = Manrope,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = HrTheme.colorScheme.onBackgroundVariant
                    ),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LinearProgressBarPreview() {
    HrTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
        ) {
            LinearProgressBar(progress = 30, showLabel = true)   // красный
            LinearProgressBar(progress = 65, showLabel = true)   // жёлтый
            LinearProgressBar(progress = 90, showLabel = true)   // зелёный
            LinearProgressBar(progress = 100, showLabel = true)  // зелёный 100%
            LinearProgressBar(progress = 45, showLabel = false)  // без метки
        }
    }
}