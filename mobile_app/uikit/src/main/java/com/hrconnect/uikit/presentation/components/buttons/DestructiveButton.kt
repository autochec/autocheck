package com.hrconnect.uikit.presentation.components.buttons

import android.util.Log
import androidx.compose.foundation.background
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
 * Деструктивная кнопка (например, "Удалить", "Выйти").
 *
 * Ответственность:
 * - Отображение кнопки с красным фоном (цвет error) и белым текстом.
 * - Обработка клика с логированием критического действия.
 * - Полная ширина с закруглёнными углами.
 *
 * Дата создания: 31-05-2026
 * Автор: Команда №2
 *
 * @param label текст на кнопке
 * @param onClick колбэк при нажатии
 * @param modifier внешний модификатор
 */
@Composable
fun DestructiveButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Логирование клика по деструктивной кнопке (INFO) — критическое пользовательское действие
    val handleClick = {
        Log.i("DestructiveButton", "Пользователь нажал на деструктивную кнопку — label=\"$label\"")
        onClick()
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(HrTheme.colorScheme.error)
            .clickable(onClick = handleClick)
            .padding(vertical = 14.dp),
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
                color = HrTheme.colorScheme.onError
            )
        )
    }
}

@Preview
@Composable
private fun DestructiveButtonPreview() {
    HrTheme {
        DestructiveButton(
            label = "Destructive Solid",
            onClick = {}
        )
    }
}