package com.medreminder.app.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.medreminder.app.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppointmentAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val apptId = intent.getLongExtra(AlarmScheduler.EXTRA_APPT_ID, -1)
        if (apptId == -1L) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val appt = db.appointmentDao().getById(apptId) ?: return@launch
                NotificationHelper.showGeneralNotification(
                    context, (2_000_000 + apptId).toInt(),
                    "یادآوری نوبت پزشک: ${appt.title}",
                    "دکتر: ${appt.doctorName} — ${appt.notes}"
                )
            } finally {
                pendingResult.finish()
            }
        }
    }
}
