package com.medreminder.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dose_logs")
data class DoseLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicationId: Long,
    val doseTime: String,
    val dateKey: String,
    val taken: Boolean = false,
    val takenAt: Long? = null
)
