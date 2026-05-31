package com.hrconnect.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.hrconnect.android.presentation.navigation.AppNavGraph
import com.hrconnect.android.presentation.navigation.Routes
import com.hrconnect.netlib.domain.manager.TokenManager
import com.hrconnect.uikit.common.theme.HrTheme
import kotlinx.coroutines.flow.first
import logcat.logcat
import org.koin.android.ext.android.inject

/**
 * Главная активность приложения AutoCheckMobile (кандидат).
 *
 * Ответственность:
 * - Определение начального экрана: если токен сохранён — «Мои задания», иначе — «Вход».
 * - Инициализация Compose UI и графа навигации.
 * - Применение темы HrTheme ко всему приложению.
 *
 * Дата создания: 31-05-2026
 * Автор: Команда №2
 */
class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    // TokenManager инжектируется через Koin для проверки сохранённой сессии
    private val tokenManager: TokenManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logcat(TAG) { "[MainActivity]: onCreate" }
        enableEdgeToEdge()
        setContent {
            HrTheme {
                val navController = rememberNavController()

                // Читаем первое значение токена из DataStore (null = загружается)
                // Если токен пустой/отсутствует — стартуем с Login, иначе — с MySubmissions
                val startDestination by produceState<String?>(initialValue = null) {
                    val token = tokenManager.getToken().first()
                    value = if (token.isNullOrBlank()) Routes.LOGIN else Routes.MY_SUBMISSIONS
                    logcat(TAG) {
                        "[MainActivity]: Начальный экран определён — route=${value}, hasToken=${!token.isNullOrBlank()}"
                    }
                }

                if (startDestination == null) {
                    // Пока DataStore читается с диска — показываем индикатор
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    AppNavGraph(
                        navController = navController,
                        startDestination = startDestination!!
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        logcat(TAG) { "[MainActivity]: onStart" }
    }

    override fun onStop() {
        super.onStop()
        logcat(TAG) { "[MainActivity]: onStop" }
    }
}
