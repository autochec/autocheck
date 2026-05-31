package com.hrconnect.android.domain.llm

import kotlinx.coroutines.flow.Flow

/**
 * Интерфейс локального AI-анализатора отчётов на основе on-device LLM.
 */
interface LocalReportAnalyzer {

    /** True если модель загружена и инициализирована. */
    val isModelReady: Boolean

    /**
     * Запускает анализ по готовому промпту.
     * @return Flow<String> — поток токенов (стриминг)
     */
    suspend fun analyze(prompt: String): Flow<String>

    /** Освобождает ресурсы модели. */
    fun release()
}
