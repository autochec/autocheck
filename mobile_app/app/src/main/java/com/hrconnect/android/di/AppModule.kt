package com.hrconnect.android.di

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

/**
 * DI-модуль приложения.
 *
 * Ответственность:
 * - Регистрация репозиториев и ViewModel для внедрения зависимостей через Koin.
 * - Связывает интерфейсы (Repository) с реализациями (RepositoryImpl).
 *
 * Дата создания: 31-05-2026
 * Автор: Команда №2
 */
val appModule = module {
    // Репозитории
    singleOf(::AuthRepositoryImpl).bind<AuthRepository>()
    singleOf(::SubmissionsRepositoryImpl).bind<SubmissionsRepository>()
    singleOf(::AssignmentsRepositoryImpl).bind<AssignmentsRepository>()

    // ViewModels
    viewModelOf(::LoginViewModel)
    viewModelOf(::MySubmissionsViewModel)
    viewModelOf(::SubmitAssignmentViewModel)
    viewModelOf(::SubmissionResultsViewModel)
}
