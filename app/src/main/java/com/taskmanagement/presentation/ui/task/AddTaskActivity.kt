package com.taskmanagement.presentation.ui.task

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.taskmanagement.R
import com.taskmanagement.common.utils.TaskAlarmReceiver
import com.taskmanagement.data.model.Task
import com.taskmanagement.data.repository.TaskRepository
import com.taskmanagement.databinding.ActivityAddTaskBinding
import com.taskmanagement.presentation.viewmodel.AddTaskViewModel
import com.taskmanagement.presentation.viewmodel.AddTaskViewModelFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddTaskBinding
    private lateinit var viewModel: AddTaskViewModel
    private var photoUri: Uri? = null
    private lateinit var progressDialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        progressDialog = ProgressDialog(this).apply {
            setMessage("Saving task...")
            setCancelable(false)
        }
        val taskRepository = TaskRepository(FirebaseDatabase.getInstance())
        val factory = AddTaskViewModelFactory(taskRepository)
        viewModel = ViewModelProvider(this, factory).get(AddTaskViewModel::class.java)

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewModel.taskSaved.observe(this) { success ->
            if (success) {
                progressDialog.dismiss()
                Toast.makeText(this, "Task saved successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        viewModel.taskError.observe(this) { error ->
            if (error != null) {
                progressDialog.dismiss()
                Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
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

        binding.editTextPhoto.setOnClickListener {
            openPhotoPicker()
        }

        binding.buttonSaveTask.setOnClickListener {
            saveTask()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            this,
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                binding.editTextDate.setText("$dayOfMonth/${month + 1}/$year")
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val timePicker = TimePickerDialog(
            this,
            { _: TimePicker, hourOfDay: Int, minute: Int ->
                binding.editTextTime.setText(String.format("%02d:%02d", hourOfDay, minute))
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        timePicker.show()
    }

    private fun openPhotoPicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        photoPickerLauncher.launch(intent)
    }

    private val photoPickerLauncher =
        this.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    photoUri = uri
                    binding.editTextPhoto.setText(uri.lastPathSegment)
                    binding.imageViewPhoto.setImageURI(uri)
                    binding.imageViewPhoto.visibility = android.view.View.VISIBLE
                }
            }
        }

    private fun saveTask() {
        val title = binding.editTextTitle.text.toString().trim()
        val description = binding.editTextDesc.text.toString().trim()
        val date = binding.editTextDate.text.toString().trim()
        val time = binding.editTextTime.text.toString().trim()

        if (title.isEmpty() || description.isEmpty() || date.isEmpty() || time.isEmpty()) {
            Snackbar.make(binding.root, "Please fill all required fields.", Snackbar.LENGTH_LONG).show()
            return
        }

        val timeLong = timeStringToTimestamp(time)
        val dateLong = dateStringToTimestamp(date)
        val startTime = combineDateAndTime(dateLong, timeLong)
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Snackbar.make(binding.root, "User is not logged in.", Snackbar.LENGTH_LONG).show()
            return
        }
        progressDialog.show()
        if (photoUri != null) {
            val storageRef = FirebaseStorage.getInstance().reference.child("tasks/${System.currentTimeMillis()}_${photoUri?.lastPathSegment}")
            storageRef.putFile(photoUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                        val photoUrl = uri.toString()
                        saveTaskToDatabase(title, description, date,dateLong, timeLong, startTime, photoUrl, userId)
                    }
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Snackbar.make(binding.root, "Failed to upload photo: ${e.message}", Snackbar.LENGTH_LONG).show()
                }
        } else {
            saveTaskToDatabase(title, description,date, dateLong, timeLong, startTime, "", userId)
        }
    }

    private fun saveTaskToDatabase(title: String, description: String, dateString: String, date: Long,time: Long, startTime: Long, photoUrl: String, userId: String) {
        val task = Task(
            title = title,
            description = description,
            dateString = dateString,
            date = date,
            time = time,
            startTime = startTime,
            photoFileName = photoUrl,
            status = "Pending",
            userId = userId
        )

        viewModel.saveTask(task)
        setTaskReminder(this, task)
        val isReminderAlarmSet = isAlarmSet(this, task.id.hashCode())
        Log.d("TaskAlarmReceiver", "Reminder alarm set: $isReminderAlarmSet")
    }

    private fun isAlarmSet(context: Context, requestCode: Int): Boolean {
        val intent = Intent(context, TaskAlarmReceiver::class.java)
        val pendingIntent = createPendingIntent(context, requestCode, intent)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return (PendingIntent.getBroadcast(context, requestCode, intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE) != null)
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun setTaskReminder(context: Context, task: Task) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val reminderIntent = Intent(context, TaskAlarmReceiver::class.java).apply {
            putExtra("TASK_ID", task.id)
            putExtra("TASK_TITLE", "Reminder: ${task.title} will start in 15 minutes")
        }

        val reminderPendingIntent = createPendingIntent(context, task.id.hashCode(), reminderIntent)

        val reminderTime = task.startTime - 15 * 60 * 1000

        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            reminderTime,
            reminderPendingIntent
        )

        // Notifikasi untuk memperbarui status task menjadi 'Ongoing'
        val ongoingIntent = Intent(context, TaskAlarmReceiver::class.java).apply {
            putExtra("TASK_ID", task.id)
            putExtra("TASK_TITLE", "Task ${task.title} is now Ongoing")
            putExtra("UPDATE_STATUS", true) // Kirim sinyal untuk update status
        }

        val ongoingPendingIntent = createPendingIntent(context, task.id.hashCode() + 1, ongoingIntent)

        // Set alarm pada saat task mulai untuk memperbarui status
        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            task.startTime,
            ongoingPendingIntent
        )
    }

    private fun createPendingIntent(context: Context, requestCode: Int, intent: Intent): PendingIntent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getBroadcast(
                context, requestCode, intent, PendingIntent.FLAG_MUTABLE
            )
        } else {
            PendingIntent.getBroadcast(
                context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
    }


    private fun timeStringToTimestamp(timeString: String, format: String = "HH:mm"): Long {
        val timeFormat = SimpleDateFormat(format, Locale.getDefault())
        return try {
            val date = timeFormat.parse(timeString)
            date?.time ?: 0L
        } catch (e: Exception) {
            0L // Handle parsing error
        }
    }

    private fun dateStringToTimestamp(dateString: String, format: String = "dd/MM/yyyy"): Long {
        val dateFormat = SimpleDateFormat(format, Locale.getDefault())
        return try {
            val date = dateFormat.parse(dateString)
            date?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
    private fun combineDateAndTime(dateMillis: Long, timeMillis: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = dateMillis
            set(Calendar.HOUR_OF_DAY, (timeMillis / (60 * 60 * 1000)).toInt())
            set(Calendar.MINUTE, ((timeMillis % (60 * 60 * 1000)) / (60 * 1000)).toInt())
        }
        return calendar.timeInMillis
    }

}
