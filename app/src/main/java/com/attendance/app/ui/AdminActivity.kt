package com.attendance.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.attendance.app.R
import com.attendance.app.adapter.StudentManageAdapter
import com.attendance.app.databinding.ActivityAdminBinding
import com.attendance.app.kiosk.KioskManager
import com.attendance.app.viewmodel.AttendanceViewModel
import com.attendance.app.viewmodel.StudentViewModel

/**
 * Admin panel — accessible only after entering the admin PIN.
 *
 * Provides:
 *  - Student management (add / edit / remove)
 *  - Kiosk mode toggle
 *  - Change admin PIN
 *  - View attendance reports
 */
class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding
    private val studentViewModel: StudentViewModel by viewModels()
    private val attendanceViewModel: AttendanceViewModel by viewModels()
    private lateinit var kioskManager: KioskManager
    private lateinit var adapter: StudentManageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.admin_panel)

        kioskManager = KioskManager(this)

        setupRecyclerView()
        setupButtons()
        observeData()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    // ─── Setup ───────────────────────────────────────────────────────────────────

    private fun setupRecyclerView() {
        adapter = StudentManageAdapter(
            onEditClick = { student ->
                val intent = Intent(this, AddStudentActivity::class.java).apply {
                    putExtra(AddStudentActivity.EXTRA_STUDENT_ID, student.id)
                    putExtra(AddStudentActivity.EXTRA_STUDENT_NAME, student.name)
                    putExtra(AddStudentActivity.EXTRA_ROLL_NUMBER, student.rollNumber)
                    putExtra(AddStudentActivity.EXTRA_CLASS_NAME, student.className)
                }
                startActivity(intent)
            },
            onDeleteClick = { student ->
                AlertDialog.Builder(this)
                    .setTitle(R.string.confirm_delete)
                    .setMessage(getString(R.string.confirm_delete_student, student.name))
                    .setPositiveButton(R.string.delete) { _, _ ->
                        studentViewModel.deactivateStudent(student.id)
                        Toast.makeText(this, R.string.student_deleted, Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
            }
        )
        binding.rvStudents.adapter = adapter
    }

    private fun setupButtons() {
        binding.btnAddStudent.setOnClickListener {
            startActivity(Intent(this, AddStudentActivity::class.java))
        }

        binding.btnViewReports.setOnClickListener {
            startActivity(Intent(this, AttendanceReportActivity::class.java))
        }

        binding.btnToggleKiosk.setOnClickListener {
            if (kioskManager.isKioskModeEnabled) {
                kioskManager.deactivateKioskMode(this)
                binding.btnToggleKiosk.text = getString(R.string.enable_kiosk_mode)
                Toast.makeText(this, R.string.kiosk_mode_disabled, Toast.LENGTH_SHORT).show()
            } else {
                kioskManager.activateKioskMode(this)
                binding.btnToggleKiosk.text = getString(R.string.disable_kiosk_mode)
                Toast.makeText(this, R.string.kiosk_mode_enabled, Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnChangePin.setOnClickListener {
            showChangePinDialog()
        }
    }

    private fun observeData() {
        studentViewModel.allStudents.observe(this, Observer { students ->
            adapter.submitList(students)
            binding.tvStudentCount.text =
                getString(R.string.student_count_format, students.size)
        })

        // Update kiosk button label to reflect current state
        updateKioskButtonLabel()
    }

    private fun updateKioskButtonLabel() {
        if (kioskManager.isKioskModeEnabled) {
            binding.btnToggleKiosk.text = getString(R.string.disable_kiosk_mode)
        } else {
            binding.btnToggleKiosk.text = getString(R.string.enable_kiosk_mode)
        }
    }

    // ─── Change PIN dialog ────────────────────────────────────────────────────────

    private fun showChangePinDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_pin, null)
        val etCurrentPin = dialogView.findViewById<android.widget.EditText>(R.id.et_current_pin)
        val etNewPin = dialogView.findViewById<android.widget.EditText>(R.id.et_new_pin)
        val etConfirmPin = dialogView.findViewById<android.widget.EditText>(R.id.et_confirm_pin)

        AlertDialog.Builder(this)
            .setTitle(R.string.change_admin_pin)
            .setView(dialogView)
            .setPositiveButton(R.string.save) { _, _ ->
                val currentPin = etCurrentPin.text.toString()
                val newPin = etNewPin.text.toString()
                val confirmPin = etConfirmPin.text.toString()

                when {
                    !kioskManager.verifyAdminPin(currentPin) ->
                        Toast.makeText(this, R.string.wrong_current_pin, Toast.LENGTH_SHORT).show()
                    newPin.length < 4 ->
                        Toast.makeText(this, R.string.pin_too_short, Toast.LENGTH_SHORT).show()
                    newPin != confirmPin ->
                        Toast.makeText(this, R.string.pins_do_not_match, Toast.LENGTH_SHORT).show()
                    else -> {
                        kioskManager.setAdminPin(newPin)
                        Toast.makeText(this, R.string.pin_changed, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
