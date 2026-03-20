package com.attendance.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Possible attendance statuses for a student on a given day.
 */
enum class AttendanceStatus {
    PRESENT,
    ABSENT,
    LATE
}

/**
 * Entity representing an attendance record for a student on a specific date.
 */
@Entity(
    tableName = "attendance_records",
    foreignKeys = [
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["studentId", "date"], unique = true)]
)
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentId: Long,
    val date: String,           // ISO format: YYYY-MM-DD
    val status: AttendanceStatus,
    val markedAt: Long = System.currentTimeMillis()
)
