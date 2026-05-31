package com.hrconnect.android.presentation.features.submit

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hrconnect.android.R
import com.hrconnect.android.common.util.ObserveAsEvents
import com.hrconnect.uikit.common.theme.HrTheme
import com.hrconnect.uikit.common.theme.Manrope
import com.hrconnect.uikit.presentation.components.buttons.PrimaryButton
import com.hrconnect.uikit.presentation.components.select.Select
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.viewmodel.koinViewModel

/**
 * Экран «Отправить задание».
 *
 * Ответственность:
 * - Выпадающий список тестовых заданий (Select).
 * - Кнопка «Выбрать ZIP-файл» через стандартный системный файловый пикер.
 * - Индикатор загрузки во время отправки файла на сервер.
 * - После успешной отправки — переход на «Мои задания».
 *
 * Дата создания: 31-05-2026
 * Автор: Команда №2
 *
 * @param onNavigateBack колбэк для возврата назад
 * @param onSubmitSuccess колбэк после успешной отправки задания
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitAssignmentScreen(
    onNavigateBack: () -> Unit,
    onSubmitSuccess: () -> Unit,
    viewModel: SubmitAssignmentViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Обработка одноразовых событий навигации
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is SubmitAssignmentUiEvent.NavigateToMySubmissions -> onSubmitSuccess()
            is SubmitAssignmentUiEvent.ShowError -> { /* ошибка отображается в состоянии */
            }
        }
    }

    // Стандартный Android файловый пикер (application/zip)
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch(Dispatchers.IO) {
                try {
                    // Читаем имя файла из URI через ContentResolver
                    val fileName = resolveFileName(context, uri) ?: "submission.zip"
                    val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
                    if (bytes != null) {
                        withContext(Dispatchers.Main) {
                            viewModel.onFileSelected(fileName, bytes)
                        }
                    }
                } catch (e: Exception) {
                    // Ошибка чтения — игнорируется, ViewModel не обновляется
                }
            }
        }
    }

    Scaffold(
        containerColor = HrTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Отправить задание",
                        style = TextStyle(
                            fontFamily = Manrope,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp,
                            color = HrTheme.colorScheme.topBarTitle
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_arrow_back),
                            contentDescription = "Назад",
                            tint = HrTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HrTheme.colorScheme.container
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isUploading) {
            // Полноэкранный индикатор загрузки во время отправки
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(color = HrTheme.colorScheme.primary)
                    Text(
                        text = "Загружаем файл на сервер...",
                        style = TextStyle(
                            fontFamily = Manrope,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            color = HrTheme.colorScheme.description
                        )
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Выпадающий список тестовых заданий
                if (uiState.isLoadingAssignments) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = HrTheme.colorScheme.primary
                    )
                } else {
                    Select(
                        label = "Тестовое задание",
                        items = uiState.assignments.map { it.title },
                        selectedItem = uiState.selectedAssignment?.title,
                        onItemClick = { title ->
                            val assignment = uiState.assignments.find { it.title == title }
                            if (assignment != null) viewModel.onAssignmentSelected(assignment)
                        }
                    )
                }

                // Метка секции загрузки ZIP
                Text(
                    text = "ZIP-архив с решением",
                    style = TextStyle(
                        fontFamily = Manrope,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = HrTheme.colorScheme.fieldLabel
                    )
                )

                // Кнопка открытия системного файлового пикера
                PrimaryButton(
                    label = if (uiState.selectedFileName != null) "Выбрать другой файл"
                    else "Выбрать ZIP-файл",
                    onClick = { filePicker.launch("application/zip") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Имя выбранного файла
                if (uiState.selectedFileName != null) {
                    Text(
                        text = "Выбран: ${uiState.selectedFileName}",
                        style = TextStyle(
                            fontFamily = Manrope,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp,
                            color = HrTheme.colorScheme.description
                        )
                    )
                }

                // Сообщение об ошибке
                if (uiState.error != null) {
                    Text(
                        text = uiState.error!!,
                        style = TextStyle(
                            fontFamily = Manrope,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp,
                            color = HrTheme.colorScheme.error
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Кнопка «Отправить на проверку» — неактивна до выбора задания и файла
                PrimaryButton(
                    label = "Отправить на проверку",
                    onClick = { viewModel.submit() },
                    enabled = uiState.canSubmit,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * Извлекает отображаемое имя файла из URI через ContentResolver.
 *
 * @param context контекст приложения
 * @param uri URI выбранного файла
 * @return имя файла или null если определить не удалось
 */
private fun resolveFileName(context: android.content.Context, uri: Uri): String? {
    return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
        cursor.moveToFirst()
        if (nameIndex >= 0) cursor.getString(nameIndex) else null
    }
}
