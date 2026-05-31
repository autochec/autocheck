package com.hrconnect.android.data.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * OpenRouterClient — клиент для работы с OpenRouter API.
 * Совместим с любым OpenAI-совместимым провайдером.
 *
 * @param apiKey   Bearer-токен (openrouter.ai/keys)
 * @param baseUrl  Базовый URL API
 * @param model    Идентификатор модели
 */
class OpenRouterClient(
    private val apiKey: String,
    private val baseUrl: String = "https://openrouter.ai/api/v1",
    private val model: String = "openai/gpt-4o-mini",
) {
    private val http = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json".toMediaType()

    /**
     * Отправляет сообщение и получает ответ от модели.
     *
     * @param userMessage  Сообщение пользователя
     * @param systemPrompt Системный промпт (опционально)
     * @return Текст ответа модели
     */
    suspend fun chat(userMessage: String, systemPrompt: String? = null): String =
        withContext(Dispatchers.IO) {
            val messages = JSONArray()

            if (systemPrompt != null) {
                messages.put(JSONObject().apply {
                    put("role", "system")
                    put("content", systemPrompt)
                })
            }

            messages.put(JSONObject().apply {
                put("role", "user")
                put("content", userMessage)
            })

            val requestBody = JSONObject().apply {
                put("model", model)
                put("messages", messages)
                put("max_tokens", 1500)
                put("temperature", 0.3)
            }

            val request = Request.Builder()
                .url("$baseUrl/chat/completions")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer $apiKey")
                .header("HTTP-Referer", "https://autocheckmobile.app")
                .post(requestBody.toString().toRequestBody(jsonMediaType))
                .build()

            val response = http.newCall(request).execute()
            val body = response.body?.string()
                ?: throw RuntimeException("Пустой ответ от API")

            if (!response.isSuccessful) {
                throw RuntimeException("API ошибка ${response.code}: $body")
            }

            JSONObject(body)
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
        }

    /**
     * Анализирует результаты проверки и возвращает структурированный ревью.
     *
     * @param checkResults  Результаты чекеров (текст)
     * @param candidateName Имя кандидата
     * @param finalScore    Итоговый балл (0..100 или null)
     * @return Текст ревью
     */
    suspend fun reviewSubmission(
        checkResults: String,
        candidateName: String,
        finalScore: Float?,
    ): String {
        val systemPrompt = """
            Ты — опытный ревьюер кода. Проанализируй результаты автоматической проверки задания и дай заключение.
            Ответь строго в следующем формате:

            ## ✅ Что реализовано хорошо
            - пункт 1
            - пункт 2

            ## 🔧 Что требует улучшения
            - пункт 1
            - пункт 2

            ## 📌 Итог
            Краткий вывод в 2–3 предложениях.
        """.trimIndent()

        val userMessage = buildString {
            appendLine("Кандидат: $candidateName")
            appendLine("Итоговый балл: ${finalScore?.toInt() ?: "не определён"}/100")
            appendLine()
            appendLine("Результаты проверок:")
            appendLine(checkResults)
        }

        return chat(userMessage, systemPrompt)
    }
}
