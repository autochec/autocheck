package com.hrconnect.android

import android.app.Application
import com.hrconnect.android.di.appModule
import com.hrconnect.netlib.di.netLibModule
import logcat.AndroidLogcatLogger
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class HrConnectApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@HrConnectApp)
            modules(netLibModule, appModule)
        }
        AndroidLogcatLogger.installOnDebuggableApp(this)
    }
}