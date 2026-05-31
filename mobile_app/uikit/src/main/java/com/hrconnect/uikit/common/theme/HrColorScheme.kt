package com.hrconnect.uikit.common.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Цветовая схема приложения (набор цветовых токенов).
 *
 * Ответственность:
 * - Хранение всех цветовых констант, используемых в UI.
 * - Не содержит логики, только данные.
 *
 * Дата создания: 31-05-2026
 * Автор: Команда №2
 *
 * @property primary основной цвет бренда
 * @property primaryVariant вариант основного цвета
 * @property primaryContainer контейнер для primary
 * @property onPrimaryContainer цвет текста/иконок на primaryContainer
 * @property onPrimary цвет текста/иконок на primary
 * @property secondary второстепенный цвет
 * @property tertiary третичный цвет
 * @property error цвет ошибки
 * @property onError цвет текста/иконок на ошибке
 * @property background цвет фона экранов
 * @property onBackground цвет текста/иконок на фоне
 * @property onBackgroundVariant вариант цвета текста на фоне
 * @property container цвет карточек/контейнеров
 * @property inputContainerDisabled цвет фона отключённого поля ввода
 * @property checkboxContainerDisabled цвет фона отключённого чекбокса
 * @property border цвет границ
 * @property containerBorder цвет границ контейнеров
 * @property bottomBarBorder цвет границы нижней панели
 * @property bottomBarContent цвет содержимого нижней панели (неактивное)
 * @property fieldLabel цвет лейбла поля
 * @property placeholder цвет плейсхолдера
 * @property description цвет вспомогательного текста
 * @property divider цвет разделителя
 * @property indicatorTrack цвет трека индикатора (неактивный)
 * @property topBarTitle цвет заголовка верхней панели
 */
@Immutable
data class HrColorScheme(
    val primary: Color,
    val primaryVariant: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val onPrimary: Color,
    val secondary: Color,
    val tertiary: Color,
    val error: Color,
    val onError: Color,
    val background: Color,
    val onBackground: Color,
    val onBackgroundVariant: Color,
    val container: Color,
    val inputContainerDisabled: Color,
    val checkboxContainerDisabled: Color,
    val border: Color,
    val containerBorder: Color,
    val bottomBarBorder: Color,
    val bottomBarContent: Color,
    val fieldLabel: Color,
    val placeholder: Color,
    val description: Color,
    val divider: Color,
    val indicatorTrack: Color,
    val topBarTitle: Color,
    val success: Color,
    val warning: Color,
    val neutral: Color,
)

/**
 * Светлая тема цветовой схемы.
 *
 * Дата создания: 31-05-2026
 * Автор: Команда №2
 */
val LightColorScheme = HrColorScheme(
    primary = Color(0xFF004AC6),
    primaryVariant = Color(0xFF2563EB),
    primaryContainer = Color(0xFFD0E1FB),
    onPrimaryContainer = Color(0xFF54647A),
    onPrimary = Color.White,
    secondary = Color(0xFF505F76),
    tertiary = Color(0xFF943700),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    background = Color(0xFFFAF8FF),
    onBackground = Color(0xFF191B23),
    onBackgroundVariant = Color(0xFF0B1C30),
    container = Color.White,
    inputContainerDisabled = Color(0xFFF3F3FE),
    checkboxContainerDisabled = Color(0xFFEDEDF9),
    border = Color(0xFFC3C6D7),
    containerBorder = Color(0xFFEDEDF9),
    bottomBarBorder = Color(0xFFF1F5F9),
    bottomBarContent = Color(0xFF94A3B8),
    fieldLabel = Color(0xFF434655),
    placeholder = Color(0xFF6B7280),
    description = Color(0xFF737686),
    divider = Color(0xFFF8FAFC),
    indicatorTrack = Color(0xFFE1E2ED),
    topBarTitle = Color(0xFF0F172A),
    success = Color(0xFF10B981),  // зелёный
    warning = Color(0xFFF59E0B),  // оранжевый/жёлтый
    neutral = Color(0xFF64748B)   // серый
)

/**
 * CompositionLocal для доступа к цветовой схеме без явной передачи.
 *
 * Дата создания: 31-05-2026
 * Автор: Команда №2
 */
val LocalHrColorScheme = staticCompositionLocalOf {
    LightColorScheme
}