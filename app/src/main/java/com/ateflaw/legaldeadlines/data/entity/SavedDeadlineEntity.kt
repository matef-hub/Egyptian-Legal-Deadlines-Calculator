package com.ateflaw.legaldeadlines.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ateflaw.legaldeadlines.domain.model.DurationUnit
import com.ateflaw.legaldeadlines.domain.model.SavedDeadline
import java.time.LocalDate

@Entity(tableName = "saved_deadlines")
data class SavedDeadlineEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "case_number")
    val caseNumber: String,

    @ColumnInfo(name = "client_name")
    val clientName: String,

    @ColumnInfo(name = "action_name")
    val actionName: String,

    @ColumnInfo(name = "announcement_date")
    val announcementDate: String, // ISO-8601: YYYY-MM-DD

    @ColumnInfo(name = "duration")
    val duration: Long,

    @ColumnInfo(name = "unit")
    val unit: String,

    @ColumnInfo(name = "distance_days")
    val distanceDays: Int,

    @ColumnInfo(name = "final_deadline")
    val finalDeadline: String, // ISO-8601: YYYY-MM-DD

    @ColumnInfo(name = "law_article")
    val lawArticle: String,

    @ColumnInfo(name = "calculation_explanation")
    val calculationExplanation: String,

    @ColumnInfo(name = "notes")
    val notes: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): SavedDeadline = SavedDeadline(
        id = id,
        caseNumber = caseNumber,
        clientName = clientName,
        actionName = actionName,
        announcementDate = LocalDate.parse(announcementDate),
        duration = duration,
        unit = try { DurationUnit.valueOf(unit) } catch (_: Exception) { DurationUnit.DAYS },
        distanceDays = distanceDays,
        finalDeadline = LocalDate.parse(finalDeadline),
        lawArticle = lawArticle,
        calculationExplanation = calculationExplanation,
        notes = notes,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(domain: SavedDeadline): SavedDeadlineEntity = SavedDeadlineEntity(
            id = domain.id,
            caseNumber = domain.caseNumber,
            clientName = domain.clientName,
            actionName = domain.actionName,
            announcementDate = domain.announcementDate.toString(),
            duration = domain.duration,
            unit = domain.unit.name,
            distanceDays = domain.distanceDays,
            finalDeadline = domain.finalDeadline.toString(),
            lawArticle = domain.lawArticle,
            calculationExplanation = domain.calculationExplanation,
            notes = domain.notes,
            createdAt = domain.createdAt
        )
    }
}
