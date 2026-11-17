package com.ateflaw.legaldeadlines.data

import android.net.Uri
import org.json.JSONObject
import com.ateflaw.legaldeadlines.data.backup.EncryptedBackupCodec
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ateflaw.legaldeadlines.data.backup.BackupTransferService
import com.ateflaw.legaldeadlines.data.backup.BackupTransferService.RestoreMode
import com.ateflaw.legaldeadlines.data.database.AppDatabase
import com.ateflaw.legaldeadlines.data.entity.SavedDeadlineEntity
import com.ateflaw.legaldeadlines.domain.model.DurationUnit
import com.ateflaw.legaldeadlines.domain.model.SavedDeadline
import java.io.File
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BackupAndReminderTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun encryptedMergeAndReplacePreserveDeadlinesAndWorkAfterIdCollisions() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        val backup = File.createTempFile("deadlines-", ".meead", context.cacheDir)
        val password = "instrumentation-backup".toCharArray()
        try {
            val uri = Uri.fromFile(backup)
            val service = BackupTransferService(db, context)
            val dao = db.savedDeadlineDao()
            val due = LocalDate.of(2027, 1, 20)
            val row = SavedDeadlineEntity.fromDomain(SavedDeadline(
                id = 1, actionName = "ميعاد أ", announcementDate = due.minusDays(20),
                duration = 20, unit = DurationUnit.DAYS, distanceDays = 0,
                finalDeadline = due, lawArticle = "مادة", calculationExplanation = "حساب",
                proactiveReminderDays = 3
            ))
            dao.insert(row)
            service.exportTo(uri, password)
            dao.insert(row.copy(actionName = "ميعاد ب", proactiveReminderDays = null))
            assertEquals(1, service.importFrom(uri, password, RestoreMode.MERGE_DEADLINES).savedDeadlines)
            assertEquals(0, service.importFrom(uri, password, RestoreMode.MERGE_DEADLINES).savedDeadlines)
            assertEquals(2, dao.observeByDeadline().first().size)
            assertTrue(dao.dueReminders("2027-01-16").isEmpty())
            val reminder = dao.dueReminders("2027-01-17").single()
            dao.markReminderSent(reminder.id, reminder.finalDeadline, 3)
            assertTrue(dao.dueReminders("2027-01-18").isEmpty())
            assertTrue(runCatching {
                service.importFrom(uri, "wrong-password-for-test".toCharArray(), RestoreMode.REPLACE_ALL)
            }.isFailure)
            assertEquals(2, dao.observeByDeadline().first().size)
            // A wrong password fails before writing. A duplicate ID fails after the
            // transaction has started replacing rows and must roll those changes back.
            val original = backup.readBytes()
            val document = JSONObject(EncryptedBackupCodec.decrypt(original, password).toString(Charsets.UTF_8))
            val rows = document.getJSONObject("tables").getJSONArray("saved_deadlines")
            rows.put(rows.getJSONObject(0))
            backup.writeBytes(EncryptedBackupCodec.encrypt(document.toString().toByteArray(), password))
            assertTrue(runCatching { service.importFrom(uri, password, RestoreMode.REPLACE_ALL) }.isFailure)
            assertEquals(2, dao.observeByDeadline().first().size)
            assertTrue(dao.dueReminders("2027-01-18").isEmpty())
            backup.writeBytes(original)
            service.importFrom(uri, password, RestoreMode.REPLACE_ALL)
            assertEquals("ميعاد أ", dao.observeByDeadline().first().single().actionName)
            assertEquals(1, dao.dueReminders("2027-01-17").size)
            assertTrue(dao.dueReminders("2027-01-21").isEmpty())
        } finally {
            db.close()
            backup.delete()
            password.fill('•')
        }
    }
}
