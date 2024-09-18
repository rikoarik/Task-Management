package com.taskmanagement.presentation.ui.task

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.taskmanagement.R
import com.taskmanagement.data.model.Task
import com.taskmanagement.data.repository.TaskRepository
import com.taskmanagement.databinding.ActivityDetailTaskBinding
import com.taskmanagement.presentation.viewmodel.TaskViewModel
import com.taskmanagement.presentation.viewmodel.TaskViewModelFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DetailTaskActivity : AppCompatActivity() {

    private lateinit var viewModel: TaskViewModel
    private lateinit var binding: ActivityDetailTaskBinding

    private var isEditing = false
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDetailTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val taskRepository = TaskRepository(FirebaseDatabase.getInstance())
        val factory = TaskViewModelFactory(taskRepository)
        viewModel = ViewModelProvider(this, factory).get(TaskViewModel::class.java)

        val taskId = intent.getStringExtra("idTask")
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        if (taskId != null) {
            viewModel.fetchTaskById(userId!!, taskId)
        }

        setupObservers()
        setupListeners()

        toggleEditing(false)
    }

    private fun setupObservers() {
        viewModel.task.observe(this) { task ->
            if (task != null) {
                binding.editTextTitle.setText(task.title)
                binding.editTextDesc.setText(task.description)
                binding.editTextDate.setText(formatDate(task.date))
                binding.editTextTime.setText(formatTime(task.time))
                Glide.with(this).load(task.photoFileName).into(binding.imageViewPhoto)
            } else {
                Toast.makeText(this, "Task not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupListeners() {
        binding.editTextDate.setOnClickListener {
            showDatePicker()
        }

        binding.editTextTime.setOnClickListener {
            showTimePicker()
        }

        binding.buttonSaveTask.setOnClickListener {
            updateTask()
        }

        binding.buttonTaskDone.setOnClickListener {
            markTaskAsDone()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = "$dayOfMonth/${month + 1}/$year"
                binding.editTextDate.setText(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                val selectedTime = String.format("%02d:%02d", hourOfDay, minute)
                binding.editTextTime.setText(selectedTime)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        timePickerDialog.show()
    }

    private fun updateTask() {
        val title = binding.editTextTitle.text.toString()
        val description = binding.editTextDesc.text.toString()
        val dateString = binding.editTextDate.text.toString()
        val timeString = binding.editTextTime.text.toString()

        if (title.isNotBlank() && description.isNotBlank() && dateString.isNotBlank() && timeString.isNotBlank()) {
            val dateInMillis = convertDateStringToMillis(dateString)
            val timeInMillis = convertTimeStringToMillis(timeString)

            val updatedTask = Task(
                id = intent.getStringExtra("idTask") ?: "",
                title = title,
                description = description,
                dateString = dateString,
                date = dateInMillis,
                time = timeInMillis,
                userId = userId ?: ""
            )


            viewModel.updateTask(userId!!, updatedTask.id, updatedTask)
            Toast.makeText(this, "Task updated", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
        }
    }

    private fun markTaskAsDone() {
        val taskId = intent.getStringExtra("idTask")
        if (taskId != null) {
            viewModel.updateTaskStatus(userId!!, taskId, "Done")
            Toast.makeText(this, "Task marked as Done", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_detail_task, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_edit -> {
                if (isEditing) {
                    toggleEditing(false)
                    item.title = "Edit"
                } else {
                    toggleEditing(true)
                    item.title = "Batal"
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun toggleEditing(isEditable: Boolean) {
        isEditing = isEditable
        binding.editTextTitle.isEnabled = isEditable
        binding.editTextDesc.isEnabled = isEditable
        binding.editTextDate.isEnabled = isEditable
        binding.editTextTime.isEnabled = isEditable
        binding.editTextPhoto.isEnabled = isEditable

        binding.buttonSaveTask.visibility = if (isEditable) {
            android.view.View.VISIBLE
        } else {
            android.view.View.GONE
        }
        binding.buttonTaskDone.visibility = if (isEditable) {
            android.view.View.GONE
        } else {
            android.view.View.VISIBLE
        }
    }
    private fun formatDate(inputDate: Long): String {
        return try {
            val date = Date(inputDate)
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            outputFormat.format(date)
        } catch (e: Exception) {
            inputDate.toString()
        }
    }

    private fun formatTime(inputTime: Long): String {
        return try {
            val time = Date(inputTime)
            val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            outputFormat.format(time)
        } catch (e: Exception) {
            inputTime.toString()
        }
    }
    private fun convertDateStringToMillis(dateString: String): Long {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = sdf.parse(dateString)
            date?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    private fun convertTimeStringToMillis(timeString: String): Long {
        return try {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val date = sdf.parse(timeString)
            date?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

}
