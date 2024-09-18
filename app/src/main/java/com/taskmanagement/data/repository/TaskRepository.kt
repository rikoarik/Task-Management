package com.taskmanagement.data.repository

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.taskmanagement.data.model.Task
import kotlinx.coroutines.tasks.await

class TaskRepository(private val firebaseDatabase: FirebaseDatabase) {

    fun saveTask(task: Task) {
        val userId = task.userId
        if (userId.isNotEmpty()) {
            val taskRef = firebaseDatabase.getReference("tasks/$userId").push()
            task.id = taskRef.key ?: ""
            taskRef.setValue(task)
        }
    }


    fun updateStatusTask(userId: String, taskId: String, newStatus: String) {
        val taskRef = firebaseDatabase.getReference("tasks/$userId").child(taskId)
        taskRef.child("status").setValue(newStatus)
    }

    suspend fun getTasks(userId: String): List<Task> {
        val tasksRef = firebaseDatabase.getReference("tasks/$userId")
        val snapshot = tasksRef.get().await()
        val taskList = mutableListOf<Task>()

        if (snapshot.exists()) {
            for (dataSnapshot in snapshot.children) {
                val task = dataSnapshot.getValue(Task::class.java)
                task?.let { taskList.add(it) }
            }
        }
        return taskList
    }
    suspend fun getTasksByDate(userId: String, selectedDate: String): List<Task> {
        val tasksRef = firebaseDatabase.getReference("tasks/$userId")

        val snapshot = try {
            tasksRef.orderByChild("dateString").equalTo(selectedDate).get().await()
        } catch (e: Exception) {
            Log.e("TaskRepository", "Error fetching tasks: ${e.message}")
            return emptyList()
        }

        val taskList = mutableListOf<Task>()

        if (snapshot.exists()) {
            for (dataSnapshot in snapshot.children) {
                val task = dataSnapshot.getValue(Task::class.java)
                task?.let { taskList.add(it) }
            }
        } else {
            Log.d("TaskRepository", "No tasks found for date: $selectedDate")
        }

        return taskList
    }


    suspend fun getTaskById(userId: String, idTask: String): Task? {
        val tasksRef = firebaseDatabase.getReference("tasks/$userId")
        val snapshot = tasksRef.get().await()

        if (snapshot.exists()) {
            for (dataSnapshot in snapshot.children) {
                val task = dataSnapshot.getValue(Task::class.java)
                if (task != null && task.id == idTask) {
                    return task
                }
            }
        }

        return null
    }

    suspend fun updateTaskById(userId: String, idTask: String, updatedTask: Task): Result<Unit> {
        return try {
            val tasksRef = firebaseDatabase.getReference("tasks/$userId")
            val taskRef = tasksRef.child(idTask)

            taskRef.setValue(updatedTask)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }



}
