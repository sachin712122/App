package com.attendance.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.attendance.app.R
import com.attendance.app.data.AttendanceSummary
import com.attendance.app.databinding.ItemAttendanceSummaryBinding

/**
 * Adapter for displaying per-student attendance summary statistics.
 */
class AttendanceSummaryAdapter(
    private val onStudentClick: (AttendanceSummary) -> Unit
) : ListAdapter<AttendanceSummary, AttendanceSummaryAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAttendanceSummaryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemAttendanceSummaryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(summary: AttendanceSummary) {
            binding.tvStudentName.text = summary.studentName
            binding.tvRollNumber.text = binding.root.context.getString(
                R.string.roll_number_format, summary.rollNumber
            )
            binding.tvClassName.text = summary.className
            binding.tvPresent.text = summary.presentDays.toString()
            binding.tvAbsent.text = summary.absentDays.toString()
            binding.tvLate.text = summary.lateDays.toString()

            val percentage = if (summary.totalDays > 0) {
                ((summary.presentDays + summary.lateDays) * 100.0 / summary.totalDays).toInt()
            } else 0
            binding.tvAttendancePercentage.text =
                binding.root.context.getString(R.string.attendance_percentage_format, percentage)
            binding.progressAttendance.progress = percentage

            binding.root.setOnClickListener { onStudentClick(summary) }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<AttendanceSummary>() {
            override fun areItemsTheSame(a: AttendanceSummary, b: AttendanceSummary) =
                a.studentId == b.studentId

            override fun areContentsTheSame(a: AttendanceSummary, b: AttendanceSummary) =
                a == b
        }
    }
}
