package com.hrconnect.android.domain.llm

import com.hrconnect.android.domain.model.CheckResult
import com.hrconnect.android.domain.model.Submission

/**
 * Строит промпт для Gemma 3 в формате Gemma Instruct (ChatML).
 */
object ReportPromptBuilder {

    fun build(submission: Submission, checkResults: List<CheckResult>): String {
        val resultsText = checkResults.joinToString("\n") { r ->
            val msg = if (r.message.isNotBlank()) " — ${r.message}" else ""
            "• ${r.checker}: ${r.score.toInt()}/100 (${r.status.name.lowercase()})$msg"
        }
        val score = submission.finalScore?.toInt()?.toString() ?: "не определён"

        return buildString {
            append("<start_of_turn>user\n")
            append("Ты — опытный ревьюер кода. ")
            append("Проанализируй результаты автоматической проверки тестового задания и дай краткое заключение.\n\n")
            append("Кандидат: ${submission.candidateName}\n")
            append("Итоговый балл: $score/100\n")
            if (checkResults.isNotEmpty()) {
                append("\nРезультаты проверок:\n$resultsText\n")
            }
            append("\nДай краткое заключение (3–5 предложений): ")
            append("общее качество работы, сильные стороны и основные проблемы.")
            append("<end_of_turn>\n")
            append("<start_of_turn>model\n")
        }
    }
}
