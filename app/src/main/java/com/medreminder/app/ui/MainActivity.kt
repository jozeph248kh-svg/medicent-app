package com.medreminder.app.ui

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.medreminder.app.alarm.AlarmScheduler
import com.medreminder.app.alarm.DoseActions
import com.medreminder.app.alarm.NotificationHelper
import com.medreminder.app.data.AppDatabase
import com.medreminder.app.data.Medication
import com.medreminder.app.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: MedicationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        NotificationHelper.createChannels(this)
        requestPermissionsIfNeeded()

        adapter = MedicationAdapter(
            mutableListOf(),
            onMarkTaken = { showMarkTakenDialog(it) },
            onDelete = { deleteMedication(it) }
        )
        binding.recyclerMedications.layoutManager = LinearLayoutManager(this)
        binding.recyclerMedications.adapter = adapter

        binding.fabAddMedication.setOnClickListener {
            startActivity(Intent(this, AddMedicationActivity::class.java))
        }
        binding.btnAppointments.setOnClickListener {
            startActivity(Intent(this, AppointmentsActivity::class.java))
        }
        binding.btnTasks.setOnClickListener {
            startActivity(Intent(this, TasksActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadMedications()
    }

    private fun loadMedications() {
        lifecycleScope.launch {
            val db = AppDatabase.getInstance(this@MainActivity)
            val meds = db.medicationDao().getAll()
            adapter.updateData(meds)
            binding.textEmpty.visibility = if (meds.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        }
    }

    private fun showMarkTakenDialog(medication: Medication) {
        val times = medication.timesList()
        if (times.isEmpty()) return
        lifecycleScope.launch {
            val db = AppDatabase.getInstance(this@MainActivity)
            val dateKey = AlarmScheduler.todayKey()
            val statuses = times.map { time ->
                val log = db.medicationDao().getDoseLog(medication.id, time, dateKey)
                time to (log?.taken == true)
            }
            val labels = statuses.map { (time, taken) -> if (taken) "$time (خورده شده ✅)" else time }.toTypedArray()

            AlertDialog.Builder(this@MainActivity)
                .setTitle("کدام نوبت ${medication.name} را مصرف کردید؟")
                .setItems(labels) { _, which ->
                    val (time, taken) = statuses[which]
                    if (!taken) {
                        val timeIndex = times.indexOf(time)
                        lifecycleScope.launch {
                            DoseActions.markTaken(this@MainActivity, medication.id, timeIndex, time, dateKey)
                            loadMedications()
                        }
                    }
                }
                .setNegativeButton("انصراف", null)
                .show()
        }
    }

    private fun deleteMedication(medication: Medication) {
        AlertDialog.Builder(this)
            .setTitle("حذف دارو")
            .setMessage("آیا از حذف «${medication.name}» مطمئن هستید؟")
            .setPositiveButton("حذف") { _, _ ->
                lifecycleScope.launch {
                    AlarmScheduler.cancelMedication(this@MainActivity, medication.id, medication.timesList().size)
                    AppDatabase.getInstance(this@MainActivity).medicationDao().delete(medication)
                    loadMedications()
                }
            }
            .setNegativeButton("انصراف", null)
            .show()
    }

    private fun requestPermissionsIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!am.canScheduleExactAlarms()) {
                try {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                } catch (_: Exception) {
                }
            }
        }
    }
}
