package com.ateflaw.legaldeadlines

import android.app.Application
import com.ateflaw.legaldeadlines.data.worker.HolidaySyncWorker
import com.ateflaw.legaldeadlines.di.AppContainer

class LegalDeadlinesApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)

        // Schedule periodic background holiday synchronization
        HolidaySyncWorker.schedule(this)
    }
}
