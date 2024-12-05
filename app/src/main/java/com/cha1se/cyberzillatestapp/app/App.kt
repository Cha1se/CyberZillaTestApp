package com.cha1se.cyberzillatestapp.app

import android.app.Application
import com.cha1se.cyberzillatestapp.di.dataModule
import com.cha1se.cyberzillatestapp.di.presentationModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class App: Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(presentationModule, dataModule)
        }
    }

}