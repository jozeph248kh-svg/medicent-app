package com.medreminder.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_tasks")
data class DailyTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val time: String,
    val active: Boolean = true
)
