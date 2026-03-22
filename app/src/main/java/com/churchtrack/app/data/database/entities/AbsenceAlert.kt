package com.churchtrack.app.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "absence_alerts",
    foreignKeys = [
        ForeignKey(
            entity = Member::class,
            parentColumns = ["id"],
            childColumns = ["memberId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["memberId"])]
)
data class AbsenceAlert(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val memberId: Long,
    val consecutiveAbsences: Int,
    val lastAttendanceDate: String = "",
    val status: String = "PENDING", // PENDING, CONTACTED, RESOLVED
    val isFollowedUp: Boolean = false,
    val followUpNotes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val resolvedAt: Long = 0
)
