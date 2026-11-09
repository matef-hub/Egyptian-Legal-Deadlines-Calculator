package com.ateflaw.legaldeadlines.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ateflaw.legaldeadlines.domain.model.Holiday
import java.time.LocalDate

@Entity(
    tableName = "holidays",
    indices = [Index(value = ["holiday_date"], unique = true)]
)
data class HolidayEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "holiday_date")
    val holidayDate: String, // Stored as ISO-8601: YYYY-MM-DD

    @ColumnInfo(name = "name")
    val name: String
) {
    fun toDomain(): Holiday = Holiday(
        id = id,
        holidayDate = LocalDate.parse(holidayDate),
        name = name
    )

    companion object {
        fun fromDomain(domain: Holiday): HolidayEntity = HolidayEntity(
            id = domain.id,
            holidayDate = domain.holidayDate.toString(),
            name = domain.name
        )
    }
}
