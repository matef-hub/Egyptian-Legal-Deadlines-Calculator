package com.ateflaw.legaldeadlines.data.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.ateflaw.legaldeadlines.LegalDeadlinesApplication
import java.util.concurrent.TimeUnit

class HolidaySyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val application = applicationContext as LegalDeadlinesApplication
        val synced = application.container.syncHolidaysUseCase()
        return if (synced.isSuccess || runAttemptCount >= 2) Result.success() else Result.retry()
    }

    companion object {
        private const val WORK_NAME = "EgyptianHolidaySyncWork"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val request = PeriodicWorkRequestBuilder<HolidaySyncWorker>(1, TimeUnit.DAYS)
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }
    }
}
