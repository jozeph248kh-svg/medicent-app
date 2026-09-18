package com.medreminder.app.alarm

import android.content.Context
import com.medreminder.app.data.AppDatabase

object DoseActions {
    suspend fun markTaken(context: Context, medId: Long, timeIndex: Int, doseTime: String, dateKey: String) {
        val db = AppDatabase.getInstance(context)
        var log = db.medicationDao().getDoseLog(medId, doseTime, dateKey)
        if (log == null) {
            val newId = db.medicationDao().insertDoseLog(
                com.medreminder.app.data.DoseLog(medicationId = medId, doseTime = doseTime, dateKey = dateKey, taken = false)
            )
            log = com.medreminder.app.data.DoseLog(id = newId, medicationId = medId, doseTime = doseTime, dateKey = dateKey, taken = false)
        }
        if (!log.taken) {
            db.medicationDao().updateDoseLog(log.copy(taken = true, takenAt = System.currentTimeMillis()))
            val medication = db.medicationDao().getById(medId)
            if (medication != null) {
                val newQty = (medication.quantityRemaining - medication.dosePerIntake).coerceAtLeast(0)
                val updatedMed = medication.copy(quantityRemaining = newQty)
                db.medicationDao().update(updatedMed)
                if (newQty <= medication.lowStockThreshold) {
                    NotificationHelper.showLowStockNotification(context, updatedMed)
                }
            }
        }
        AlarmScheduler.cancelNag(context, medId, timeIndex)
        NotificationHelper.cancelDoseNotification(context, medId, timeIndex)
    }
}
