package com.hrconnect.uikit.presentation.components.submission_card

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Карточка загрузки задания для кандидата.
 *
 * Ответственность:
 * - Отображение даты загрузки, названия задания, статуса и итогового балла.
 * - Если балл отсутствует (null) — отображается прочерк.
 * - Логирование клика по карточке.
 *
 * Дата создания: 31-05-2026
 * Автор: Команда №2
 *
 * @param submissionDate дата загрузки (timestamp или Date)
 * @param assignmentTitle название задания
 * @param status статус проверки
 * @param score итоговый балл (0..100 или null)
 * @param onClick колбэк клика (опционально)
 * @param modifier внешний модификатор
 */
@Composable
fun SubmissionCard(
    submissionDate: Date,
    assignmentTitle: String,
    status: StatusType,
    score: Int? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val formattedDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(submissionDate)
    val scoreText = score?.let { "$it" } ?: "—"

    LaunchedEffect(Unit) {
        Log.i(
            "SubmissionCard",
            "Отображена карточка загрузки — задание=\"$assignmentTitle\", статус=$status, балл=${score ?: "нет"}"
        )
    }

    val clickHandler = onClick?.let {
        {
            Log.i(
                "SubmissionCard",
                "Клик по карточке — задание=\"$assignmentTitle\", дата=$formattedDate"
            )
            it()
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp), ambientColor = HrTheme.colorScheme.border)
            .clip(RoundedCornerShape(12.dp))
            .background(HrTheme.colorScheme.container)
            .then(if (clickHandler != null) Modifier.clickable(onClick = clickHandler) else Modifier)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Верхняя строка: дата + статус
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formattedDate,
                style = TextStyle(
                    fontFamily = Manrope,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = HrTheme.colorScheme.description
                )
            )
            StatusBadge(status = status)
        }

        // Название задания
        Text(
            text = assignmentTitle,
            style = TextStyle(
                fontFamily = Manrope,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                color = HrTheme.colorScheme.onBackground
            ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        // Итоговый балл
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "Итоговый балл",
                style = TextStyle(
                    fontFamily = Manrope,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = HrTheme.colorScheme.description
                )
            )
            Text(
                text = scoreText,
                style = TextStyle(
                    fontFamily = Manrope,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    lineHeight = 24.sp,
                    color = when {
                        score == null -> HrTheme.colorScheme.description
                        score >= 80 -> HrTheme.colorScheme.success
                        score >= 50 -> HrTheme.colorScheme.warning
                        else -> HrTheme.colorScheme.error
                    }
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SubmissionCardPreview() {
    HrTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SubmissionCard(
                submissionDate = Date(),
                assignmentTitle = "Разработка модуля авторизации",
                status = StatusType.PASSED,
                score = 94,
                onClick = {}
            )
            SubmissionCard(
                submissionDate = Date(System.currentTimeMillis() - 86400000),
                assignmentTitle = "Дизайн архитектуры приложения",
                status = StatusType.RUNNING,
                score = null
            )
            SubmissionCard(
                submissionDate = Date(System.currentTimeMillis() - 172800000),
                assignmentTitle = "Реализация UI-компонентов",
                status = StatusType.FAILED,
                score = 34
            )
        }
    }
}