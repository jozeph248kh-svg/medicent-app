package com.medreminder.app.alarm

import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.medreminder.app.databinding.ActivityAlarmRingBinding

class AlarmRingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAlarmRingBinding
    private var medId: Long = -1
    private var timeIndex: Int = -1
    private var doseTime: String = ""
    private var dateKey: String = ""
    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmRingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        medId = intent.getLongExtra(AlarmScheduler.EXTRA_MED_ID, -1)
        timeIndex = intent.getIntExtra(AlarmScheduler.EXTRA_TIME_INDEX, -1)
        doseTime = intent.getStringExtra(AlarmScheduler.EXTRA_DOSE_TIME) ?: ""
        dateKey = intent.getStringExtra(AlarmScheduler.EXTRA_DATE_KEY) ?: ""
        val medName = intent.getStringExtra("med_name") ?: ""
        val dosageNote = intent.getStringExtra("dosage_note") ?: ""

        binding.textMedName.text = medName
        binding.textDosageNote.text = dosageNote
        binding.textDoseTime.text = "ساعت مصرف: $doseTime"

        binding.btnTaken.setOnClickListener {
            val takenIntent = Intent(this, TakenActionReceiver::class.java).apply {
                putExtra(AlarmScheduler.EXTRA_MED_ID, medId)
                putExtra(AlarmScheduler.EXTRA_TIME_INDEX, timeIndex)
                putExtra(AlarmScheduler.EXTRA_DOSE_TIME, doseTime)
                putExtra(AlarmScheduler.EXTRA_DATE_KEY, dateKey)
            }
            sendBroadcast(takenIntent)
            stopAlarmSound()
            finish()
        }

        binding.btnLater.setOnClickListener {
            stopAlarmSound()
            NotificationHelper.cancelDoseNotification(this, medId, timeIndex)
            finish()
        }

        playAlarmSound()
    }

    private fun playAlarmSound() {
        try {
            val uri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ringtone = RingtoneManager.getRingtone(this, uri)
            ringtone?.play()
            vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            val pattern = longArrayOf(0, 500, 500)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
        } catch (_: Exception) {
        }
    }

    private fun stopAlarmSound() {
        ringtone?.stop()
        vibrator?.cancel()
    }

    override fun onDestroy() {
        stopAlarmSound()
        super.onDestroy()
    }
}
