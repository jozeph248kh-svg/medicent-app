package com.medreminder.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface TaskDao {
    @Insert
    suspend fun insert(task: DailyTask): Long

    @Update
    suspend fun update(task: DailyTask)

    @Delete
    suspend fun delete(task: DailyTask)

    @Query("SELECT * FROM daily_tasks ORDER BY time")
    suspend fun getAll(): List<DailyTask>

    @Query("SELECT * FROM daily_tasks WHERE id = :id")
    suspend fun getById(id: Long): DailyTask?
}
