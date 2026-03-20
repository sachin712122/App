package com.attendance.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.attendance.app.R
import com.attendance.app.adapter.AttendanceMarkingAdapter
import com.attendance.app.data.AttendanceStatus
import com.attendance.app.databinding.ActivityMainBinding
import com.attendance.app.kiosk.KioskManager
import com.attendance.app.viewmodel.AttendanceViewModel
import com.attendance.app.viewmodel.StudentViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Main screen shown to students / operators.
 *
 * Displays the full student list with Present/Late/Absent buttons.
 * When kiosk mode is active the device is locked to this screen.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val studentViewModel: StudentViewModel by viewModels()
    private val attendanceViewModel: AttendanceViewModel by viewModels()
    private lateinit var kioskManager: KioskManager
    private lateinit var adapter: AttendanceMarkingAdapter

    private val today: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        kioskManager = KioskManager(this)

        // Set default PIN if first run
        if (!kioskManager.hasAdminPin()) {
            kioskManager.setAdminPin(KioskManager.DEFAULT_ADMIN_PIN)
        }

        setupRecyclerView()
        setupButtons()
        observeData()

        // Keep screen on while attendance is being taken
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // If launched from boot in kiosk mode, activate immediately
        if (intent.getBooleanExtra(EXTRA_KIOSK_BOOT, false)) {
            kioskManager.activateKioskMode(this)
        } else if (kioskManager.isKioskModeEnabled) {
            kioskManager.enforceKioskOnResume(this)
        }
    }

    override fun onResume() {
        super.onResume()
        kioskManager.enforceKioskOnResume(this)
        updateDateDisplay()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val kioskItem = menu.findItem(R.id.action_kiosk_toggle)
        if (kioskManager.isKioskModeEnabled) {
            kioskItem?.setTitle(R.string.exit_kiosk_mode)
            kioskItem?.setIcon(R.drawable.ic_kiosk_off)
        } else {
            kioskItem?.setTitle(R.string.enter_kiosk_mode)
            kioskItem?.setIcon(R.drawable.ic_kiosk_on)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_admin -> {
                promptAdminPin()
                true
            }
            R.id.action_kiosk_toggle -> {
                if (kioskManager.isKioskModeEnabled) {
                    promptAdminPin(exitKioskAfterAuth = true)
                } else {
                    promptAdminPin(enableKioskAfterAuth = true)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Back button is disabled in kiosk mode
    override fun onBackPressed() {
        if (kioskManager.isKioskModeEnabled) {
            // Do nothing — kiosk mode prevents navigation away
            return
        }
        super.onBackPressed()
    }

    // ─── Setup ───────────────────────────────────────────────────────────────────

    private fun setupRecyclerView() {
        adapter = AttendanceMarkingAdapter { studentId, status ->
            attendanceViewModel.markAttendance(studentId, status, today)
        }
        binding.rvStudents.adapter = adapter
        binding.rvStudents.setHasFixedSize(true)
    }

    private fun setupButtons() {
        binding.btnMarkAllPresent.setOnClickListener {
            adapter.markAllPresent()
            val students = studentViewModel.allStudents.value ?: return@setOnClickListener
            students.forEach { student ->
                attendanceViewModel.markAttendance(student.id, AttendanceStatus.PRESENT, today)
            }
            Toast.makeText(this, R.string.all_marked_present, Toast.LENGTH_SHORT).show()
        }

        binding.btnSaveAttendance.setOnClickListener {
            val students = studentViewModel.allStudents.value ?: emptyList()
            val statusMap = adapter.getStatusMap()
            if (students.isEmpty()) {
                Toast.makeText(this, R.string.no_students_to_save, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            attendanceViewModel.saveAttendanceBatch(students, statusMap, today)
        }
    }

    private fun observeData() {
        studentViewModel.allStudents.observe(this, Observer { students ->
            adapter.submitList(students)
            binding.tvStudentCount.text = getString(R.string.student_count_format, students.size)

            // Load existing attendance for today so buttons show correct state
            attendanceViewModel.getAttendanceForDate(today).observe(this, Observer { records ->
                val map = records.associate { it.studentId to it.status }
                adapter.setInitialStatuses(map)
            })
        })

        attendanceViewModel.saveResult.observe(this, Observer { success ->
            success ?: return@Observer
            if (success) {
                Toast.makeText(this, R.string.attendance_saved, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, R.string.attendance_save_error, Toast.LENGTH_SHORT).show()
            }
            attendanceViewModel.clearSaveResult()
        })

        studentViewModel.studentCount.observe(this, Observer { count ->
            binding.tvStudentCount.text = getString(R.string.student_count_format, count ?: 0)
        })
    }

    private fun updateDateDisplay() {
        val displayFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
        binding.tvCurrentDate.text = displayFormat.format(Date())
    }

    // ─── Admin PIN dialog ─────────────────────────────────────────────────────────

    private fun promptAdminPin(
        enableKioskAfterAuth: Boolean = false,
        exitKioskAfterAuth: Boolean = false
    ) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_pin_entry, null)
        val pinInput = dialogView.findViewById<android.widget.EditText>(R.id.et_pin)

        AlertDialog.Builder(this)
            .setTitle(R.string.enter_admin_pin)
            .setView(dialogView)
            .setPositiveButton(R.string.ok) { _, _ ->
                val pin = pinInput.text.toString()
                if (kioskManager.verifyAdminPin(pin)) {
                    when {
                        exitKioskAfterAuth -> {
                            kioskManager.deactivateKioskMode(this)
                            invalidateOptionsMenu()
                            Toast.makeText(this, R.string.kiosk_mode_disabled, Toast.LENGTH_SHORT).show()
                        }
                        enableKioskAfterAuth -> {
                            kioskManager.activateKioskMode(this)
                            invalidateOptionsMenu()
                            Toast.makeText(this, R.string.kiosk_mode_enabled, Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            startActivity(Intent(this, AdminActivity::class.java))
                        }
                    }
                } else {
                    Toast.makeText(this, R.string.wrong_pin, Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    companion object {
        const val EXTRA_KIOSK_BOOT = "extra_kiosk_boot"
    }
}
