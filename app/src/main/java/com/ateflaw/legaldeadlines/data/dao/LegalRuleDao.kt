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

    @Query("DELETE FROM legal_rules")
    suspend fun clear()
}
