package com.taskmanagement.presentation.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.taskmanagement.data.model.Task
import com.taskmanagement.databinding.ItemTaskBinding

class TaskAdapter(private val onItemClick: (Task) -> Unit) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private val tasks = mutableListOf<Task>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.bind(task, onItemClick)
    }

    override fun getItemCount(): Int = tasks.size

    fun setTasks(newTasks: List<Task>) {
        tasks.clear()
        tasks.addAll(newTasks)
        notifyDataSetChanged()
    }

    inner class TaskViewHolder(private val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: Task, onItemClick: (Task) -> Unit) {
            binding.textViewTitle.text = task.title
            binding.textViewDescription.text = task.description
            binding.textViewStatus.text = task.status
            if (task.status == "Pending") {
                binding.textViewStatus.setTextColor(Color.YELLOW)
            }
            else if (task.status == "Ongoing") {
                binding.textViewStatus.setTextColor(Color.BLUE)
            }
            else {
                binding.textViewStatus.setTextColor(Color.GREEN)
            }


            itemView.setOnClickListener {
                onItemClick(task)
            }
        }
    }
}
