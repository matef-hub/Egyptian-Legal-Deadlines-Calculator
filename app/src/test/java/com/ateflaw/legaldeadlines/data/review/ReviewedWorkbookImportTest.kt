package com.ateflaw.legaldeadlines.data.review

import com.ateflaw.legaldeadlines.data.dao.ADOPT_REVIEWED_WORKBOOK_RULE_SQL
import com.ateflaw.legaldeadlines.data.dao.BACKFILL_REVIEWED_WORKBOOK_TEXT_SQL
import com.ateflaw.legaldeadlines.data.seed.LegalRuleSeedData
import com.ateflaw.legaldeadlines.domain.model.LegalRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.sql.Connection
import java.sql.DriverManager

/** Executes the production import queries against SQLite, including installed v1 rows. */
class ReviewedWorkbookImportTest {
    private val seeds = LegalRuleSeedData.getSeedRules().associateBy { it.id }
    private val correctedIds = listOf(11L, 16L, 74L, 131L, 132L, 159L, 160L, 161L)

    private fun record(id: Long) = ReviewedWorkbookRecord(
        id = id,
        decision = if (id == 160L) "VERIFIED_ACCURATE" else "NEEDS_CORRECTION",
        method = "أمامي",
        sourceUrl = "https://www.alamiria.com/",
        effectiveFrom = "2026-01-01",
        reviewerNote = "ملاحظة المراجع"
    )

    private fun database(): Connection = DriverManager.getConnection("jdbc:sqlite::memory:").apply {
        createStatement().use {
            it.execute("""
                CREATE TABLE legal_rules (
                    id INTEGER PRIMARY KEY, action_name TEXT NOT NULL, law_article TEXT NOT NULL,
                    duration INTEGER NOT NULL, unit TEXT NOT NULL, notes TEXT NOT NULL,
                    summary TEXT NOT NULL, revision INTEGER NOT NULL, review_version INTEGER NOT NULL,
                    workbook_decision TEXT NOT NULL, review_status TEXT NOT NULL,
                    review_method TEXT NOT NULL, reviewer_note TEXT NOT NULL,
                    official_source_url TEXT NOT NULL, effective_from TEXT NOT NULL,
                    effective_to TEXT, calculation_kind TEXT NOT NULL, last_reviewed_at TEXT NOT NULL,
                    article_text TEXT NOT NULL DEFAULT ''
                )
            """.trimIndent())
            it.execute("CREATE TABLE saved_deadlines (id INTEGER PRIMARY KEY, notes TEXT, rule_snapshot TEXT)")
            it.execute("INSERT INTO saved_deadlines VALUES (1, 'ملاحظات محفوظة', 'حساب تاريخي أصلي')")
        }
    }

    private fun Connection.execute(sql: String, values: Map<String, Any?>): Int {
        val parameters = Regex(":([A-Za-z][A-Za-z0-9_]*)")
        val names = parameters.findAll(sql).map { it.groupValues[1] }.toList()
        return prepareStatement(parameters.replace(sql, "?")).use { statement ->
            names.forEachIndexed { index, name ->
                check(values.containsKey(name)) { "Missing SQL binding: $name" }
                statement.setObject(index + 1, values[name])
            }
            statement.executeUpdate()
        }
    }

    private fun Connection.insertSeed(seed: LegalRule) {
        execute("""
            INSERT INTO legal_rules VALUES (
                :id, :action, :article, :duration, :unit, :notes, :summary,
                1, 0, '', 'NEEDS_REVIEW', '', '', '', '', NULL, :kind, '', ''
            )
        """.trimIndent(), mapOf(
            "id" to seed.id, "action" to seed.actionName, "article" to seed.lawArticle,
            "duration" to seed.duration, "unit" to seed.unit.name, "notes" to seed.notes,
            "summary" to seed.summary, "kind" to seed.calculationKind
        ))
    }

    private fun Connection.importVersionOne(seed: LegalRule, item: ReviewedWorkbookRecord): Int =
        execute(ADOPT_REVIEWED_WORKBOOK_RULE_SQL, mapOf(
            "id" to item.id, "actionName" to seed.actionName,
            "decision" to item.decision, "method" to item.method,
            "reviewerNote" to item.reviewerNote, "source" to item.sourceUrl,
            "effectiveFrom" to item.effectiveFrom, "effectiveTo" to null,
            "kind" to item.normalizedKind, "duration" to item.correctedDuration,
            "unit" to item.correctedUnit, "correctedActionName" to item.correctedActionName,
            "correctedLawArticle" to item.correctedLawArticle
        ))

