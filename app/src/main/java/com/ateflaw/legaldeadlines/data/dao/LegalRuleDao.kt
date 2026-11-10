package com.ateflaw.legaldeadlines.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ateflaw.legaldeadlines.data.entity.LegalRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LegalRuleDao {

    @Query("SELECT * FROM legal_rules ORDER BY action_name ASC")
    fun getAll(): Flow<List<LegalRuleEntity>>

    @Query("SELECT COUNT(*) FROM legal_rules")
    suspend fun count(): Int

    @Query("SELECT * FROM legal_rules WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): LegalRuleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rules: List<LegalRuleEntity>)

    /**
     * Metadata-only adoption of historical built-in rows. NEVER overwrite user
     * annotations, duration or other original record contents during upgrade.
     * Match action_name and unversioned revision to avoid touching custom IDs.
     */
    @Query("""
        UPDATE legal_rules
        SET review_status = 'NEEDS_REVIEW',
            effective_from = '',
            effective_to = :expiresAt,
            revision = :revision,
            calculation_kind = :kind
        WHERE id = :id AND action_name = :expectedName AND revision = 0
    """)
    suspend fun adoptSeedMetadata(
        id: Long,
        expectedName: String,
        expiresAt: String?,
        revision: Int,
        kind: String
    ): Int

    @Query("DELETE FROM legal_rules")
    suspend fun clear()
}
