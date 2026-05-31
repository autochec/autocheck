package com.hrconnect.uikit.common.theme

import android.util.Log
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

/**
 * Корневой компонент темы приложения.
 *
 * Ответственность:
 * - Предоставление цветовой схемы (HrColorScheme) и типографики (HrTypography) через CompositionLocal.
 * - Интеграция с MaterialTheme для совместимости с Material компонентами.
 * - Логирование инициализации темы (жизненный цикл компонента).
 *
 * Дата создания: 31-05-2026
 * Автор: Команда №2
 *
 * @param content дочерний UI, обёрнутый в тему
 */
@Composable
fun HrTheme(
    content: @Composable () -> Unit,
) {
    // Логирование инициализации темы (INFO) — жизненный цикл компонента
    Log.i("HrTheme", "Тема приложения инициализирована — цветовая схема=LightColorScheme")

    CompositionLocalProvider(
        LocalHrColorScheme provides LightColorScheme,
        LocalHrTypography provides Typography
    ) {
        MaterialTheme(
            colorScheme = mapMaterialColorScheme(
                hrColorScheme = LightColorScheme
            ),
            content = content
        )
    }
}

/**
 * Объект-провайдер для доступа к теме из любого места Compose.
 *
 * Ответственность:
 * - Предоставление цветовой схемы и типографики через композиционный контекст.
 *
 * Дата создания: 31-05-2026
 * Автор: Команда №2
 */
object HrTheme {

    val colorScheme: HrColorScheme
        @Composable
        get() = LocalHrColorScheme.current

    val typography: HrTypography
        @Composable
        get() = LocalHrTypography.current
}

/**
 * Преобразование кастомной цветовой схемы в Material ColorScheme.
 *
 * @param hrColorScheme кастомная цветовая схема HrColorScheme
 * @return ColorScheme для MaterialTheme
 */
private fun mapMaterialColorScheme(
    hrColorScheme: HrColorScheme,
): ColorScheme {
    return lightColorScheme(
        primary = hrColorScheme.primary,
        onPrimary = hrColorScheme.onPrimary,
        secondary = hrColorScheme.secondary,
        tertiary = hrColorScheme.tertiary,
        error = hrColorScheme.error,
        background = hrColorScheme.background,
        onBackground = hrColorScheme.onBackground,
        surfaceContainer = hrColorScheme.container
    )
}