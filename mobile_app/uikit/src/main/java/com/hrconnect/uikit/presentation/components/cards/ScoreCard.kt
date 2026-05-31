package com.hrconnect.uikit.presentation.components.score_card

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hrconnect.uikit.common.theme.HrTheme
import com.hrconnect.uikit.common.theme.Manrope
import com.hrconnect.uikit.presentation.components.badge.StatusBadge
import com.hrconnect.uikit.presentation.components.badge.StatusType

/**
 * Карточка с результатами задания для кандидата.
 *
 * Ответственность:
 * - Отображение крупного балла (score) с цветовой индикацией в зависимости от значения.
 * - Показ названия задания, имени кандидата и статуса проверки.
 * - Логирование клика по карточке (переход к деталям).
 *
 * Дата создания: 31-05-2026
 * Автор: Команда №2
 *
 * @param score итоговый балл (0..100)
 * @param assignmentTitle название задания
 * @param candidateName имя кандидата
 * @param status статус проверки (используется StatusBadge)
 * @param onClick колбэк клика по карточке (опционально)
 * @param modifier внешний модификатор
 */
@Composable
fun ScoreCard(
    score: Int,
    assignmentTitle: String,
    candidateName: String,
    status: StatusType,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val clampedScore = score.coerceIn(0, 100)

    // Цветовая индикация балла (фон круга)
    val scoreColor = when {
        clampedScore >= 80 -> HrTheme.colorScheme.success
        clampedScore >= 50 -> HrTheme.colorScheme.warning
        else -> HrTheme.colorScheme.error
    }

    // Логирование отображения карточки с высоким/низким баллом
    LaunchedEffect(clampedScore) {
        when {
            clampedScore >= 90 -> Log.i(
                "ScoreCard",
                "Отличный результат — балл=$clampedScore, задание=\"$assignmentTitle\", кандидат=\"$candidateName\""
            )
            clampedScore <= 30 -> Log.i(
                "ScoreCard",
                "Низкий балл — балл=$clampedScore, задание=\"$assignmentTitle\", кандидат=\"$candidateName\""
            )
        }
    }

    val clickHandler: (() -> Unit)? = onClick?.let {
        {
            Log.i(
                "ScoreCard",
                "Клик по карточке — задание=\"$assignmentTitle\", кандидат=\"$candidateName\", балл=$clampedScore"
            )
            it()
        }
    }

    CardContainer(
        modifier = modifier,
        onClick = clickHandler
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Круг с крупным баллом
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(scoreColor.copy(alpha = 0.15f))
                    .border(2.dp, scoreColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$clampedScore",
                    style = TextStyle(
                        fontFamily = Manrope,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        lineHeight = 28.sp,
                        color = scoreColor
                    )
                )
            }

            // Информация о задании и кандидате
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = assignmentTitle,
                    style = TextStyle(
                        fontFamily = Manrope,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        lineHeight = 20.sp,
                        color = HrTheme.colorScheme.onBackground
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = candidateName,
                    style = TextStyle(
                        fontFamily = Manrope,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        color = HrTheme.colorScheme.secondary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                StatusBadge(
                    status = status,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

/**
 * Контейнер карточки с тенью и скруглением.
 */
@Composable
private fun CardContainer(
    modifier: Modifier,
    onClick: (() -> Unit)?,
    content: @Composable () -> Unit,
) {
    val finalModifier = modifier
        .fillMaxWidth()
        .shadow(
            elevation = 2.dp,
            shape = RoundedCornerShape(12.dp),
            ambientColor = Color.Black.copy(alpha = 0.05f),
            spotColor = Color.Black.copy(alpha = 0.05f)
        )
        .clip(RoundedCornerShape(12.dp))
        .background(HrTheme.colorScheme.container)
        .padding(16.dp)

    if (onClick != null) {
        Box(
            modifier = finalModifier.clickable(
                onClick = onClick
            )
        ) {
            content()
        }
    } else {
        Box(modifier = finalModifier) {
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ScoreCardPreview() {
    HrTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ScoreCard(
                score = 92,
                assignmentTitle = "Kotlin Multiplatform Mobile",
                candidateName = "Алексей Иванов",
                status = StatusType.PASSED,
                onClick = {}
            )
            ScoreCard(
                score = 67,
                assignmentTitle = "Compose UI Implementation",
                candidateName = "Мария Петрова",
                status = StatusType.RUNNING
            )
            ScoreCard(
                score = 34,
                assignmentTitle = "Clean Architecture",
                candidateName = "Дмитрий Смирнов",
                status = StatusType.FAILED
            )
            ScoreCard(
                score = 100,
                assignmentTitle = "Very Long Assignment Title That Should Be Ellipsized",
                candidateName = "Екатерина Долгая",
                status = StatusType.PENDING
            )
        }
    }
}