package com.ateflaw.legaldeadlines.data.service

import com.ateflaw.legaldeadlines.domain.model.Holiday
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.URI
import java.time.LocalDate

internal data class HolidayFeed(
    val revision: Int,
    val publishedOn: LocalDate,
    val holidays: List<Holiday>,
    val superseded: Set<LocalDate>
) {
    private val byDate = holidays.associateBy(Holiday::holidayDate)

    fun holidayOn(date: LocalDate): Holiday? = byDate[date]

    fun includingLegacy(legacy: List<Holiday>): List<Holiday> =
        (holidays + legacy.filter { it.holidayDate !in byDate && it.holidayDate !in superseded }
            .map { it.copy(verificationStatus = "UNVERIFIED") })
            .sortedBy(Holiday::holidayDate)

    fun acceptsUpdate(previous: HolidayFeed): Boolean =
        revision > previous.revision && publishedOn >= previous.publishedOn &&
            previous.holidays.all { it.holidayDate in byDate || it.holidayDate in superseded } &&
            previous.superseded.all { it in byDate || it in superseded }

    companion object {
        private const val MAX_BYTES = 1024 * 1024
        const val FILE_NAME = "egypt-holidays.json"
        private val officialHosts = setOf(
            "cabinet.gov.eg", "www.cabinet.gov.eg", "alamiria.com", "www.alamiria.com",
            "presidency.eg", "www.presidency.eg", "sis.gov.eg", "www.sis.gov.eg",
            "moj.gov.eg", "www.moj.gov.eg"
        )

        fun read(input: InputStream): ByteArray {
            val output = ByteArrayOutputStream()
            val buffer = ByteArray(8192)
            while (true) {
                val count = input.read(buffer)
                if (count == -1) break
                require(output.size() + count <= MAX_BYTES) { "Calendar exceeds the size limit" }
                output.write(buffer, 0, count)
            }
            return output.toByteArray()
        }

        fun parse(bytes: ByteArray): HolidayFeed {
            require(bytes.size in 1..MAX_BYTES)
            val json = JSONObject(bytes.toString(Charsets.UTF_8))
            require(json.get("schemaVersion") == 1)
            val revision = json.get("revision")
            require(revision is Int && revision > 0)
            val publishedOn = json.date("publishedOn")
            val rows = json.getJSONArray("holidays")
            require(rows.length() > 0)
            val holidays = (0 until rows.length()).map { index ->
                val row = rows.getJSONObject(index)
                Holiday(
                    holidayDate = row.date("date"),
                    name = row.text("name"),
                    verificationStatus = "VERIFIED_OFFICIAL",
                    sourceUrl = row.officialSource(),
                    decision = row.text("decision")
                )
            }.sortedBy(Holiday::holidayDate)
            val dates = holidays.map(Holiday::holidayDate).toSet()
            require(dates.size == holidays.size)

            val removed = json.getJSONArray("superseded")
            val superseded = (0 until removed.length()).map { index ->
                val row = removed.getJSONObject(index)
                row.officialSource()
                row.text("decision")
                if (!row.isNull("replacementDate")) require(row.date("replacementDate") in dates)
                row.date("date")
            }.toSet()
            require(superseded.size == removed.length() && dates.intersect(superseded).isEmpty())
            return HolidayFeed(revision, publishedOn, holidays, superseded)
        }

        private fun JSONObject.text(key: String): String = (get(key) as String).also {
            require(it.isNotBlank() && it.length <= 300)
        }

        private fun JSONObject.date(key: String): LocalDate {
            val value = text(key)
            return LocalDate.parse(value).also {
                require(value.length == 10 && it.toString() == value)
            }
        }

        private fun JSONObject.officialSource(): String = (get("sourceUrl") as String).also { value ->
            require(value.length <= 2048)
            val uri = URI(value)
            require(uri.scheme == "https" && uri.host?.lowercase() in officialHosts &&
                uri.rawUserInfo == null && uri.port == -1)
        }
    }
}
