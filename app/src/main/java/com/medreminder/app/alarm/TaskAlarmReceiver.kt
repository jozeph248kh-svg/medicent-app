package com.medreminder.app.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.medreminder.app.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(AlarmScheduler.EXTRA_TASK_ID, -1)
        if (taskId == -1L) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val task = db.taskDao().getById(taskId) ?: return@launch
                NotificationHelper.showGeneralNotification(
                    context, (3_000_000 + taskId).toInt(),
                    "یادآوری کار روزانه", task.title
                )
                AlarmScheduler.scheduleTask(context, task)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
