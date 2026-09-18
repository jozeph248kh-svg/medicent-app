package com.medreminder.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface MedicationDao {
    @Insert
    suspend fun insert(medication: Medication): Long

    @Update
    suspend fun update(medication: Medication)

    @Delete
    suspend fun delete(medication: Medication)

    @Query("SELECT * FROM medications ORDER BY name")
    suspend fun getAll(): List<Medication>

    @Query("SELECT * FROM medications WHERE id = :id")
    suspend fun getById(id: Long): Medication?

    @Insert
    suspend fun insertDoseLog(doseLog: DoseLog): Long

    @Update
    suspend fun updateDoseLog(doseLog: DoseLog)

    @Query("SELECT * FROM dose_logs WHERE medicationId = :medId AND doseTime = :doseTime AND dateKey = :dateKey LIMIT 1")
    suspend fun getDoseLog(medId: Long, doseTime: String, dateKey: String): DoseLog?

    @Query("SELECT * FROM dose_logs WHERE dateKey = :dateKey")
    suspend fun getDoseLogsForDate(dateKey: String): List<DoseLog>
}
