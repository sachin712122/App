package com.attendance.app.data

import android.content.Context

/**
 * Repository providing a clean API for data operations.
 */
class AttendanceRepository(context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val studentDao = db.studentDao()
    private val attendanceDao = db.attendanceDao()

    // --- Student operations ---

    fun getAllActiveStudents() = studentDao.getAllActiveStudents()

    suspend fun getAllActiveStudentsOnce() = studentDao.getAllActiveStudentsOnce()

    fun getStudentsByClass(className: String) = studentDao.getStudentsByClass(className)

    suspend fun getStudentById(studentId: Long) = studentDao.getStudentById(studentId)

    fun getAllClasses() = studentDao.getAllClasses()

    suspend fun insertStudent(student: Student) = studentDao.insertStudent(student)

    suspend fun updateStudent(student: Student) = studentDao.updateStudent(student)

    suspend fun deactivateStudent(studentId: Long) = studentDao.deactivateStudent(studentId)

    fun getActiveStudentCount() = studentDao.getActiveStudentCount()

    // --- Attendance operations ---

    fun getAttendanceForDate(date: String) = attendanceDao.getAttendanceForDate(date)

    fun getAttendanceForStudent(studentId: Long) = attendanceDao.getAttendanceForStudent(studentId)

    suspend fun getAttendanceForStudentOnDate(studentId: Long, date: String) =
        attendanceDao.getAttendanceForStudentOnDate(studentId, date)

    suspend fun insertOrUpdateAttendance(record: AttendanceRecord) =
        attendanceDao.insertOrUpdateAttendance(record)

    suspend fun insertOrUpdateAttendanceBatch(records: List<AttendanceRecord>) =
        attendanceDao.insertOrUpdateAttendanceBatch(records)

    suspend fun deleteAttendance(studentId: Long, date: String) =
        attendanceDao.deleteAttendance(studentId, date)

    fun getAttendanceSummaryForAll() = attendanceDao.getAttendanceSummaryForAll()

    fun getAttendanceSummaryForClass(className: String) =
        attendanceDao.getAttendanceSummaryForClass(className)

    fun getAllAttendanceDates() = attendanceDao.getAllAttendanceDates()
}
