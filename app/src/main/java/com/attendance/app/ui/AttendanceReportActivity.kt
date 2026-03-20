package com.attendance.app.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.attendance.app.R
import com.attendance.app.adapter.AttendanceSummaryAdapter
import com.attendance.app.databinding.ActivityAttendanceReportBinding
import com.attendance.app.viewmodel.AttendanceViewModel

/**
 * Displays attendance summary for all students with percentage progress bars.
 */
class AttendanceReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAttendanceReportBinding
    private val attendanceViewModel: AttendanceViewModel by viewModels()
    private lateinit var adapter: AttendanceSummaryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAttendanceReportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.attendance_report)

        adapter = AttendanceSummaryAdapter { summary ->
            // Future: drill down to per-student detail view
        }
        binding.rvReport.adapter = adapter

        attendanceViewModel.attendanceSummary.observe(this, Observer { summaries ->
            adapter.submitList(summaries)
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
