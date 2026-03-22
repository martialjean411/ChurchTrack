package com.churchtrack.app.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val targetAmount: Double,
    val collectedAmount: Double = 0.0,
    val status: String = "IN_PROGRESS", // IN_PROGRESS, COMPLETED, PAUSED
    val startDate: String = "",
    val endDate: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun progressPercent(): Int {
        if (targetAmount <= 0) return 0
        return ((collectedAmount / targetAmount) * 100).toInt().coerceIn(0, 100)
    }
}
