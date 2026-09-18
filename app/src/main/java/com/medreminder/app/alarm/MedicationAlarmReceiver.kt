package com.medreminder.app.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.medreminder.app.data.AppDatabase
import com.medreminder.app.data.DoseLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MedicationAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val medId = intent.getLongExtra(AlarmScheduler.EXTRA_MED_ID, -1)
        val timeIndex = intent.getIntExtra(AlarmScheduler.EXTRA_TIME_INDEX, -1)
        val doseTime = intent.getStringExtra(AlarmScheduler.EXTRA_DOSE_TIME) ?: return
        val type = intent.getStringExtra(AlarmScheduler.EXTRA_TYPE) ?: return
        if (medId == -1L || timeIndex == -1) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val medication = db.medicationDao().getById(medId) ?: return@launch

                val dateKey = intent.getStringExtra(AlarmScheduler.EXTRA_DATE_KEY) ?: AlarmScheduler.todayKey()
                var log = db.medicationDao().getDoseLog(medId, doseTime, dateKey)
                if (log == null) {
                    val newId = db.medicationDao().insertDoseLog(
                        DoseLog(medicationId = medId, doseTime = doseTime, dateKey = dateKey, taken = false)
                    )
                    log = DoseLog(id = newId, medicationId = medId, doseTime = doseTime, dateKey = dateKey, taken = false)
                }

                if (log.taken) {
                    if (type == AlarmScheduler.TYPE_MEDICATION) {
                        AlarmScheduler.scheduleMedicationTime(context, medId, timeIndex, doseTime)
                    }
                    return@launch
                }

                NotificationHelper.showDoseNotification(context, medication, timeIndex, doseTime, dateKey)
                AlarmScheduler.scheduleNag(context, medId, timeIndex, doseTime, dateKey)

                if (type == AlarmScheduler.TYPE_MEDICATION) {
                    AlarmScheduler.scheduleMedicationTime(context, medId, timeIndex, doseTime)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
