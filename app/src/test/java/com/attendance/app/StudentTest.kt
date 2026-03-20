package com.attendance.app

import com.attendance.app.data.AttendanceStatus
import com.attendance.app.data.Student
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for the Student data model.
 */
class StudentTest {

    @Test
    fun `student defaults to active`() {
        val student = Student(name = "Alice", rollNumber = "001", className = "10-A")
        assertTrue(student.isActive)
    }

    @Test
    fun `student roll number stored correctly`() {
        val student = Student(name = "Bob", rollNumber = "B-42", className = "10-B")
        assertEquals("B-42", student.rollNumber)
    }

    @Test
    fun `attendance status enum values exist`() {
        val statuses = AttendanceStatus.values()
        assertTrue(statuses.contains(AttendanceStatus.PRESENT))
        assertTrue(statuses.contains(AttendanceStatus.ABSENT))
        assertTrue(statuses.contains(AttendanceStatus.LATE))
    }

    @Test
    fun `attendance status valueOf works`() {
        assertEquals(AttendanceStatus.PRESENT, AttendanceStatus.valueOf("PRESENT"))
        assertEquals(AttendanceStatus.ABSENT, AttendanceStatus.valueOf("ABSENT"))
        assertEquals(AttendanceStatus.LATE, AttendanceStatus.valueOf("LATE"))
    }
}
