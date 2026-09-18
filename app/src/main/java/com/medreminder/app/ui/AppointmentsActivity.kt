package com.medreminder.app.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.medreminder.app.alarm.AlarmScheduler
import com.medreminder.app.data.Appointment
import com.medreminder.app.data.AppDatabase
import com.medreminder.app.databinding.ActivityAppointmentsBinding
import kotlinx.coroutines.launch

class AppointmentsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAppointmentsBinding
    private lateinit var adapter: AppointmentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppointmentsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = AppointmentAdapter(mutableListOf()) { deleteAppointment(it) }
        binding.recyclerAppointments.layoutManager = LinearLayoutManager(this)
        binding.recyclerAppointments.adapter = adapter

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, AddAppointmentActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        load()
    }

    private fun load() {
        lifecycleScope.launch {
            val list = AppDatabase.getInstance(this@AppointmentsActivity).appointmentDao().getAll()
            adapter.updateData(list)
            binding.textEmpty.visibility = if (list.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        }
    }

    private fun deleteAppointment(appointment: Appointment) {
        AlertDialog.Builder(this)
            .setTitle("حذف نوبت")
            .setMessage("آیا از حذف «${appointment.title}» مطمئن هستید؟")
            .setPositiveButton("حذف") { _, _ ->
                lifecycleScope.launch {
                    AlarmScheduler.cancelAppointment(this@AppointmentsActivity, appointment.id)
                    AppDatabase.getInstance(this@AppointmentsActivity).appointmentDao().delete(appointment)
                    load()
                }
            }
            .setNegativeButton("انصراف", null)
            .show()
    }
}
