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
    val summary: String = "",

    @ColumnInfo(name = "trigger_event_type", defaultValue = "ANNOUNCEMENT")
    val triggerEventType: String = "ANNOUNCEMENT",

    @ColumnInfo(name = "trigger_event_label", defaultValue = "تاريخ الإعلان أو صدور الحكم")
    val triggerEventLabel: String = "تاريخ الإعلان أو صدور الحكم",

    @ColumnInfo(name = "court_type", defaultValue = "مدني وتجاري")
    val courtType: String = "مدني وتجاري",

    @ColumnInfo(name = "category", defaultValue = "طرق الطعن")
    val category: String = "طرق الطعن",

    @ColumnInfo(name = "distance_policy", defaultValue = "APPLICABLE")
    val distancePolicy: String = "APPLICABLE",

    @ColumnInfo(name = "extension_policy", defaultValue = "ARTICLE_18_COURTS")
    val extensionPolicy: String = "ARTICLE_18_COURTS",

    @ColumnInfo(name = "law_name", defaultValue = "قانون المرافعات المدنية والتجارية")
    val lawName: String = "قانون المرافعات المدنية والتجارية",

    @ColumnInfo(name = "article", defaultValue = "")
    val article: String = "",

    @ColumnInfo(name = "official_source_url", defaultValue = "")
    val officialSourceUrl: String = "",

    @ColumnInfo(name = "effective_from", defaultValue = "1968-11-01")
    val effectiveFrom: String = "1968-11-01",

    @ColumnInfo(name = "effective_to")
    val effectiveTo: String? = null,

    @ColumnInfo(name = "last_reviewed_at", defaultValue = "2026-10-13")
    val lastReviewedAt: String = "2026-10-13",

    @ColumnInfo(name = "review_status", defaultValue = "NEEDS_REVIEW")
    val reviewStatus: String = "NEEDS_REVIEW"
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
        summary = summary,
        triggerEventType = triggerEventType,
        triggerEventLabel = triggerEventLabel,
        courtType = courtType,
        category = category,
        distancePolicy = distancePolicy,
        extensionPolicy = extensionPolicy,
        lawName = lawName,
        article = article,
        officialSourceUrl = officialSourceUrl,
        effectiveFrom = effectiveFrom,
        effectiveTo = effectiveTo,
        lastReviewedAt = lastReviewedAt,
        reviewStatus = reviewStatus
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
            summary = domain.summary,
            triggerEventType = domain.triggerEventType,
            triggerEventLabel = domain.triggerEventLabel,
            courtType = domain.courtType,
            category = domain.category,
            distancePolicy = domain.distancePolicy,
            extensionPolicy = domain.extensionPolicy,
            lawName = domain.lawName,
            article = domain.article,
            officialSourceUrl = domain.officialSourceUrl,
            effectiveFrom = domain.effectiveFrom,
            effectiveTo = domain.effectiveTo,
            lastReviewedAt = domain.lastReviewedAt,
            reviewStatus = domain.reviewStatus
        )
    }
}
