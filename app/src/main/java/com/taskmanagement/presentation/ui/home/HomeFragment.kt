package com.taskmanagement.presentation.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.taskmanagement.data.repository.TaskRepository
import com.taskmanagement.databinding.FragmentHomeBinding
import com.taskmanagement.presentation.viewmodel.HomeViewModel
import com.taskmanagement.presentation.viewmodel.HomeViewModelFactory

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = FirebaseAuth.getInstance().currentUser?.uid

        val taskRepository = TaskRepository(FirebaseDatabase.getInstance())
        val factory = HomeViewModelFactory(taskRepository)
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]
        viewModel.loadTasks(userId ?: "")

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewModel.pendingTasks.observe(viewLifecycleOwner) { tasks ->
            binding.tvPendingTasks.text = tasks.joinToString { it.title }
        }

        viewModel.ongoingTasks.observe(viewLifecycleOwner) { tasks ->
            binding.tvOngoingTasks.text = tasks.joinToString { it.title }
        }

        viewModel.doneTasks.observe(viewLifecycleOwner) { tasks ->
            binding.tvDoneTasks.text = tasks.joinToString { it.title }
        }
    }

    private fun setupListeners() {
        binding.btnViewAllPending.setOnClickListener {
            startActivity(Intent(requireContext(), ViewAllTasksActivity::class.java).apply {
                putExtra("isPending", true)
            })
        }


        binding.btnViewAllOngoing.setOnClickListener {
            startActivity(Intent(requireContext(), ViewAllTasksActivity::class.java).apply {
                putExtra("isOngoing", true)
                })
        }

        binding.btnViewAllDone.setOnClickListener {
            startActivity(Intent(requireContext(), ViewAllTasksActivity::class.java).apply {
                putExtra("isDone", true)
                })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
