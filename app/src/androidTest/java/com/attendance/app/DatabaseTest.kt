package com.attendance.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.attendance.app.data.AppDatabase
import com.attendance.app.data.AttendanceRecord
import com.attendance.app.data.AttendanceStatus
import com.attendance.app.data.Student
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test for the Room database (AttendanceDao and StudentDao).
 */
@RunWith(AndroidJUnit4::class)
class DatabaseTest {

    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = androidx.room.Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndRetrieveStudent() = runBlocking {
        val student = Student(name = "Test Student", rollNumber = "T01", className = "11-A")
        val id = db.studentDao().insertStudent(student)
        val retrieved = db.studentDao().getStudentById(id)
        assertNotNull(retrieved)
        assertEquals("Test Student", retrieved?.name)
        assertEquals("T01", retrieved?.rollNumber)
    }

    @Test
    fun markAttendanceAndRetrieve() = runBlocking {
        val student = Student(name = "Attendance Test", rollNumber = "A01", className = "11-B")
        val studentId = db.studentDao().insertStudent(student)

        val record = AttendanceRecord(
            studentId = studentId,
            date = "2026-03-20",
            status = AttendanceStatus.PRESENT
        )
        db.attendanceDao().insertOrUpdateAttendance(record)

        val retrieved = db.attendanceDao().getAttendanceForStudentOnDate(studentId, "2026-03-20")
        assertNotNull(retrieved)
        assertEquals(AttendanceStatus.PRESENT, retrieved?.status)
    }

    @Test
    fun updateAttendanceRecord() = runBlocking {
        val student = Student(name = "Update Test", rollNumber = "U01", className = "11-C")
        val studentId = db.studentDao().insertStudent(student)

        // Mark as ABSENT first
        db.attendanceDao().insertOrUpdateAttendance(
            AttendanceRecord(studentId = studentId, date = "2026-03-20", status = AttendanceStatus.ABSENT)
        )

        // Update to LATE
        db.attendanceDao().insertOrUpdateAttendance(
            AttendanceRecord(studentId = studentId, date = "2026-03-20", status = AttendanceStatus.LATE)
        )

        val retrieved = db.attendanceDao().getAttendanceForStudentOnDate(studentId, "2026-03-20")
        assertEquals(AttendanceStatus.LATE, retrieved?.status)
    }

    @Test
    fun deactivateStudentHidesFromActiveList() = runBlocking {
        val student = Student(name = "Deactivate Test", rollNumber = "D01", className = "12-A")
        val studentId = db.studentDao().insertStudent(student)

        db.studentDao().deactivateStudent(studentId)

        val activeStudents = db.studentDao().getAllActiveStudentsOnce()
        assertFalse(activeStudents.any { it.id == studentId })
    }
}
