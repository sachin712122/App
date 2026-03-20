package com.attendance.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.attendance.app.data.AttendanceRecord
import com.attendance.app.data.AttendanceRepository
import com.attendance.app.data.AttendanceStatus
import com.attendance.app.data.Student
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ViewModel for attendance marking and reporting operations.
 */
class AttendanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AttendanceRepository(application)

    private val _currentDate = MutableLiveData(getTodayDate())
    val currentDate: LiveData<String> = _currentDate

    private val _saveResult = MutableLiveData<Boolean?>()
    val saveResult: LiveData<Boolean?> = _saveResult

    val attendanceSummary = repository.getAttendanceSummaryForAll()
    val allDates = repository.getAllAttendanceDates()

    fun getAttendanceForDate(date: String) = repository.getAttendanceForDate(date)

    fun getAttendanceForStudent(studentId: Long) = repository.getAttendanceForStudent(studentId)

    fun getAttendanceSummaryForClass(className: String) =
        repository.getAttendanceSummaryForClass(className)

    /**
     * Mark attendance for a single student on the current date.
     */
    fun markAttendance(studentId: Long, status: AttendanceStatus, date: String = getTodayDate()) {
        viewModelScope.launch {
            val record = AttendanceRecord(
                studentId = studentId,
                date = date,
                status = status
            )
            repository.insertOrUpdateAttendance(record)
        }
    }

    /**
     * Save attendance for all students at once (bulk save for a class session).
     */
    fun saveAttendanceBatch(
        students: List<Student>,
        statusMap: Map<Long, AttendanceStatus>,
        date: String = getTodayDate()
    ) {
        viewModelScope.launch {
            try {
                val records = students.mapNotNull { student ->
                    statusMap[student.id]?.let { status ->
                        AttendanceRecord(
                            studentId = student.id,
                            date = date,
                            status = status
                        )
                    }
                }
                repository.insertOrUpdateAttendanceBatch(records)
                _saveResult.postValue(true)
            } catch (e: Exception) {
                _saveResult.postValue(false)
            }
        }
    }

    fun clearSaveResult() {
        _saveResult.value = null
    }

    companion object {
        fun getTodayDate(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return sdf.format(Date())
        }
    }
}
