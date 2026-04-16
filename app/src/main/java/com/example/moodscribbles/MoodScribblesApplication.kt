package com.example.moodscribbles

import android.app.Application
import com.example.moodscribbles.core.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MoodScribblesApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MoodScribblesApplication)
            modules(appModule)
        }
    }
}
