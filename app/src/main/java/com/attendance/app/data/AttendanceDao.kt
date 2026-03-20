package com.attendance.app.data

import androidx.lifecycle.LiveData
import androidx.room.*

/**
 * Data Transfer Object for student attendance summary.
 */
data class AttendanceSummary(
    val studentId: Long,
    val studentName: String,
    val rollNumber: String,
    val className: String,
    val totalDays: Int,
    val presentDays: Int,
    val absentDays: Int,
    val lateDays: Int
)

/**
 * Data Access Object for attendance record operations.
 */
@Dao
interface AttendanceDao {

    @Query(
        """
        SELECT * FROM attendance_records 
        WHERE date = :date 
        ORDER BY studentId
        """
    )
    fun getAttendanceForDate(date: String): LiveData<List<AttendanceRecord>>

    @Query(
        """
        SELECT * FROM attendance_records 
        WHERE studentId = :studentId 
        ORDER BY date DESC
        """
    )
    fun getAttendanceForStudent(studentId: Long): LiveData<List<AttendanceRecord>>

    @Query(
        """
        SELECT * FROM attendance_records 
        WHERE studentId = :studentId AND date = :date
        LIMIT 1
        """
    )
    suspend fun getAttendanceForStudentOnDate(studentId: Long, date: String): AttendanceRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAttendance(record: AttendanceRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAttendanceBatch(records: List<AttendanceRecord>)

    @Query("DELETE FROM attendance_records WHERE studentId = :studentId AND date = :date")
    suspend fun deleteAttendance(studentId: Long, date: String)

    @Query(
        """
        SELECT 
            s.id AS studentId,
            s.name AS studentName,
            s.rollNumber AS rollNumber,
            s.className AS className,
            COUNT(a.id) AS totalDays,
            SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END) AS presentDays,
            SUM(CASE WHEN a.status = 'ABSENT' THEN 1 ELSE 0 END) AS absentDays,
            SUM(CASE WHEN a.status = 'LATE' THEN 1 ELSE 0 END) AS lateDays
        FROM students s
        LEFT JOIN attendance_records a ON s.id = a.studentId
        WHERE s.isActive = 1
        GROUP BY s.id, s.name, s.rollNumber, s.className
        ORDER BY s.className, s.rollNumber
        """
    )
    fun getAttendanceSummaryForAll(): LiveData<List<AttendanceSummary>>

    @Query(
        """
        SELECT 
            s.id AS studentId,
            s.name AS studentName,
            s.rollNumber AS rollNumber,
            s.className AS className,
            COUNT(a.id) AS totalDays,
            SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END) AS presentDays,
            SUM(CASE WHEN a.status = 'ABSENT' THEN 1 ELSE 0 END) AS absentDays,
            SUM(CASE WHEN a.status = 'LATE' THEN 1 ELSE 0 END) AS lateDays
        FROM students s
        LEFT JOIN attendance_records a ON s.id = a.studentId
        WHERE s.isActive = 1 AND s.className = :className
        GROUP BY s.id, s.name, s.rollNumber, s.className
        ORDER BY s.rollNumber
        """
    )
    fun getAttendanceSummaryForClass(className: String): LiveData<List<AttendanceSummary>>

    @Query("SELECT DISTINCT date FROM attendance_records ORDER BY date DESC")
    fun getAllAttendanceDates(): LiveData<List<String>>
}
