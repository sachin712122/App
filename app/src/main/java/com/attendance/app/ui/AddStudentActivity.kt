package com.attendance.app.ui

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.attendance.app.R
import com.attendance.app.data.Student
import com.attendance.app.databinding.ActivityAddStudentBinding
import com.attendance.app.viewmodel.StudentViewModel

/**
 * Add a new student or edit an existing one.
 */
class AddStudentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddStudentBinding
    private val studentViewModel: StudentViewModel by viewModels()

    private var editStudentId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Check if we are editing an existing student
        editStudentId = intent.getLongExtra(EXTRA_STUDENT_ID, -1L)
        val isEditing = editStudentId != -1L

        supportActionBar?.title = if (isEditing) getString(R.string.edit_student) else getString(R.string.add_student)

        if (isEditing) {
            binding.etName.setText(intent.getStringExtra(EXTRA_STUDENT_NAME) ?: "")
            binding.etRollNumber.setText(intent.getStringExtra(EXTRA_ROLL_NUMBER) ?: "")
            binding.etClassName.setText(intent.getStringExtra(EXTRA_CLASS_NAME) ?: "")
        }

        binding.btnSave.setOnClickListener {
            saveStudent(isEditing)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun saveStudent(isEditing: Boolean) {
        val name = binding.etName.text.toString().trim()
        val rollNumber = binding.etRollNumber.text.toString().trim()
        val className = binding.etClassName.text.toString().trim()

        if (name.isEmpty()) {
            binding.etName.error = getString(R.string.name_required)
            return
        }
        if (rollNumber.isEmpty()) {
            binding.etRollNumber.error = getString(R.string.roll_number_required)
            return
        }
        if (className.isEmpty()) {
            binding.etClassName.error = getString(R.string.class_name_required)
            return
        }

        val student = if (isEditing) {
            Student(id = editStudentId, name = name, rollNumber = rollNumber, className = className)
        } else {
            Student(name = name, rollNumber = rollNumber, className = className)
        }

        if (isEditing) {
            studentViewModel.updateStudent(student)
            Toast.makeText(this, R.string.student_updated, Toast.LENGTH_SHORT).show()
        } else {
            studentViewModel.addStudent(student)
            Toast.makeText(this, R.string.student_added, Toast.LENGTH_SHORT).show()
        }
        finish()
    }

    companion object {
        const val EXTRA_STUDENT_ID = "extra_student_id"
        const val EXTRA_STUDENT_NAME = "extra_student_name"
        const val EXTRA_ROLL_NUMBER = "extra_roll_number"
        const val EXTRA_CLASS_NAME = "extra_class_name"
    }
}
