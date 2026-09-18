package com.medreminder.app.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.medreminder.app.alarm.AlarmScheduler
import com.medreminder.app.data.AppDatabase
import com.medreminder.app.data.DailyTask
import com.medreminder.app.databinding.ActivityTasksBinding
import kotlinx.coroutines.launch

class TasksActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTasksBinding
    private lateinit var adapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTasksBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        adapter = TaskAdapter(
            mutableListOf(),
            onToggleActive = { task, active -> toggleActive(task, active) },
            onDelete = { deleteTask(it) }
        )
        binding.recyclerTasks.layoutManager = LinearLayoutManager(this)
        binding.recyclerTasks.adapter = adapter

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, AddTaskActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        load()
    }

    private fun load() {
        lifecycleScope.launch {
            val list = AppDatabase.getInstance(this@TasksActivity).taskDao().getAll()
            adapter.updateData(list)
            binding.textEmpty.visibility = if (list.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        }
    }

    private fun toggleActive(task: DailyTask, active: Boolean) {
        lifecycleScope.launch {
            val updated = task.copy(active = active)
            AppDatabase.getInstance(this@TasksActivity).taskDao().update(updated)
            if (active) {
                AlarmScheduler.scheduleTask(this@TasksActivity, updated)
            } else {
                AlarmScheduler.cancelTask(this@TasksActivity, task.id)
            }
        }
    }

    private fun deleteTask(task: DailyTask) {
        AlertDialog.Builder(this)
            .setTitle("حذف کار")
            .setMessage("آیا از حذف «${task.title}» مطمئن هستید؟")
            .setPositiveButton("حذف") { _, _ ->
                lifecycleScope.launch {
                    AlarmScheduler.cancelTask(this@TasksActivity, task.id)
                    AppDatabase.getInstance(this@TasksActivity).taskDao().delete(task)
                    load()
                }
            }
            .setNegativeButton("انصراف", null)
            .show()
    }
}
