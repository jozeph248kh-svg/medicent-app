package com.medreminder.app.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.medreminder.app.data.Medication

object NotificationHelper {
    const val CHANNEL_MEDICATION = "medication_channel"
    const val CHANNEL_GENERAL = "general_channel"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val medChannel = NotificationChannel(
                CHANNEL_MEDICATION, "یادآوری دارو", NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableVibration(true)
                description = "یادآوری زمان مصرف دارو - تا تایید تکرار می‌شود"
            }
            val generalChannel = NotificationChannel(
                CHANNEL_GENERAL, "یادآوری‌های عمومی", NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "نوبت پزشک، کارهای روزانه و هشدار موجودی دارو"
            }
            nm.createNotificationChannel(medChannel)
            nm.createNotificationChannel(generalChannel)
        }
    }

    fun showDoseNotification(context: Context, medication: Medication, timeIndex: Int, doseTime: String, dateKey: String) {
        val takenIntent = Intent(context, TakenActionReceiver::class.java).apply {
            putExtra(AlarmScheduler.EXTRA_MED_ID, medication.id)
            putExtra(AlarmScheduler.EXTRA_TIME_INDEX, timeIndex)
            putExtra(AlarmScheduler.EXTRA_DOSE_TIME, doseTime)
            putExtra(AlarmScheduler.EXTRA_DATE_KEY, dateKey)
        }
        val takenPi = PendingIntent.getBroadcast(
            context, (medication.id * 1000 + timeIndex).toInt() + 900000, takenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val fullScreenIntent = Intent(context, AlarmRingActivity::class.java).apply {
            putExtra(AlarmScheduler.EXTRA_MED_ID, medication.id)
            putExtra(AlarmScheduler.EXTRA_TIME_INDEX, timeIndex)
            putExtra(AlarmScheduler.EXTRA_DOSE_TIME, doseTime)
            putExtra(AlarmScheduler.EXTRA_DATE_KEY, dateKey)
            putExtra("med_name", medication.name)
            putExtra("dosage_note", medication.dosageNote)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val fullScreenPi = PendingIntent.getActivity(
            context, (medication.id * 1000 + timeIndex).toInt(), fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_MEDICATION)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("وقت مصرف دارو: ${medication.name}")
            .setContentText("${medication.dosageNote} — ساعت $doseTime — تا تایید نکنید هر ۵ دقیقه یادآوری می‌شود")
            .setStyle(NotificationCompat.BigTextStyle())
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPi, true)
            .setContentIntent(fullScreenPi)
            .addAction(0, "دارو را خوردم ✅", takenPi)
            .setAutoCancel(false)
            .setOngoing(true)
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify((medication.id * 1000 + timeIndex).toInt(), notification)
    }

    fun cancelDoseNotification(context: Context, medId: Long, timeIndex: Int) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel((medId * 1000 + timeIndex).toInt())
    }

    fun showLowStockNotification(context: Context, medication: Medication) {
        val notification = NotificationCompat.Builder(context, CHANNEL_GENERAL)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("موجودی دارو رو به اتمام است: ${medication.name}")
            .setContentText("موجودی فعلی: ${medication.quantityRemaining} — لطفاً تهیه کنید")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify((5_000_000 + medication.id).toInt(), notification)
    }

    fun showGeneralNotification(context: Context, id: Int, title: String, text: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_GENERAL)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(id, notification)
    }
}
