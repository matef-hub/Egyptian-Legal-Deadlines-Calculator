package com.ateflaw.legaldeadlines.data.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.ateflaw.legaldeadlines.R
import com.ateflaw.legaldeadlines.data.database.AppDatabase
import com.ateflaw.legaldeadlines.presentation.MainActivity
import com.ateflaw.legaldeadlines.utils.DateFormatterUtils
import java.time.LocalDate
import java.util.concurrent.TimeUnit

class DeadlineReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val context = applicationContext
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context,
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return Result.success()
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return Result.success()
        val dao = AppDatabase.getInstance(context).savedDeadlineDao()
        val due = dao.dueReminders(LocalDate.now().toString())
        if (due.isEmpty()) return Result.success()
        if (Build.VERSION.SDK_INT >= 26) {
            manager.createNotificationChannel(NotificationChannel(CHANNEL, "تذكيرات المواعيد",
                NotificationManager.IMPORTANCE_DEFAULT))
            if (manager.getNotificationChannel(CHANNEL)?.importance == NotificationManager.IMPORTANCE_NONE) return Result.success()
        }
        val open = PendingIntent.getActivity(context, 0,
            Intent(context, MainActivity::class.java).setAction(OPEN_SAVED),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        for (record in due) {
            val days = record.proactiveReminderDays ?: continue
            val date = DateFormatterUtils.formatArabicFullDate(LocalDate.parse(record.finalDeadline))
            val notification = NotificationCompat.Builder(context, CHANNEL)
                .setSmallIcon(R.drawable.ic_calendar_event)
                .setContentTitle("ميعاد محفوظ يحتاج مراجعة")
                .setContentText("تاريخ الميعاد المحسوب $date")
                .setContentIntent(open)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setOnlyAlertOnce(true)
                .setAutoCancel(true)
                .build()
            manager.notify("deadline-${record.id}", 1, notification)
            dao.markReminderSent(record.id, record.finalDeadline, days)
        }
        return Result.success()
    }

    companion object {
        private const val CHANNEL = "saved_deadline_reminders"
        const val OPEN_SAVED = "com.ateflaw.legaldeadlines.OPEN_SAVED"
        fun schedule(context: Context) {
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(CHANNEL, ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<DeadlineReminderWorker>(1, TimeUnit.HOURS).build())
        }
    }
}
