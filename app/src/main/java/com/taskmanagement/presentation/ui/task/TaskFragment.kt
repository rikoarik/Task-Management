package com.taskmanagement.presentation.ui.task

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.taskmanagement.databinding.FragmentTaskBinding
import com.taskmanagement.presentation.adapter.TaskAdapter
import com.taskmanagement.presentation.viewmodel.TaskViewModel
import com.taskmanagement.presentation.viewmodel.TaskViewModelFactory
import com.taskmanagement.data.model.Task
import com.taskmanagement.common.utils.EventDecorator
import com.taskmanagement.data.repository.TaskRepository
import java.text.SimpleDateFormat
import java.util.*

class TaskFragment : Fragment() {

    private var _binding: FragmentTaskBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: TaskViewModel
    private lateinit var taskAdapter: TaskAdapter
    private val taskDates = mutableSetOf<CalendarDay>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("DefaultLocale")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val taskRepository = TaskRepository(FirebaseDatabase.getInstance())
        val factory = TaskViewModelFactory(taskRepository)
        viewModel = ViewModelProvider(this, factory).get(TaskViewModel::class.java)

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val selectedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        userId?.let {
            viewModel.fetchTasks(it)
            viewModel.fetchTasksByDate(it, selectedDate)
        }

        setupRecyclerView()
        setupObservers()

        binding.fabAddTask.setOnClickListener {
            startActivity(Intent(requireContext(), AddTaskActivity::class.java))
        }

        selectToday()
        binding.calendarView.setOnDateChangedListener { widget, date, selected ->
            val selectedDate = String.format("%d/%d/%d", date.day, date.month + 1, date.year)
            userId?.let {
                viewModel.fetchTasksByDate(it, selectedDate)
            }

        }
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter { task ->
            val intent = Intent(requireContext(), DetailTaskActivity::class.java)
            intent.putExtra("idTask", task.id)
            intent.putExtra("statusTask", task.status)
            startActivity(intent)
        }
        binding.rvTaskList.adapter = taskAdapter
        binding.rvTaskList.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupObservers() {
        viewModel.tasks.observe(viewLifecycleOwner) { tasks ->
            if (tasks.isNotEmpty()) {
                displayTasksOnCalendar(tasks)
            } else {
                Toast.makeText(requireContext(), "No tasks available", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.tasksByDate.observe(viewLifecycleOwner) { tasks ->
            if (tasks.isNotEmpty()) {
                taskAdapter.setTasks(tasks)
            } else {
                Toast.makeText(requireContext(), "No tasks available for the selected date", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayTasksOnCalendar(tasks: List<Task>) {
        taskDates.clear()
        val taskMap = tasks.groupBy { formatDate(it.date) }
        taskMap.keys.forEach { dateString ->
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateString)
            date?.let {
                val calendarDay = CalendarDay.from(it)
                taskDates.add(calendarDay)
            }
        }
        binding.calendarView.addDecorator(EventDecorator(Color.RED, taskDates))
    }

    private fun formatDate(timestamp: Long): String {
        val date = Date(timestamp)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(date)
    }

    private fun selectToday() {
        binding.calendarView.selectedDate = CalendarDay.today()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
