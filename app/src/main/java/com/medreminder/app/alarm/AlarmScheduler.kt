package com.medreminder.app.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.medreminder.app.data.Appointment
import com.medreminder.app.data.DailyTask
import com.medreminder.app.data.Medication
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object AlarmScheduler {

    const val EXTRA_TYPE = "type"
    const val EXTRA_MED_ID = "med_id"
    const val EXTRA_TIME_INDEX = "time_index"
    const val EXTRA_DOSE_TIME = "dose_time"
    const val EXTRA_DATE_KEY = "date_key"
    const val EXTRA_APPT_ID = "appt_id"
    const val EXTRA_TASK_ID = "task_id"

    const val TYPE_MEDICATION = "medication"
    const val TYPE_MEDICATION_NAG = "medication_nag"

    fun todayKey(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private fun alarmManager(context: Context) =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleMedication(context: Context, medication: Medication) {
        medication.timesList().forEachIndexed { index, time ->
            scheduleMedicationTime(context, medication.id, index, time)
        }
    }

    fun scheduleMedicationTime(context: Context, medId: Long, timeIndex: Int, time: String) {
        val parts = time.split(":")
        if (parts.size != 2) return
        val hour = parts[0].toIntOrNull() ?: return
        val minute = parts[1].toIntOrNull() ?: return

        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        if (cal.timeInMillis <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        val intent = Intent(context, MedicationAlarmReceiver::class.java).apply {
            putExtra(EXTRA_TYPE, TYPE_MEDICATION)
            putExtra(EXTRA_MED_ID, medId)
            putExtra(EXTRA_TIME_INDEX, timeIndex)
            putExtra(EXTRA_DOSE_TIME, time)
        }
        val requestCode = (medId * 1000 + timeIndex).toInt()
        val pi = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        setExact(context, cal.timeInMillis, pi)
    }

    fun scheduleNag(context: Context, medId: Long, timeIndex: Int, doseTime: String, dateKey: String, delayMinutes: Long = 5) {
        val intent = Intent(context, MedicationAlarmReceiver::class.java).apply {
            putExtra(EXTRA_TYPE, TYPE_MEDICATION_NAG)
            putExtra(EXTRA_MED_ID, medId)
            putExtra(EXTRA_TIME_INDEX, timeIndex)
            putExtra(EXTRA_DOSE_TIME, doseTime)
            putExtra(EXTRA_DATE_KEY, dateKey)
        }
        val requestCode = (medId * 1000 + timeIndex).toInt() + 500000
        val pi = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val triggerAt = System.currentTimeMillis() + delayMinutes * 60_000
        setExact(context, triggerAt, pi)
    }

    fun cancelNag(context: Context, medId: Long, timeIndex: Int) {
        val intent = Intent(context, MedicationAlarmReceiver::class.java)
        val requestCode = (medId * 1000 + timeIndex).toInt() + 500000
        val pi = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager(context).cancel(pi)
    }

    fun cancelMedication(context: Context, medId: Long, timeCount: Int) {
        for (i in 0 until timeCount) {
            val intent = Intent(context, MedicationAlarmReceiver::class.java)
            val requestCode = (medId * 1000 + i).toInt()
            val pi = PendingIntent.getBroadcast(
                context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager(context).cancel(pi)
            cancelNag(context, medId, i)
        }
    }

    fun scheduleAppointment(context: Context, appointment: Appointment) {
        if (appointment.dateTimeMillis <= System.currentTimeMillis()) return
        val intent = Intent(context, AppointmentAlarmReceiver::class.java).apply {
            putExtra(EXTRA_APPT_ID, appointment.id)
        }
        val requestCode = (2_000_000 + appointment.id).toInt()
        val pi = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        setExact(context, appointment.dateTimeMillis, pi)
    }

    fun cancelAppointment(context: Context, appointmentId: Long) {
        val intent = Intent(context, AppointmentAlarmReceiver::class.java)
        val requestCode = (2_000_000 + appointmentId).toInt()
        val pi = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager(context).cancel(pi)
    }

    fun scheduleTask(context: Context, task: DailyTask) {
        if (!task.active) return
        val parts = task.time.split(":")
        if (parts.size != 2) return
        val hour = parts[0].toIntOrNull() ?: return
        val minute = parts[1].toIntOrNull() ?: return
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        if (cal.timeInMillis <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        val intent = Intent(context, TaskAlarmReceiver::class.java).apply {
            putExtra(EXTRA_TASK_ID, task.id)
        }
        val requestCode = (3_000_000 + task.id).toInt()
        val pi = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        setExact(context, cal.timeInMillis, pi)
    }

    fun cancelTask(context: Context, taskId: Long) {
        val intent = Intent(context, TaskAlarmReceiver::class.java)
        val requestCode = (3_000_000 + taskId).toInt()
        val pi = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager(context).cancel(pi)
    }

    private fun setExact(context: Context, triggerAt: Long, pi: PendingIntent) {
        val am = alarmManager(context)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            } else {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            }
        } catch (e: SecurityException) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }
    }
}
