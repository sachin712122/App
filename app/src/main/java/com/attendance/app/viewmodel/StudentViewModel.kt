package com.attendance.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.attendance.app.data.AttendanceRepository
import com.attendance.app.data.Student
import kotlinx.coroutines.launch

/**
 * ViewModel for student management operations.
 */
class StudentViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AttendanceRepository(application)

    val allStudents = repository.getAllActiveStudents()
    val allClasses = repository.getAllClasses()
    val studentCount = repository.getActiveStudentCount()

    fun getStudentsByClass(className: String) = repository.getStudentsByClass(className)

    fun addStudent(student: Student) {
        viewModelScope.launch {
            repository.insertStudent(student)
        }
    }

    fun updateStudent(student: Student) {
        viewModelScope.launch {
            repository.updateStudent(student)
        }
    }

    fun deactivateStudent(studentId: Long) {
        viewModelScope.launch {
            repository.deactivateStudent(studentId)
        }
    }
}
