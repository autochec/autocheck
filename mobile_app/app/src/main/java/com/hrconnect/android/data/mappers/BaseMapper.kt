package com.autocheckmobile.data.mapper

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Утилиты для маппинга.
 * Дата создания: 26-05-2026
 * Автор: Team01
 */
object BaseMapper {

    private const val DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    private val dateFormat = SimpleDateFormat(DATE_PATTERN, Locale.US)

    /**
     * Преобразует строку в Date (UTC).
     * Если строка null или непарсится, возвращает текущую дату.
     */
    fun parseDate(dateStr: String?): Date {
        return try {
            if (dateStr.isNullOrEmpty()) Date()
            else dateFormat.parse(dateStr) ?: Date()
        } catch (e: Exception) {
            Date()
        }
    }

    /**
     * Безопасное преобразование Map в значение по ключу.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getFromMap(map: Map<String, Any>, key: String, default: T): T {
        return (map[key] as? T) ?: default
    }
}