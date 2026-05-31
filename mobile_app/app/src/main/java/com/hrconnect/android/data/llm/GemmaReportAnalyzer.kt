package com.hrconnect.android.data.llm

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.hrconnect.android.domain.llm.LocalReportAnalyzer
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import logcat.LogPriority.ERROR
import logcat.LogPriority.INFO
import logcat.logcat
import java.io.File

/**
 * Реализация LocalReportAnalyzer на основе Gemma 3 270M через MediaPipe Tasks GenAI.
 *
 * Модель: gemma3-270m-it-int4.task (~170 МБ, 4-bit квантизация)
 * Путь: context.filesDir/models/gemma3-270m-it-int4.task
 *
 * Жизненный цикл:
 * - Вызовите [initIfReady] после того как модель скачана — инициализирует LlmInference.
 * - Вызовите [release] в onDestroy / onCleared ViewModel.
 */
class GemmaReportAnalyzer(private val context: Context) : LocalReportAnalyzer {

    companion object {
        const val MODEL_FILENAME = "gemma3-270m-it-int4.task"
        private const val MAX_TOKENS = 1024
        private const val TAG = "GemmaReportAnalyzer"
    }

    private var llmInference: LlmInference? = null

    val modelFile: File
        get() = File(context.filesDir, "models/$MODEL_FILENAME")

    override val isModelReady: Boolean
        get() = llmInference != null

    /**
     * Инициализирует LlmInference если файл модели существует.
     * Безопасно вызывать повторно — повторная инициализация не происходит.
     *
     * @return true если модель успешно инициализирована
     */
    fun initIfReady(): Boolean {
        if (llmInference != null) return true
        if (!modelFile.exists()) {
            logcat(TAG, ERROR) { "Файл модели не найден: ${modelFile.absolutePath}" }
            return false
        }
        return try {
            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelFile.absolutePath)
                .setMaxTokens(MAX_TOKENS)
                .build()
            llmInference = LlmInference.createFromOptions(context, options)
            logcat(TAG, INFO) { "Gemma 3 270M инициализирована" }
            true
        } catch (e: Exception) {
            logcat(TAG, ERROR) { "Ошибка инициализации модели: ${e.message}" }
            false
        }
    }

    /**
     * Запускает генерацию и возвращает Flow с частичными токенами.
     * Flow завершается когда модель заканчивает генерацию.
     */
    override suspend fun analyze(prompt: String): Flow<String> = callbackFlow {
        val inference = checkNotNull(llmInference) { "Модель не инициализирована" }
        logcat(TAG, INFO) { "Запуск генерации (${prompt.length} символов в промпте)" }

        inference.generateResponseAsync(prompt) { partialResult, done ->
            if (!partialResult.isNullOrEmpty()) {
                trySend(partialResult)
            }
            if (done) {
                logcat(TAG, INFO) { "Генерация завершена" }
                close()
            }
        }

        awaitClose()
    }

    override fun release() {
        llmInference?.close()
        llmInference = null
        logcat(TAG, INFO) { "Ресурсы Gemma освобождены" }
    }
}
