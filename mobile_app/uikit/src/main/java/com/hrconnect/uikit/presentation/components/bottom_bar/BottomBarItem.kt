package com.hrconnect.uikit.presentation.components.bottom_bar

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Модель данных для элемента нижней навигационной панели.
 *
 * Ответственность:
 * - Хранение состояния и внешнего вида одного пункта меню.
 * - Не содержит логики, только данные.
 *
 * Дата создания: 31-05-2026
 * Автор: Команда №2
 *
 * @param selected выбран ли данный пункт (определяет цветовую стилизацию)
 * @param icon иконка пункта
 * @param label текстовый лейбл пункта
 * @param route идентификатор или объект маршрута для навигации
 */
data class BottomBarItem<T>(
    val selected: Boolean,
    val icon: ImageVector,
    val label: String,
    val route: T,
)