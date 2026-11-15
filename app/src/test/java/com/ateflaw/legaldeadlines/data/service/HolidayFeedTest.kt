package com.ateflaw.legaldeadlines.data.service

import com.ateflaw.legaldeadlines.domain.model.Holiday
import org.json.JSONObject
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.time.LocalDate

class HolidayFeedTest {
    private val source = sequenceOf(File("calendar"), File("../calendar"))
        .map { it.resolve(HolidayFeed.FILE_NAME) }
        .first { it.isFile }.readBytes()

    @Test
    fun cabinetDecisionReplacesOctoberSixOnFreshAndUpgradedInstallations() {
        val feed = HolidayFeed.parse(source)
        val oldDate = LocalDate.parse("2026-10-06")
        val newDate = LocalDate.parse("2026-10-08")
        val unrelated = LocalDate.parse("2025-10-06")
        val legacy = listOf(oldDate, unrelated).map {
            Holiday(holidayDate = it, name = "سجل قديم", verificationStatus = "VERIFIED_OFFICIAL",
                sourceUrl = "https://www.presidency.eg/")
        }
        assertNull(feed.holidayOn(oldDate))
        assertTrue(feed.holidayOn(newDate)!!.isVerifiedOfficial)
        assertFalse(feed.includingLegacy(legacy).any { it.holidayDate == oldDate })
        assertFalse(feed.includingLegacy(legacy).single { it.holidayDate == unrelated }.isVerifiedOfficial)
    }

    @Test
    fun newerRevisionPreservesKnownDatesAndItsLastGoodVersion() {
        val previous = HolidayFeed.parse(source)
        val json = JSONObject(source.toString(Charsets.UTF_8))
        assertFalse(previous.acceptsUpdate(previous))
        val current = HolidayFeed.parse(json.put("revision", 2).toString().toByteArray())
        assertTrue(current.acceptsUpdate(previous))
        assertFalse(previous.acceptsUpdate(current))
        json.getJSONArray("holidays").remove(0)
        val incomplete = HolidayFeed.parse(json.toString().toByteArray())
        assertFalse(incomplete.acceptsUpdate(previous))
    }

    @Test
    fun duplicatesAndUnofficialSourcesAreRejected() {
        val json = JSONObject(source.toString(Charsets.UTF_8))
        val dates = json.getJSONArray("holidays")
        val originalSize = dates.length()
        val original = dates.getJSONObject(0)
        dates.put(original)
        assertThrows(IllegalArgumentException::class.java) {
            HolidayFeed.parse(json.toString().toByteArray())
        }
        dates.remove(originalSize)
        original.put("sourceUrl", "https://www.facebook.com/example")
        assertThrows(IllegalArgumentException::class.java) {
            HolidayFeed.parse(json.toString().toByteArray())
        }
    }
}
