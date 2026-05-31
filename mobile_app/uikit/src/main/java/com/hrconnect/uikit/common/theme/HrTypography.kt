package com.hrconnect.uikit.common.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.hrconnect.uikit.R

/**
 * Шрифтовое семейство Manrope с поддерживаемыми начертаниями.
 *
 * Ответственность:
 * - Предоставление доступа к кастомным шрифтам (regular, medium, semibold, bold, extrabold).
 *
 * Дата создания: 31-05-2026
 * Автор: Команда №2
 */
val Manrope = FontFamily(
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold),
    Font(R.font.manrope_extrabold, FontWeight.ExtraBold)
)

/**
 * Типографическая схема приложения.
 *
 * Ответственность:
 * - Хранение стилей текста для различных UI-элементов.
 * - Не содержит логики, только данные.
 *
 * Дата создания: 31-05-2026
 * Автор: Команда №2
 *
 * @property screenHeader стиль для заголовков экранов (24sp, Bold)
 * @property subheader стиль для подзаголовков (18sp, SemiBold)
 * @property bodyMedium стиль основного текста (16sp, Normal)
 * @property bodySmall стиль мелкого текста (14sp, Normal)
 * @property fieldLabel стиль для лейблов полей ввода (14sp, SemiBold)
 */
@Immutable
data class HrTypography(
    val screenHeader: TextStyle,
    val subheader: TextStyle,
    val bodyMedium: TextStyle,
    val bodySmall: TextStyle,
    val fieldLabel: TextStyle,
    val h1: TextStyle,
    val h2: TextStyle,
    val h3: TextStyle,
    val h4: TextStyle,
    val body: TextStyle,
    val caption: TextStyle,
    val monospace: TextStyle,
)

/**
 * Базовая типографика (тема по умолчанию).
 *
 * Дата создания: 31-05-2026
 * Автор: Команда №2
 */
val Typography = HrTypography(
    screenHeader = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.48).sp
    ),
    subheader = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.18).sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    bodySmall = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    fieldLabel = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    h1 = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    h2 = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    h3 = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    h4 = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    body = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    caption = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    monospace = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
)

/**
 * CompositionLocal для доступа к типографике без явной передачи.
 *
 * Дата создания: 31-05-2026
 * Автор: Команда №2
 */
val LocalHrTypography = staticCompositionLocalOf { Typography }