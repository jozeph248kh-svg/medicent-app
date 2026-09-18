package com.medreminder.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medications")
data class Medication(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val dosageNote: String,
    val timesCsv: String,
    val quantityRemaining: Int,
    val lowStockThreshold: Int,
    val dosePerIntake: Int = 1
) {
    fun timesList(): List<String> = timesCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
}
