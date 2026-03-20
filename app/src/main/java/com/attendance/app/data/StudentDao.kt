package com.attendance.app.data

import androidx.lifecycle.LiveData
import androidx.room.*

/**
 * Data Access Object for student operations.
 */
@Dao
interface StudentDao {

    @Query("SELECT * FROM students WHERE isActive = 1 ORDER BY className, rollNumber")
    fun getAllActiveStudents(): LiveData<List<Student>>

    @Query("SELECT * FROM students WHERE isActive = 1 ORDER BY className, rollNumber")
    suspend fun getAllActiveStudentsOnce(): List<Student>

    @Query("SELECT * FROM students WHERE className = :className AND isActive = 1 ORDER BY rollNumber")
    fun getStudentsByClass(className: String): LiveData<List<Student>>

    @Query("SELECT * FROM students WHERE id = :studentId")
    suspend fun getStudentById(studentId: Long): Student?

    @Query("SELECT DISTINCT className FROM students WHERE isActive = 1 ORDER BY className")
    fun getAllClasses(): LiveData<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Update
    suspend fun updateStudent(student: Student)

    @Query("UPDATE students SET isActive = 0 WHERE id = :studentId")
    suspend fun deactivateStudent(studentId: Long)

    @Query("SELECT COUNT(*) FROM students WHERE isActive = 1")
    fun getActiveStudentCount(): LiveData<Int>
}
