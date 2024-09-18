package com.taskmanagement.presentation.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.taskmanagement.R
import com.taskmanagement.data.repository.TaskRepository
import com.taskmanagement.databinding.ActivityViewAllTaskBinding
import com.taskmanagement.presentation.adapter.TaskAdapter
import com.taskmanagement.presentation.ui.task.DetailTaskActivity
import com.taskmanagement.presentation.viewmodel.HomeViewModel
import com.taskmanagement.presentation.viewmodel.HomeViewModelFactory

class ViewAllTasksActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewAllTaskBinding
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var viewModel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityViewAllTaskBinding.inflate(layoutInflater)
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

        val userId = FirebaseAuth.getInstance().currentUser?.uid

        val taskRepository = TaskRepository(FirebaseDatabase.getInstance())
        val factory = HomeViewModelFactory(taskRepository)
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]
        viewModel.loadTasks(userId ?: "")

        setupRecyclerView()
        setupObservers()

    }
    private fun setupObservers() {
        val pending = intent.getBooleanExtra("isPending", false)
        val ongoing = intent.getBooleanExtra("isOngoing", false)
        val completed = intent.getBooleanExtra("isDone", false)

        if (pending) {
            binding.toolbar.title = "All Pending Tasks"
            viewModel.pendingTasks.observe(this) { tasks ->
                taskAdapter.setTasks(tasks)
            }
        } else if (ongoing) {
            binding.toolbar.title = "All Ongoing Tasks"
            viewModel.ongoingTasks.observe(this) { tasks ->
                taskAdapter.setTasks(tasks)
            }
        } else if (completed) {
            binding.toolbar.title = "All Done Tasks"
            viewModel.doneTasks.observe(this) { tasks ->
                taskAdapter.setTasks(tasks)
            }
        }
    }
    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter { task ->
            val intent = Intent(this, DetailTaskActivity::class.java)
            intent.putExtra("idTask", task.id)
            startActivity(intent)
        }
        binding.rvTaskList.adapter = taskAdapter
        binding.rvTaskList.layoutManager = LinearLayoutManager(this)
    }
}