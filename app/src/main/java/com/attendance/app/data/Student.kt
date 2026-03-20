package com.attendance.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a student in the attendance system.
 */
@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val rollNumber: String,
    val className: String,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
