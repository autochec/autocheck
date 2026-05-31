package com.hrconnect.uikit.presentation.components.buttons

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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

/**
 * Вторичная кнопка (контурная) с рамкой primary цвета.
 *
 * Ответственность:
 * - Отображение кнопки с прозрачным фоном, цветной рамкой и текстом primary.
 * - Обработка клика с логированием.
 *
 * Дата создания: 31-05-2026
 * Автор: Команда №2
 *
 * @param label текст на кнопке
 * @param onClick колбэк при нажатии
 * @param modifier внешний модификатор
 */
@Composable
fun SecondaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Логирование клика (INFO) — критическое пользовательское действие
    val handleClick = {
        Log.i("SecondaryButton", "Пользователь нажал на вторичную кнопку — label=\"$label\"")
        onClick()
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 2.dp,
                color = HrTheme.colorScheme.primary,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = handleClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontFamily = Manrope,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.sp,
                textAlign = TextAlign.Center,
                color = HrTheme.colorScheme.primary
            )
        )
    }
}

@Preview
@Composable
private fun SecondaryButtonPreview() {
    HrTheme {
        SecondaryButton(
            label = "Secondary",
            onClick = {}
        )
    }
}