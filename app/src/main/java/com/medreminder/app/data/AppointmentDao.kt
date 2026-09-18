package com.medreminder.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface AppointmentDao {
    @Insert
    suspend fun insert(appointment: Appointment): Long

    @Update
    suspend fun update(appointment: Appointment)

    @Delete
    suspend fun delete(appointment: Appointment)

    @Query("SELECT * FROM appointments ORDER BY dateTimeMillis")
    suspend fun getAll(): List<Appointment>

    @Query("SELECT * FROM appointments WHERE id = :id")
    suspend fun getById(id: Long): Appointment?
}
