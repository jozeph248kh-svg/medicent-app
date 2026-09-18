package com.medreminder.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "appointments")
data class Appointment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val doctorName: String,
    val dateTimeMillis: Long,
    val notes: String
)
