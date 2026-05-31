package com.hrconnect.uikit.presentation.components.avatar

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hrconnect.uikit.common.theme.HrTheme
import com.hrconnect.uikit.common.theme.Manrope

/**
 * Компонент аватара (круглая иконка пользователя).
 *
 * Ответственность:
 * - Отображение текстовой инициалы (displayText) по умолчанию.
 * - Отображение изображения (image), если передано, поверх текста.
 * - Визуальная индикация: рамка primaryVariant при наличии изображения, иначе border.
 * - Обработка клика с логированием (только если передан onClick).
 *
 * Дата создания: 31-05-2026
 * Автор: Команда №2
 *
 * @param displayText текст (обычно инициалы), отображаемый, когда нет изображения
 * @param modifier внешний модификатор (размер по умолчанию 60.dp)
 * @param image опциональное изображение (Painter) – если передано, отображается поверх текста
 * @param onClick опциональный колбэк клика (если null, аватар не кликабелен)
 */
@Composable
fun Avatar(
    displayText: String,
    modifier: Modifier = Modifier,
    image: Painter? = null,
    onClick: (() -> Unit)? = null,
) {
    // Логирование клика по аватару (INFO) — критическое пользовательское действие
    val handleClick = {
        if (onClick != null) {
            Log.i(
                "Avatar",
                "Пользователь нажал на аватар — displayText=\"$displayText\", hasImage=${image != null}"
            )
            onClick()
        }
    }

    Box(
        modifier = modifier
            .size(60.dp)
            .clip(CircleShape)
            .border(
                width = 2.dp,
                color = if (image != null) {
                    HrTheme.colorScheme.primaryVariant
                } else {
                    HrTheme.colorScheme.border
                },
                shape = CircleShape
            )
            .clickable(
                enabled = onClick != null,
                onClick = handleClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Инициалы (отображаются под изображением, если изображение есть)
        Text(
            text = displayText,
            style = TextStyle(
                fontFamily = Manrope,
                fontWeight = FontWeight.Bold,
                color = HrTheme.colorScheme.onBackgroundVariant
            )
        )
        // Изображение поверх текста (если передано)
        if (image != null) {
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = image,
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
        }
    }
}