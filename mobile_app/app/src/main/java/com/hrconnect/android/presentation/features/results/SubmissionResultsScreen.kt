package com.hrconnect.android.presentation.features.results

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.hrconnect.uikit.presentation.components.cards.ResultRow
import com.hrconnect.uikit.presentation.components.score_card.ScoreCard
import org.koin.compose.viewmodel.koinViewModel

/**
 * Экран «Результаты проверки».
 *
 * Ответственность:
 * - Отображение ScoreCard с итоговым баллом и статусом.
 * - Список ResultRow с результатами каждого чекера (раскрываемые детали).
 * - Секция AI-анализа или плашка «AI-анализ недоступен».
 * - Индикатор загрузки и обработка ошибок.
 *
 * Дата создания: 31-05-2026
 * Автор: Команда №2
 *
 * @param onNavigateBack колбэк для возврата назад
 */
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
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = HrTheme.colorScheme.primary)
                }
            }

            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
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
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }
    }
}

/**
 * Основной контент экрана результатов.
 *
 * @param submission данные проверки
 * @param checkResults список результатов чекеров
 * @param aiReview текст AI-анализа (null = недоступен)
 * @param modifier модификатор
 */
@Composable
private fun ResultsContent(
    submission: Submission,
    checkResults: List<CheckResult>,
    aiReview: String?,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ScoreCard с итоговым баллом
        item {
            ScoreCard(
                score = submission.finalScore?.toInt() ?: 0,
                assignmentTitle = submission.assignmentId,
                candidateName = submission.candidateName,
                status = submission.status.toStatusType()
            )
        }

        // Секция «Детализация проверок»
        if (checkResults.isNotEmpty()) {
            item {
                SectionTitle("Детализация проверок")
            }

            items(
                items = checkResults,
                key = { it.checker }
            ) { result ->
                ResultRow(
                    checkName = result.checker,
                    status = result.status.toStatusType(),
                    score = result.score.toInt(),
                    details = result.details.ifBlank { result.message }
                )
                HorizontalDivider(
                    color = HrTheme.colorScheme.divider,
                    thickness = 1.dp
                )
            }
        }

        // Секция AI-анализа
        item {
            SectionTitle("AI-анализ")
            Spacer(modifier = Modifier.height(8.dp))
            AiReviewSection(aiReview = aiReview)
        }
    }
}

/** Заголовок секции. */
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

/**
 * Секция AI-анализа.
 * При недоступности AI показывает плашку.
 *
 * @param aiReview текст AI-анализа (null = недоступен)
 */
@Composable
private fun AiReviewSection(aiReview: String?) {
    if (aiReview.isNullOrBlank()) {
        // Плашка «AI-анализ недоступен»
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(HrTheme.colorScheme.neutral.copy(alpha = 0.1f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "AI-анализ недоступен",
                style = TextStyle(
                    fontFamily = Manrope,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = HrTheme.colorScheme.description
                )
            )
        }
    } else {
        // Текст AI-анализа
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(HrTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                .padding(16.dp)
        ) {
            Text(
                text = aiReview,
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
}

/** Преобразует SubmissionStatus в StatusType для UIKit. */
private fun SubmissionStatus.toStatusType(): StatusType = when (this) {
    SubmissionStatus.PENDING -> StatusType.PENDING
    SubmissionStatus.RUNNING -> StatusType.RUNNING
    SubmissionStatus.DONE -> StatusType.PASSED
    SubmissionStatus.ERROR -> StatusType.ERROR
}

/** Преобразует CheckStatus в StatusType для UIKit. */
private fun CheckStatus.toStatusType(): StatusType = when (this) {
    CheckStatus.PASSED -> StatusType.PASSED
    CheckStatus.FAILED -> StatusType.FAILED
    CheckStatus.ERROR -> StatusType.ERROR
}
