package com.ateflaw.legaldeadlines.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Immutable snapshot written before each user revision of a legal rule. */
@Entity(tableName = "rule_edit_history", indices = [Index(value = ["rule_id"])])
data class RuleEditHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "rule_id") val ruleId: Long,
    @ColumnInfo(name = "previous_revision") val previousRevision: Int,
    @ColumnInfo(name = "action_name") val actionName: String,
    @ColumnInfo(name = "law_name") val lawName: String,
    @ColumnInfo(name = "law_article") val lawArticle: String,
    @ColumnInfo(name = "article_text") val articleText: String,
    @ColumnInfo(name = "duration") val duration: Long,
    @ColumnInfo(name = "unit") val unit: String,
    @ColumnInfo(name = "notes") val notes: String,
    @ColumnInfo(name = "review_status") val reviewStatus: String,
    @ColumnInfo(name = "official_source_url") val officialSourceUrl: String,
    @ColumnInfo(name = "edited_at") val editedAt: Long
)
