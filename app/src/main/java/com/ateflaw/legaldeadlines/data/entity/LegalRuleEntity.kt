package com.ateflaw.legaldeadlines.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ateflaw.legaldeadlines.domain.model.DurationUnit
import com.ateflaw.legaldeadlines.domain.model.LegalRule
import com.ateflaw.legaldeadlines.domain.model.StartRule

@Entity(tableName = "legal_rules")
data class LegalRuleEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "action_name")
    val actionName: String,

    @ColumnInfo(name = "duration")
    val duration: Long,

    @ColumnInfo(name = "unit")
    val unit: String,

    @ColumnInfo(name = "distance_days")
    val distanceDays: Int,

    @ColumnInfo(name = "start_rule")
    val startRule: String,

    @ColumnInfo(name = "law_article")
    val lawArticle: String,

    @ColumnInfo(name = "notes")
    val notes: String,

    @ColumnInfo(name = "summary", defaultValue = "")
    val summary: String = ""
) {
    fun toDomain(): LegalRule = LegalRule(
        id = id,
        actionName = actionName,
        duration = duration,
        unit = try { DurationUnit.valueOf(unit) } catch (_: Exception) { DurationUnit.DAYS },
        distanceDays = distanceDays,
        startRule = try { StartRule.valueOf(startRule) } catch (_: Exception) { StartRule.NEXT_DAY },
        lawArticle = lawArticle,
        notes = notes,
        summary = summary
    )

    companion object {
        fun fromDomain(domain: LegalRule): LegalRuleEntity = LegalRuleEntity(
            id = domain.id,
            actionName = domain.actionName,
            duration = domain.duration,
            unit = domain.unit.name,
            distanceDays = domain.distanceDays,
            startRule = domain.startRule.name,
            lawArticle = domain.lawArticle,
            notes = domain.notes,
            summary = domain.summary
        )
    }
}
