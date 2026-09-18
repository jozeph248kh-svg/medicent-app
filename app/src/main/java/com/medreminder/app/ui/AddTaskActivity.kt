package com.medreminder.app.ui

import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.medreminder.app.alarm.AlarmScheduler
import com.medreminder.app.data.AppDatabase
import com.medreminder.app.data.DailyTask
import com.medreminder.app.databinding.ActivityAddTaskBinding
import kotlinx.coroutines.launch
import java.util.Calendar

class AddTaskActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddTaskBinding
    private var selectedTime: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnPickTime.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(this, { _, hour, minute ->
                selectedTime = String.format("%02d:%02d", hour, minute)
                binding.textSelectedTime.text = selectedTime
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        binding.btnSave.setOnClickListener { save() }
    }

    private fun save() {
        val title = binding.editTitle.text.toString().trim()
        val time = selectedTime

        if (title.isEmpty()) {
            Toast.makeText(this, "عنوان کار را وارد کنید", Toast.LENGTH_SHORT).show()
            return
        }
        if (time == null) {
            Toast.makeText(this, "ساعت یادآوری را انتخاب کنید", Toast.LENGTH_SHORT).show()
            return
        }

        val task = DailyTask(title = title, time = time, active = true)

        lifecycleScope.launch {
            val db = AppDatabase.getInstance(this@AddTaskActivity)
            val id = db.taskDao().insert(task)
            AlarmScheduler.scheduleTask(this@AddTaskActivity, task.copy(id = id))
            Toast.makeText(this@AddTaskActivity, "کار ذخیره شد", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
