package com.ateflaw.legaldeadlines.data.service

import android.content.Context
import android.util.AtomicFile
import com.ateflaw.legaldeadlines.data.dao.HolidayDao
import com.ateflaw.legaldeadlines.domain.model.Holiday
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.time.LocalDate
import java.util.concurrent.TimeUnit

class OfficialHolidayService(
    private val context: Context,
    private val holidayDao: HolidayDao
) {
    private val bundled by lazy {
        context.assets.open(HolidayFeed.FILE_NAME).use { HolidayFeed.parse(HolidayFeed.read(it)) }
    }
    private val cache = AtomicFile(File(context.noBackupFilesDir, HolidayFeed.FILE_NAME))
    private val snapshot = MutableStateFlow<HolidayFeed?>(null)
    private val loadLock = Mutex()
    private val syncLock = Mutex()
    private val client = OkHttpClient.Builder()
        .callTimeout(20, TimeUnit.SECONDS)
        .followRedirects(false)
        .build()

    fun getAllHolidays(): Flow<List<Holiday>> = flow {
        current()
        emitAll(combine(snapshot.filterNotNull(), holidayDao.getAll()) { feed, legacy ->
            feed.includingLegacy(legacy.map { it.toDomain() })
        })
    }

    suspend fun getAllHolidaysList(): List<Holiday> =
        current().includingLegacy(holidayDao.getAllList().map { it.toDomain() })

    suspend fun getHoliday(date: LocalDate): Holiday? = current().holidayOn(date)

    private suspend fun current(): HolidayFeed = withContext(Dispatchers.IO) {
        snapshot.value ?: loadLock.withLock {
            snapshot.value ?: loadSnapshot().also { snapshot.value = it }
        }
    }

    private fun loadSnapshot(): HolidayFeed {
        val baseline = bundled
        val previous = try {
            cache.openRead().use { HolidayFeed.parse(HolidayFeed.read(it)) }
        } catch (_: Exception) {
            return baseline
        }
        return if (previous.acceptsUpdate(baseline)) previous else baseline
    }

    suspend fun syncHolidays(): Result<Int> = withContext(Dispatchers.IO) {
        syncLock.withLock {
            try {
                val before = current()
                val bytes = download()
                val incoming = HolidayFeed.parse(bytes)
                currentCoroutineContext().ensureActive()
                if (incoming.revision > before.revision) {
                    require(incoming.acceptsUpdate(before)) { "Calendar revision would remove dates without a decision" }
                    val output = cache.startWrite()
                    try {
                        output.write(bytes)
                        cache.finishWrite(output)
                    } catch (failure: Exception) {
                        cache.failWrite(output)
                        throw failure
                    }
                    snapshot.value = incoming
                } else if (incoming.revision == before.revision) {
                    require(incoming == before) { "Calendar content changed without a new revision" }
                }
                Result.success(current().holidays.size)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                Result.failure(failure)
            }
        }
    }

    private fun download(): ByteArray {
        val request = Request.Builder().url(FEED_URL).build()
        return client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Calendar request returned HTTP ${response.code}")
            response.body.byteStream().use(HolidayFeed::read)
        }
    }

    companion object {
        private const val FEED_URL =
            "https://raw.githubusercontent.com/matef-hub/Egyptian-Legal-Deadlines-Calculator/main/calendar/egypt-holidays.json"
    }
}
