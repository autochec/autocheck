package com.hrconnect.android.presentation.features.results

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hrconnect.android.R
import com.hrconnect.android.domain.model.CheckResult
import com.hrconnect.android.domain.model.CheckStatus
import com.hrconnect.android.domain.model.Submission
import com.hrconnect.android.domain.model.SubmissionStatus
import com.hrconnect.uikit.common.theme.HrTheme
import com.hrconnect.uikit.common.theme.Manrope
import com.hrconnect.uikit.presentation.components.badge.StatusType
import com.hrconnect.uikit.presentation.components.buttons.PrimaryButton
import com.hrconnect.uikit.presentation.components.buttons.SecondaryButton
import com.hrconnect.uikit.presentation.components.cards.ResultRow
import com.hrconnect.uikit.presentation.components.score_card.ScoreCard
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmissionResultsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SubmissionResultsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = HrTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Результаты проверки",
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
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = HrTheme.colorScheme.primary)
                }
            }

            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.error!!,
                        style = TextStyle(
                            fontFamily = Manrope,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            color = HrTheme.colorScheme.error
                        ),
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }

            uiState.submission != null -> {
                ResultsContent(
                    submission = uiState.submission!!,
                    checkResults = uiState.checkResults,
                    aiReview = uiState.aiReview,
                    localAiState = uiState.localAiState,
                    onAnalyzeLocally = viewModel::requestLocalAnalysis,
                    onDownloadModel = viewModel::startModelDownload,
                    modifier = Modifier.fillMaxSize().padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun ResultsContent(
    submission: Submission,
    checkResults: List<CheckResult>,
    aiReview: String?,
    localAiState: LocalAiState,
    onAnalyzeLocally: () -> Unit,
    onDownloadModel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ScoreCard(
                score = submission.finalScore?.toInt() ?: 0,
                assignmentTitle = submission.assignmentId,
                candidateName = submission.candidateName,
                status = submission.status.toStatusType()
            )
        }

        if (checkResults.isNotEmpty()) {
            item { SectionTitle("Детализация проверок") }
            items(items = checkResults, key = { it.checker }) { result ->
                ResultRow(
                    checkName = result.checker,
                    status = result.status.toStatusType(),
                    score = result.score.toInt(),
                    details = result.details.ifBlank { result.message }
                )
                HorizontalDivider(color = HrTheme.colorScheme.divider, thickness = 1.dp)
            }
        }

        // Серверный AI-анализ
        if (!aiReview.isNullOrBlank()) {
            item {
                SectionTitle("AI-анализ (сервер)")
                Spacer(Modifier.height(8.dp))
                AiTextBox(text = aiReview)
            }
        }

        // Локальный AI-анализ через Gemma 3 270M
        item {
            SectionTitle("Локальный AI-анализ (Gemma 3 270M)")
            Spacer(Modifier.height(8.dp))
            LocalAiSection(
                state = localAiState,
                onAnalyzeLocally = onAnalyzeLocally,
                onDownloadModel = onDownloadModel
            )
        }
    }
}

/**
 * Секция локального AI-анализа — адаптируется под состояние [LocalAiState].
 */
@Composable
private fun LocalAiSection(
    state: LocalAiState,
    onAnalyzeLocally: () -> Unit,
    onDownloadModel: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        when (state) {
            is LocalAiState.Idle -> {
                SecondaryButton(
                    text = "Анализировать локально",
                    onClick = onAnalyzeLocally,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            is LocalAiState.ModelNotDownloaded -> {
                InfoBox(
                    text = "Модель Gemma 3 270M не загружена (~170 МБ).",
                    isError = false
                )
                PrimaryButton(
                    text = "Скачать модель",
                    onClick = onDownloadModel,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            is LocalAiState.Downloading -> {
                InfoBox(text = "Загрузка модели: ${state.progress}%", isError = false)
                LinearProgressIndicator(
                    progress = { state.progress / 100f },
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)),
                    color = HrTheme.colorScheme.primary,
                    trackColor = HrTheme.colorScheme.border
                )
            }

            is LocalAiState.Initializing -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = HrTheme.colorScheme.primary,
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = "Инициализация Gemma 3 270M…",
                        style = TextStyle(
                            fontFamily = Manrope,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp,
                            color = HrTheme.colorScheme.description
                        )
                    )
                }
            }

            is LocalAiState.Generating -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = HrTheme.colorScheme.primary,
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = "Gemma 3 генерирует…",
                        style = TextStyle(
                            fontFamily = Manrope,
                            fontSize = 12.sp,
                            color = HrTheme.colorScheme.description
                        )
                    )
                }
                AnimatedVisibility(visible = state.text.isNotBlank()) {
                    AiTextBox(text = state.text)
                }
            }

            is LocalAiState.Done -> {
                AiTextBox(text = state.text)
                SecondaryButton(
                    text = "Повторить анализ",
                    onClick = onAnalyzeLocally,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            is LocalAiState.Error -> {
                InfoBox(text = state.message, isError = true)
                SecondaryButton(
                    text = "Попробовать снова",
                    onClick = onAnalyzeLocally,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun AiTextBox(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(HrTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            .padding(16.dp)
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontFamily = Manrope,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = HrTheme.colorScheme.onBackground
            )
        )
    }
}

@Composable
private fun InfoBox(text: String, isError: Boolean, modifier: Modifier = Modifier) {
    val bg = if (isError)
        HrTheme.colorScheme.error.copy(alpha = 0.08f)
    else
        HrTheme.colorScheme.neutral.copy(alpha = 0.1f)
    val textColor = if (isError) HrTheme.colorScheme.error else HrTheme.colorScheme.description

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(12.dp)
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontFamily = Manrope,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                color = textColor
            )
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = TextStyle(
            fontFamily = Manrope,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            lineHeight = 20.sp,
            color = HrTheme.colorScheme.onBackground
        )
    )
}

private fun SubmissionStatus.toStatusType(): StatusType = when (this) {
    SubmissionStatus.PENDING -> StatusType.PENDING
    SubmissionStatus.RUNNING -> StatusType.RUNNING
    SubmissionStatus.DONE -> StatusType.PASSED
    SubmissionStatus.ERROR -> StatusType.ERROR
}

private fun CheckStatus.toStatusType(): StatusType = when (this) {
    CheckStatus.PASSED -> StatusType.PASSED
    CheckStatus.FAILED -> StatusType.FAILED
    CheckStatus.ERROR -> StatusType.ERROR
}
