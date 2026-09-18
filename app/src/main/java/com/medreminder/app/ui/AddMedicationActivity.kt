package com.medreminder.app.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.medreminder.app.alarm.AlarmScheduler
import com.medreminder.app.data.AppDatabase
import com.medreminder.app.data.Medication
import com.medreminder.app.databinding.ActivityAddMedicationBinding
import kotlinx.coroutines.launch

class AddMedicationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddMedicationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMedicationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSave.setOnClickListener { save() }
    }

    private fun save() {
        val name = binding.editName.text.toString().trim()
        val dosageNote = binding.editDosageNote.text.toString().trim()
        val timesRaw = binding.editTimes.text.toString().trim()
        val quantity = binding.editQuantity.text.toString().trim().toIntOrNull()
        val threshold = binding.editThreshold.text.toString().trim().toIntOrNull() ?: 5
        val dosePerIntake = binding.editDosePerIntake.text.toString().trim().toIntOrNull() ?: 1

        if (name.isEmpty()) {
            Toast.makeText(this, "نام دارو را وارد کنید", Toast.LENGTH_SHORT).show()
            return
        }
        val validTimes = timesRaw.split(",").map { it.trim() }.filter { it.matches(Regex("^\\d{1,2}:\\d{2}$")) }
        if (validTimes.isEmpty()) {
            Toast.makeText(this, "حداقل یک ساعت معتبر وارد کنید (مثلاً 08:00)", Toast.LENGTH_SHORT).show()
            return
        }
        if (quantity == null) {
            Toast.makeText(this, "تعداد موجودی را وارد کنید", Toast.LENGTH_SHORT).show()
            return
        }

        val medication = Medication(
            name = name,
            dosageNote = dosageNote,
            timesCsv = validTimes.joinToString(","),
            quantityRemaining = quantity,
            lowStockThreshold = threshold,
            dosePerIntake = dosePerIntake
        )

        lifecycleScope.launch {
            val db = AppDatabase.getInstance(this@AddMedicationActivity)
            val id = db.medicationDao().insert(medication)
            AlarmScheduler.scheduleMedication(this@AddMedicationActivity, medication.copy(id = id))
            Toast.makeText(this@AddMedicationActivity, "دارو ذخیره و یادآوری تنظیم شد", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
