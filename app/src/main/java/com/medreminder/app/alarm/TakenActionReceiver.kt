package com.medreminder.app.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TakenActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val medId = intent.getLongExtra(AlarmScheduler.EXTRA_MED_ID, -1)
        val timeIndex = intent.getIntExtra(AlarmScheduler.EXTRA_TIME_INDEX, -1)
        val doseTime = intent.getStringExtra(AlarmScheduler.EXTRA_DOSE_TIME) ?: return
        val dateKey = intent.getStringExtra(AlarmScheduler.EXTRA_DATE_KEY) ?: return
        if (medId == -1L || timeIndex == -1) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                DoseActions.markTaken(context, medId, timeIndex, doseTime, dateKey)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
