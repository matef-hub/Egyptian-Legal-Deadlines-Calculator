package com.ateflaw.legaldeadlines.data.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.ateflaw.legaldeadlines.data.database.AppDatabase
import com.ateflaw.legaldeadlines.data.remote.RetrofitClient
import com.ateflaw.legaldeadlines.data.service.OfficialHolidayService
import java.time.LocalDate
import java.util.concurrent.TimeUnit

/**
 * WorkManager CoroutineWorker for periodic and non-blocking background synchronization
 * of official Egyptian holidays from Calendarific API to local Room database.
 */
class HolidaySyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getInstance(applicationContext)
        val holidayService = OfficialHolidayService(
            holidayDao = database.holidayDao(),
            apiService = RetrofitClient.apiService
        )

        val currentYear = LocalDate.now().year

        // Sync for current year and next year
        val syncCurrent = holidayService.syncHolidays(currentYear)
        val syncNext = holidayService.syncHolidays(currentYear + 1)

        return if (syncCurrent.isSuccess || syncNext.isSuccess) {
            Result.success()
        } else {
            // Transient failure: retry with exponential backoff without crashing the app
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "EgyptianHolidaySyncWork"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            // Synchronize once every 7 days (efficient and avoids unnecessary API calls)
            val syncRequest = PeriodicWorkRequestBuilder<HolidaySyncWorker>(7, TimeUnit.DAYS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
        }
    }
}
