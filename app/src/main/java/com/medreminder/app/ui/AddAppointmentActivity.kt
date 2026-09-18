package com.medreminder.app.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.medreminder.app.alarm.AlarmScheduler
import com.medreminder.app.data.AppDatabase
import com.medreminder.app.data.Appointment
import com.medreminder.app.databinding.ActivityAddAppointmentBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddAppointmentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddAppointmentBinding
    private val calendar = Calendar.getInstance()
    private var dateSet = false
    private var timeSet = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddAppointmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnPickDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)
                dateSet = true
                updateSelectedText()
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.btnPickTime.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(this, { _, hour, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)
                timeSet = true
                updateSelectedText()
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        binding.btnSave.setOnClickListener { save() }
    }

    private fun updateSelectedText() {
        if (dateSet && timeSet) {
            val sdf = SimpleDateFormat("yyyy/MM/dd - HH:mm", Locale.getDefault())
            binding.textSelectedDateTime.text = sdf.format(calendar.time)
        }
    }

    private fun save() {
        val title = binding.editTitle.text.toString().trim()
        val doctor = binding.editDoctor.text.toString().trim()
        val notes = binding.editNotes.text.toString().trim()

        if (title.isEmpty()) {
            Toast.makeText(this, "عنوان نوبت را وارد کنید", Toast.LENGTH_SHORT).show()
            return
        }
        if (!dateSet || !timeSet) {
            Toast.makeText(this, "تاریخ و ساعت نوبت را انتخاب کنید", Toast.LENGTH_SHORT).show()
            return
        }
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            Toast.makeText(this, "زمان انتخابی باید در آینده باشد", Toast.LENGTH_SHORT).show()
            return
        }

        val appointment = Appointment(
            title = title,
            doctorName = doctor,
            dateTimeMillis = calendar.timeInMillis,
            notes = notes
        )

        lifecycleScope.launch {
            val db = AppDatabase.getInstance(this@AddAppointmentActivity)
            val id = db.appointmentDao().insert(appointment)
            AlarmScheduler.scheduleAppointment(this@AddAppointmentActivity, appointment.copy(id = id))
            Toast.makeText(this@AddAppointmentActivity, "نوبت ذخیره شد", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
