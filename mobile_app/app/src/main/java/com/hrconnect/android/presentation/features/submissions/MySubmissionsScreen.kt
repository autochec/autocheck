package com.hrconnect.android.presentation.features.submissions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hrconnect.android.R
import com.hrconnect.android.common.util.ObserveAsEvents
import com.hrconnect.android.domain.model.SubmissionStatus
import com.hrconnect.uikit.common.theme.HrTheme
import com.hrconnect.uikit.common.theme.Manrope
import com.hrconnect.uikit.presentation.components.badge.StatusType
import com.hrconnect.uikit.presentation.components.buttons.PrimaryButton
import com.hrconnect.uikit.presentation.components.submission_card.SubmissionCard
import org.koin.compose.viewmodel.koinViewModel

/**
 * Экран «Мои задания».
 *
 * Ответственность:
 * - Отображение списка проверок кандидата в виде карточек SubmissionCard.
 * - Pull-to-refresh для обновления данных.
 * - Пустое состояние с призывом к действию, если проверок нет.
 * - Переход к экрану результатов при клике на карточку.
 * - Кнопка «Отправить задание» для перехода к форме отправки.
 *
 * Дата создания: 31-05-2026
 * Автор: Ком��нда №2
 *
 * @param onNavigateToSubmit колбэк перехода к экрану отправки задания
 * @param onNavigateToResults колбэк перехода к результатам проверки
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MySubmissionsScreen(
    onNavigateToSubmit: () -> Unit,
    onNavigateToResults: (submissionId: String) -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: MySubmissionsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is MySubmissionsUiEvent.NavigateToLogin -> onNavigateToLogin()
        }
    }

    Scaffold(
        containerColor = HrTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Мои задания",
                        style = TextStyle(
                            fontFamily = Manrope,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp,
                            color = HrTheme.colorScheme.topBarTitle
                        )
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_logout),
                            contentDescription = "Выйти",
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
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.submissions.isEmpty() && !uiState.isLoading) {
                // Пустое состояние — призыв к действию
                EmptySubmissionsContent(
                    error = uiState.error,
                    onSendClick = onNavigateToSubmit,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = uiState.submissions,
                        key = { it.id }
                    ) { submission ->
                        SubmissionCard(
                            submissionDate = submission.submittedAt,
                            assignmentTitle = submission.assignmentId,
                            status = submission.status.toStatusType(),
                            score = submission.finalScore?.toInt(),
                            onClick = { onNavigateToResults(submission.id) }
                        )
                    }

                    // Кнопка «Отправить задание» внизу списка
                    item {
                        PrimaryButton(
                            label = "Отправить задание",
                            onClick = onNavigateToSubmit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Пустое состояние экрана «Мои задания».
 *
 * @param error сообщение ошибки (null = нет ошибки)
 * @param onSendClick колбэк кнопки «Отправить задание»
 * @param modifier модификатор
 */
@Composable
private fun EmptySubmissionsContent(
    error: String?,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Text(
                text = if (error != null) "Не удалось загрузить задания" else "Нет загруженных заданий",
                style = TextStyle(
                    fontFamily = Manrope,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = HrTheme.colorScheme.onBackground
                )
            )
            Text(
                text = if (error != null) error
                else "Загрузите своё первое решение, чтобы начать автоматическую проверку",
                style = TextStyle(
                    fontFamily = Manrope,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = HrTheme.colorScheme.description
                )
            )
            PrimaryButton(
                label = "Отправить задание",
                onClick = onSendClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/** Преобразует SubmissionStatus в StatusType для UIKit. */
private fun SubmissionStatus.toStatusType(): StatusType = when (this) {
    SubmissionStatus.PENDING -> StatusType.PENDING
    SubmissionStatus.RUNNING -> StatusType.RUNNING
    SubmissionStatus.DONE -> StatusType.PASSED
    SubmissionStatus.ERROR -> StatusType.ERROR
}
