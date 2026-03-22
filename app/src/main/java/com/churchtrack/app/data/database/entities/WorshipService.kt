package com.churchtrack.app.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "worship_services")
data class WorshipService(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // yyyy-MM-dd
    val serviceType: String, // SUNDAY_MORNING, SUNDAY_EVENING, WEDNESDAY, SPECIAL, YOUTH
    val title: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
