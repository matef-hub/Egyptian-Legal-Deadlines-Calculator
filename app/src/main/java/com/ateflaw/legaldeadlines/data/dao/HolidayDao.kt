package com.ateflaw.legaldeadlines.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ateflaw.legaldeadlines.data.entity.HolidayEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HolidayDao {

    @Query("SELECT * FROM holidays ORDER BY holiday_date ASC")
    fun getAll(): Flow<List<HolidayEntity>>

    @Query("SELECT COUNT(*) FROM holidays")
    suspend fun count(): Int

    @Query("SELECT * FROM holidays WHERE holiday_date = :date LIMIT 1")
    suspend fun getHoliday(date: String): HolidayEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM holidays WHERE holiday_date = :date LIMIT 1)")
    suspend fun isHoliday(date: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(holidays: List<HolidayEntity>)

    @Query("DELETE FROM holidays WHERE holiday_date LIKE :yearPrefix || '%'")
    suspend fun deleteHolidaysByYear(yearPrefix: String)

    @Query("DELETE FROM holidays")
    suspend fun clear()
}
