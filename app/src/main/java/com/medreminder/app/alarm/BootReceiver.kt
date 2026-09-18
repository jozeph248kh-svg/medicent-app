package com.medreminder.app.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.medreminder.app.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                db.medicationDao().getAll().forEach { AlarmScheduler.scheduleMedication(context, it) }
                db.taskDao().getAll().forEach { if (it.active) AlarmScheduler.scheduleTask(context, it) }
                db.appointmentDao().getAll().forEach {
                    if (it.dateTimeMillis > System.currentTimeMillis()) AlarmScheduler.scheduleAppointment(context, it)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
