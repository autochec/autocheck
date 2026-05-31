package com.hrconnect.uikit.presentation.components.badge

import android.util.Log
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hrconnect.uikit.common.theme.HrTheme
import com.hrconnect.uikit.common.theme.Manrope

/**
 * Статусные варианты бейджа.
 */
enum class StatusType {
    PENDING,  // ожидание
    RUNNING,  // выполняется (с анимацией)
    PASSED,   // успешно
    FAILED,   // провал
    ERROR     // ошибка выполнения
}

/**
 * Компонент бейджа статуса (чип).
 *
 * Ответственность:
 * - Отображение статуса с соответствующим цветом фона и текста.
 * - Анимация пульсации для статуса RUNNING.
 * - Логирование отображения критических статусов.
 *
 * Дата создания: 31-05-2026
 * Автор: Команда №2
 *
 * @param status тип статуса
 * @param label текст статуса (если null, используется значение по умолчанию)
 * @param modifier внешний модификатор
 */
@Composable
fun StatusBadge(
    status: StatusType,
    label: String? = null,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(status) {
        Log.i("StatusBadge", "Отображён статус — status=$status, label=${label ?: status.name}")
    }

    // Определяем цвета фона и текста (без сильной прозрачности)
    val (backgroundColor, textColor, shouldAnimate) = when (status) {
        StatusType.PENDING -> Triple(
            HrTheme.colorScheme.neutral.copy(alpha = 0.2f),
            HrTheme.colorScheme.onBackgroundVariant,
            false
        )
        StatusType.RUNNING -> Triple(
            HrTheme.colorScheme.primary.copy(alpha = 0.2f),
            HrTheme.colorScheme.primary,
            true
        )
        StatusType.PASSED -> Triple(
            HrTheme.colorScheme.success.copy(alpha = 0.2f),
            HrTheme.colorScheme.success,
            false
        )
        StatusType.FAILED -> Triple(
            HrTheme.colorScheme.error.copy(alpha = 0.2f),
            HrTheme.colorScheme.error,
            false
        )
        StatusType.ERROR -> Triple(
            HrTheme.colorScheme.warning.copy(alpha = 0.2f),
            HrTheme.colorScheme.warning,
            false
        )
    }

    val displayText = label ?: when (status) {
        StatusType.PENDING -> "Pending"
        StatusType.RUNNING -> "Running"
        StatusType.PASSED -> "Passed"
        StatusType.FAILED -> "Failed"
        StatusType.ERROR -> "Error"
    }

    // Анимация пульсации для RUNNING
    val alphaMultiplier = if (shouldAnimate) {
        val infiniteTransition = rememberInfiniteTransition(label = "running_pulse")
        val alpha by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 0.6f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 800, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
        alpha
    } else {
        1f
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(32.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = displayText,
            style = TextStyle(
                fontFamily = Manrope,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = textColor
            ),
            modifier = Modifier.alpha(alphaMultiplier)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StatusBadgePreview() {
    HrTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .wrapContentSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatusBadge(status = StatusType.PENDING)
                StatusBadge(status = StatusType.RUNNING)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatusBadge(status = StatusType.PASSED)
                StatusBadge(status = StatusType.FAILED)
            }
            StatusBadge(status = StatusType.ERROR)
            StatusBadge(status = StatusType.RUNNING, label = "In Progress")
        }
    }
}