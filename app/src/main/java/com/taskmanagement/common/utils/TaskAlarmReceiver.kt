package com.taskmanagement.common.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.firebase.database.FirebaseDatabase
import androidx.core.app.NotificationCompat
import com.taskmanagement.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.taskmanagement.data.repository.TaskRepository
import com.taskmanagement.presentation.viewmodel.TaskViewModel

class TaskAlarmReceiver : BroadcastReceiver() {

    private lateinit var repository: TaskRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (!::repository.isInitialized) {
            val database = FirebaseDatabase.getInstance()
            repository = TaskRepository(database)
        }

        val taskId = intent.getStringExtra("TASK_ID")
        val taskTitle = intent.getStringExtra("TASK_TITLE")
        val updateStatus = intent.getBooleanExtra("UPDATE_STATUS", false)
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null) {
            Log.e("TaskAlarmReceiver", "User is not logged in, cannot update task status!")
            return
        }

        if (taskId != null && updateStatus) {
            Log.d("TaskAlarmReceiver", "Updating status for taskId: $taskId to 'Ongoing'")
            repository.updateStatusTask(userId, taskId, "Ongoing")
            taskTitle?.let { sendNotification(context, it) }

        } else {
            if (taskTitle != null) {
                Log.d("TaskAlarmReceiver", "Sending notification for task: $taskTitle")
                sendNotification(context, taskTitle)
            } else {
                Log.e("TaskAlarmReceiver", "Task title is null, cannot send notification!")
            }
        }
    }

    private fun sendNotification(context: Context?, taskTitle: String) {
        if (context == null) {
            Log.e("TaskAlarmReceiver", "Context is null, cannot send notification!")
            return
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "TASK_CHANNEL_ID"
        val channelName = "Task Notifications"
        val channelDescription = "Channel for task notifications"
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
            description = channelDescription
        }
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(context, "TASK_CHANNEL_ID")
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle("Task Reminder")
            .setContentText("Task $taskTitle is now Ongoing!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(taskTitle.hashCode(), notification)
    }
}
