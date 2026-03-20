package com.attendance.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.attendance.app.R
import com.attendance.app.data.AttendanceStatus
import com.attendance.app.data.Student
import com.attendance.app.databinding.ItemStudentAttendanceBinding

/**
 * Adapter that displays each student with Present / Late / Absent toggle buttons.
 * Used on the main attendance-marking screen.
 */
class AttendanceMarkingAdapter(
    private val onStatusChanged: (studentId: Long, status: AttendanceStatus) -> Unit
) : ListAdapter<Student, AttendanceMarkingAdapter.ViewHolder>(DIFF_CALLBACK) {

    /** Tracks the current attendance status for each student by ID. */
    private val statusMap = mutableMapOf<Long, AttendanceStatus>()

    fun getStatusMap(): Map<Long, AttendanceStatus> = statusMap.toMap()

    /** Returns the number of students currently marked as PRESENT. */
    fun getPresentCount(): Int = statusMap.values.count { it == AttendanceStatus.PRESENT }

    /** Pre-fill statuses loaded from the database. */
    fun setInitialStatuses(map: Map<Long, AttendanceStatus>) {
        statusMap.clear()
        statusMap.putAll(map)
        notifyDataSetChanged()
    }

    /** Mark all students as PRESENT at once. */
    fun markAllPresent() {
        currentList.forEach { statusMap[it.id] = AttendanceStatus.PRESENT }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStudentAttendanceBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemStudentAttendanceBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(student: Student) {
            binding.tvStudentName.text = student.name
            binding.tvRollNumber.text = binding.root.context.getString(
                R.string.roll_number_format, student.rollNumber
            )
            binding.tvClassName.text = student.className

            updateButtonStates(statusMap[student.id])

            binding.btnPresent.setOnClickListener {
                updateStatus(student.id, AttendanceStatus.PRESENT)
            }
            binding.btnLate.setOnClickListener {
                updateStatus(student.id, AttendanceStatus.LATE)
            }
            binding.btnAbsent.setOnClickListener {
                updateStatus(student.id, AttendanceStatus.ABSENT)
            }
        }

        private fun updateStatus(studentId: Long, status: AttendanceStatus) {
            statusMap[studentId] = status
            updateButtonStates(status)
            onStatusChanged(studentId, status)
        }

        private fun updateButtonStates(status: AttendanceStatus?) {
            val ctx = binding.root.context
            val presentColor = ContextCompat.getColor(ctx, R.color.present_color)
            val lateColor = ContextCompat.getColor(ctx, R.color.late_color)
            val absentColor = ContextCompat.getColor(ctx, R.color.absent_color)
            val defaultColor = ContextCompat.getColor(ctx, R.color.status_button_default)

            binding.btnPresent.setBackgroundColor(
                if (status == AttendanceStatus.PRESENT) presentColor else defaultColor
            )
            binding.btnLate.setBackgroundColor(
                if (status == AttendanceStatus.LATE) lateColor else defaultColor
            )
            binding.btnAbsent.setBackgroundColor(
                if (status == AttendanceStatus.ABSENT) absentColor else defaultColor
            )
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Student>() {
            override fun areItemsTheSame(oldItem: Student, newItem: Student) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Student, newItem: Student) =
                oldItem == newItem
        }
    }
}