    private fun Connection.repairText(seed: LegalRule, item: ReviewedWorkbookRecord): Int =
        execute(BACKFILL_REVIEWED_WORKBOOK_TEXT_SQL, mapOf(
            "id" to item.id, "decision" to item.decision,
            "actionName" to (item.correctedActionName ?: seed.actionName),
            "lawArticle" to (item.correctedLawArticle ?: seed.lawArticle),
            "duration" to (item.correctedDuration ?: seed.duration),
            "unit" to (item.correctedUnit ?: seed.unit.name),
            "seedNotes" to seed.notes, "seedSummary" to seed.summary,
            "correctedNotes" to item.correctedNotes, "correctedSummary" to item.correctedSummary
        ))

    private fun Connection.row(id: Long): List<Any?> =
        prepareStatement("SELECT * FROM legal_rules WHERE id = ?").use { statement ->
            statement.setLong(1, id)
            statement.executeQuery().use { result ->
                check(result.next())
                (1..result.metaData.columnCount).map { result.getObject(it) }
            }
        }

    private fun Connection.assertCorrected(seed: LegalRule, item: ReviewedWorkbookRecord) {
        prepareStatement("""
            SELECT duration, unit, notes, summary, revision, review_version, law_article
            FROM legal_rules WHERE id = ?
        """.trimIndent()).use { statement ->
            statement.setLong(1, item.id)
            statement.executeQuery().use {
                assertTrue(it.next())
                assertEquals(item.correctedDuration ?: seed.duration, it.getLong(1))
                assertEquals(item.correctedUnit ?: seed.unit.name, it.getString(2))
                assertEquals(item.correctedNotes, it.getString(3))
                assertEquals(item.correctedSummary, it.getString(4))
                assertEquals(3, it.getInt(5))
                assertEquals(2, it.getInt(6))
                assertEquals(item.correctedLawArticle ?: seed.lawArticle, it.getString(7))
            }
        }
    }

    @Test fun freshImportsKeepCorrectedNumbersAndReadableTextTogether() {
        database().use { db ->
            correctedIds.forEach { id ->
                val seed = seeds.getValue(id)
                val item = record(id)
                db.insertSeed(seed)
                assertEquals(1, db.importVersionOne(seed, item))
                assertEquals(1, db.repairText(seed, item))
                db.assertCorrected(seed, item)
            }
        }
    }

    @Test fun installedVersionOneRowsAreRepairedOnceWithoutRewritingSavedDeadlines() {
        database().use { db ->
            correctedIds.forEach { id ->
                val seed = seeds.getValue(id)
                val item = record(id)
                db.insertSeed(seed)
                db.importVersionOne(seed, item)
                assertEquals(1, db.repairText(seed, item))
                val corrected = db.row(id)
                assertEquals(0, db.importVersionOne(seed, item))
                assertEquals(0, db.repairText(seed, item))
                assertEquals(corrected, db.row(id))
                db.assertCorrected(seed, item)
            }
            db.createStatement().use { statement ->
                statement.executeQuery("SELECT notes, rule_snapshot FROM saved_deadlines WHERE id = 1").use {
                    assertTrue(it.next())
                    assertEquals("ملاحظات محفوظة", it.getString(1))
                    assertEquals("حساب تاريخي أصلي", it.getString(2))
                }
            }
        }
    }

    @Test fun userEditsAndUntrackedHistoricalAnnotationsArePreserved() {
        listOf(
            "revision = 3, workbook_decision = 'USER_APPROVED', notes = 'تعديل المستخدم'",
            "notes = 'تعليق تاريخي للمستخدم'",
            "summary = 'ملخص مخصص'",
            "duration = 99",
            "law_article = 'سند معدل'",
            "action_name = 'إجراء مخصص'",
            "review_status = 'NEEDS_REVIEW'"
        ).forEach { override ->
            database().use { db ->
                val seed = seeds.getValue(161L)
                val item = record(seed.id)
                db.insertSeed(seed)
                db.importVersionOne(seed, item)
                db.createStatement().use { it.executeUpdate("UPDATE legal_rules SET $override WHERE id = 161") }
                val before = db.row(seed.id)
                assertEquals(0, db.repairText(seed, item))
                assertEquals(before, db.row(seed.id))
            }
        }
    }
}
