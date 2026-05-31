package com.hrconnect.netlib.di

import com.hrconnect.netlib.common.util.Constants
import com.hrconnect.netlib.data.manager.TokenManagerImpl
import com.hrconnect.netlib.data.remote.AssignmentsApi
import com.hrconnect.netlib.data.remote.AuthApi
import com.hrconnect.netlib.data.remote.AuthInterceptor
import com.hrconnect.netlib.data.remote.CandidatesApi
import com.hrconnect.netlib.data.remote.ReportsApi
import com.hrconnect.netlib.data.remote.SubmissionsApi
import com.hrconnect.netlib.domain.manager.TokenManager
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * DI-модуль для тестирования библиотеки NetLib
 */
val netLibModule = module {
    singleOf(::TokenManagerImpl).bind<TokenManager>()
    singleOf(::AuthInterceptor)
    single {
        OkHttpClient.Builder()
            .addInterceptor(get<AuthInterceptor>())
            .build()
    }
    single {
        Json {
            ignoreUnknownKeys = true
        }
    }
    single {
        val contentType = "application/json".toMediaType()

        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(get<Json>().asConverterFactory(contentType))
            .client(get())
            .build()
    }
    single {
        get<Retrofit>().create(AuthApi::class.java)
    }
    single {
        get<Retrofit>().create(AssignmentsApi::class.java)
    }
    single {
        get<Retrofit>().create(SubmissionsApi::class.java)
    }
    single {
        get<Retrofit>().create(CandidatesApi::class.java)
    }
    single {
        get<Retrofit>().create(ReportsApi::class.java)
    }
}