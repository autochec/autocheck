package com.hrconnect.android.di

import com.hrconnect.android.BuildConfig
import com.hrconnect.android.data.ai.OpenRouterClient
import com.hrconnect.android.data.repository.AssignmentsRepositoryImpl
import com.hrconnect.android.data.repository.AuthRepositoryImpl
import com.hrconnect.android.data.repository.SubmissionsRepositoryImpl
import com.hrconnect.android.domain.repository.AssignmentsRepository
import com.hrconnect.android.domain.repository.AuthRepository
import com.hrconnect.android.domain.repository.SubmissionsRepository
import com.hrconnect.android.presentation.features.login.LoginViewModel
import com.hrconnect.android.presentation.features.results.SubmissionResultsViewModel
import com.hrconnect.android.presentation.features.submit.SubmitAssignmentViewModel
import com.hrconnect.android.presentation.features.submissions.MySubmissionsViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    // Репозитории
    singleOf(::AuthRepositoryImpl).bind<AuthRepository>()
    singleOf(::SubmissionsRepositoryImpl).bind<SubmissionsRepository>()
    singleOf(::AssignmentsRepositoryImpl).bind<AssignmentsRepository>()

    // OpenRouter AI
    single { OpenRouterClient(apiKey = BuildConfig.OPENROUTER_API_KEY) }

    // ViewModels
    viewModelOf(::LoginViewModel)
    viewModelOf(::MySubmissionsViewModel)
    viewModelOf(::SubmitAssignmentViewModel)
    viewModelOf(::SubmissionResultsViewModel)
}
