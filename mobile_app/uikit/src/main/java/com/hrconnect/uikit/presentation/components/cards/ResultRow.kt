package com.hrconnect.uikit.presentation.components.cards

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hrconnect.uikit.R
import com.hrconnect.uikit.common.theme.HrTheme
import com.hrconnect.uikit.common.theme.Manrope
import com.hrconnect.uikit.presentation.components.badge.StatusBadge
import com.hrconnect.uikit.presentation.components.badge.StatusType

/**
 * Строка результата проверки с возможностью раскрытия деталей.
 *
 * Ответственность:
 * - Отображение названия проверки, статуса и балла.
 * - Возможность развернуть/свернуть дополнительную информацию.
 * - Логирование раскрытия деталей.
 *
 * Дата создания: 31-05-2026
 * Автор: Команда №2
 *
 * @param checkName название вида проверки
 * @param status статус проверки
 * @param score балл (0..100)
 * @param details дополнительный текст (раскрывается при клике)
 * @param modifier внешний модификатор
 */
@Composable
fun ResultRow(
    checkName: String,
    status: StatusType,
    score: Int,
    details: String? = null,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val hasDetails = !details.isNullOrBlank()

    LaunchedEffect(expanded) {
        if (expanded) {
            Log.i("ResultRow", "Раскрыты детали проверки — check=\"$checkName\", балл=$score")
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(HrTheme.colorScheme.container)
            .padding(vertical = 8.dp)
    ) {
        // Основная строка
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = hasDetails) { expanded = !expanded }
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Иконка стрелки (только если есть детали)
            if (hasDetails) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_arrow_down),
                    contentDescription = null,
                    modifier = Modifier.rotate(if (expanded) 180f else 0f),
                    tint = HrTheme.colorScheme.secondary
                )
            } else {
                // Заглушка для отступа (если нет стрелки)
                androidx.compose.foundation.layout.Spacer(modifier = Modifier)
            }

            // Название проверки
            Text(
                text = checkName,
                style = TextStyle(
                    fontFamily = Manrope,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    color = HrTheme.colorScheme.onBackground
                ),
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Статус
            StatusBadge(status = status, modifier = Modifier)

            // Балл
            Text(
                text = "$score",
                style = TextStyle(
                    fontFamily = Manrope,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    color = when {
                        score >= 80 -> HrTheme.colorScheme.success
                        score >= 50 -> HrTheme.colorScheme.warning
                        else -> HrTheme.colorScheme.error
                    }
                ),
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Раскрываемая детальная информация
        if (expanded && hasDetails) {
            Text(
                text = details,
                style = TextStyle(
                    fontFamily = Manrope,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = HrTheme.colorScheme.description
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 32.dp, end = 12.dp, bottom = 12.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ResultRowPreview() {
    HrTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            ResultRow(
                checkName = "Статический анализ кода",
                status = StatusType.PASSED,
                score = 95,
                details = "Найдено 2 предупреждения о стиле кода. Ошибок нет."
            )
            ResultRow(
                checkName = "Модульные тесты",
                status = StatusType.RUNNING,
                score = 72,
                details = "Пройдено 18 из 25 тестов. Остальные выполняются."
            )
            ResultRow(
                checkName = "Проверка безопасности",
                status = StatusType.FAILED,
                score = 32,
                details = "Обнаружены уязвимости: SQL injection в модуле входа."
            )
            ResultRow(
                checkName = "Без деталей",
                status = StatusType.PENDING,
                score = 0,
                details = null
            )
        }
    }
}