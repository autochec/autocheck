package com.hrconnect.uikit.presentation.components.file_upload

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hrconnect.uikit.common.theme.HrTheme
import com.hrconnect.uikit.common.theme.Manrope

/**
 * Состояние загрузки файла.
 */
sealed class FileUploadState {
    object Idle : FileUploadState()
    data class Selected(val fileName: String, val fileSizeBytes: Long) : FileUploadState()
    data class Error(val message: String) : FileUploadState()
}

/**
 * Результат валидации файла.
 */
data class FileValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null,
)

/**
 * Компонент загрузки файла с drag-and-drop зоной и кнопкой выбора.
 *
 * Ответственность:
 * - Отображение зоны перетаскивания файлов.
 * - Кнопка для выбора файла через системный диалог.
 * - Валидация файла: тип (.zip) и размер (до 50 МБ по умолчанию).
 * - Отображение имени файла и ошибок.
 * - Логирование действий пользователя и ошибок.
 *
 * Дата создания: 31-05-2026
 * Автор: Команда №2
 *
 * @param onFileSelected колбэк при успешной валидации файла (возвращает FileUploadResult)
 * @param allowedExtensions список разрешённых расширений (по умолчанию ["zip"])
 * @param maxFileSizeBytes максимальный размер файла в байтах (по умолчанию 50 * 1024 * 1024)
 * @param modifier внешний модификатор
 * @param buttonText текст на кнопке выбора файла
 * @param dragAndDropText текст в зоне перетаскивания
 */
@Composable
fun FileUpload(
    onFileSelected: (FileUploadResult) -> Unit,
    allowedExtensions: List<String> = listOf("zip"),
    maxFileSizeBytes: Long = 50 * 1024 * 1024,
    modifier: Modifier = Modifier,
    buttonText: String = "Выбрать файл",
    dragAndDropText: String = "Перетащите файл сюда или",
) {
    var state: FileUploadState by remember { mutableStateOf(FileUploadState.Idle) }
    var isDraggingOver by remember { mutableStateOf(false) }

    // Обработка выбора файла (заглушка – в реальном проекте используйте rememberLauncherForActivityResult)
    // Для демонстрации используем фиктивный выбор файла через кнопку.
    // В боевом проекте здесь будет вызов системного файлового пикера.
    val selectFile = {
        // Эмуляция выбора файла (в реальности вызывается пикер)
        // Для теста генерируем случайный файл .zip или .txt для проверки валидации
        // В реальном проекте сюда придёт реальный файл из пикера.
        // Мы для примера создадим фиктивный файл .zip с валидным размером.
        val testFileName = "submission.zip"
        val testFileSize = 30 * 1024 * 1024L // 30 МБ

        validateAndHandleFile(
            fileName = testFileName,
            fileSize = testFileSize,
            allowedExtensions = allowedExtensions,
            maxFileSizeBytes = maxFileSizeBytes,
            onFileSelected = onFileSelected,
            onStateUpdate = { newState -> state = newState }
        )
    }

    // Логирование отображения ошибки
    LaunchedEffect(state) {
        if (state is FileUploadState.Error) {
            Log.e("FileUpload", "Отображена ошибка: ${(state as FileUploadState.Error).message}")
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Зона drag-and-drop (визуальная)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (isDraggingOver) HrTheme.colorScheme.primary.copy(alpha = 0.1f)
                    else HrTheme.colorScheme.background
                )
                .border(
                    width = if (isDraggingOver) 2.dp else 1.dp,
                    color = if (isDraggingOver) HrTheme.colorScheme.primary
                    else HrTheme.colorScheme.border,
                    shape = RoundedCornerShape(12.dp)
                )
                // В реальном проекте здесь добавляется Modifier.dragAndDropTarget
                // Для демонстрации просто показываем обработчик клика для эмуляции перетаскивания
                .clickable {
                    // Эмуляция перетаскивания – открываем выбор файла
                    selectFile()
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = dragAndDropText,
                    style = TextStyle(
                        fontFamily = Manrope,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = HrTheme.colorScheme.description
                    ),
                    textAlign = TextAlign.Center
                )
                TextButton(onClick = selectFile) {
                    Text(buttonText)
                }
            }
        }

        // Отображение выбранного файла или ошибки
        when (state) {
            is FileUploadState.Selected -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            HrTheme.colorScheme.primary.copy(alpha = 0.1f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = (state as FileUploadState.Selected).fileName,
                        style = HrTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = formatFileSize((state as FileUploadState.Selected).fileSizeBytes),
                        style = HrTheme.typography.bodySmall,
                        color = HrTheme.colorScheme.secondary
                    )
                }
            }
            is FileUploadState.Error -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            HrTheme.colorScheme.error.copy(alpha = 0.1f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = (state as FileUploadState.Error).message,
                        style = HrTheme.typography.bodySmall,
                        color = HrTheme.colorScheme.error
                    )
                }
            }
            FileUploadState.Idle -> {
                // Ничего не отображаем
            }
        }
    }
}

/**
 * Валидация файла: расширение и размер.
 */
fun validateFile(
    fileName: String,
    fileSizeBytes: Long,
    allowedExtensions: List<String>,
    maxFileSizeBytes: Long,
): FileValidationResult {
    // Проверка расширения
    val extension = fileName.substringAfterLast('.', "").lowercase()
    if (extension !in allowedExtensions) {
        return FileValidationResult(
            isValid = false,
            errorMessage = "Недопустимый тип файла. Разрешены: ${allowedExtensions.joinToString(", ")}"
        )
    }
    // Проверка размера
    if (fileSizeBytes > maxFileSizeBytes) {
        val maxMB = maxFileSizeBytes / (1024 * 1024)
        return FileValidationResult(
            isValid = false,
            errorMessage = "Файл слишком большой. Максимальный размер: $maxMB МБ"
        )
    }
    return FileValidationResult(isValid = true)
}

/**
 * Форматирование размера файла в человекочитаемый вид.
 */
fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes Б"
        bytes < 1024 * 1024 -> "${bytes / 1024} КБ"
        else -> "${bytes / (1024 * 1024)} МБ"
    }
}

// Валидация и обновление состояния
fun validateAndHandleFile(
    fileName: String,
    fileSize: Long,
    allowedExtensions: List<String>,
    maxFileSizeBytes: Long,
    onFileSelected: (FileUploadResult) -> Unit,
    onStateUpdate: (FileUploadState) -> Unit,
) {
    val validationResult = validateFile(fileName, fileSize, allowedExtensions, maxFileSizeBytes)

    if (validationResult.isValid) {
        Log.i(
            "FileUpload",
            "Файл выбран и прошёл валидацию — имя=\"$fileName\", размер=$fileSize байт"
        )
        onStateUpdate(FileUploadState.Selected(fileName, fileSize))
        onFileSelected(FileUploadResult(fileName, fileSize))
    } else {
        val errorMsg = validationResult.errorMessage ?: "Неизвестная ошибка"
        Log.e("FileUpload", "Ошибка валидации файла — $errorMsg")
        onStateUpdate(FileUploadState.Error(errorMsg))
    }
}

/**
 * Результат успешной загрузки файла.
 */
data class FileUploadResult(
    val fileName: String,
    val fileSizeBytes: Long,
)

@Preview(showBackground = true)
@Composable
private fun FileUploadPreview() {
    HrTheme {
        FileUpload(
            onFileSelected = { result ->
                println("File selected: ${result.fileName}")
            },
            modifier = Modifier.padding(16.dp)
        )
    }
}