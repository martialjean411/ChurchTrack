package com.churchtrack.app.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attendances",
    foreignKeys = [
        ForeignKey(
            entity = Member::class,
            parentColumns = ["id"],
            childColumns = ["memberId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = WorshipService::class,
            parentColumns = ["id"],
            childColumns = ["serviceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["memberId", "serviceId"], unique = true),
        Index(value = ["serviceId"]),
        Index(value = ["memberId"])
    ]
)
data class Attendance(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val memberId: Long,
    val serviceId: Long,
    val isPresent: Boolean = true,
    val checkInTime: Long = System.currentTimeMillis(),
    val method: String = "FINGERPRINT" // FINGERPRINT, MANUAL
)
