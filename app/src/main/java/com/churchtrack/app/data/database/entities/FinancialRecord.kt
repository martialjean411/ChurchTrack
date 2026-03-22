package com.churchtrack.app.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "financial_records",
    foreignKeys = [
        ForeignKey(
            entity = WorshipService::class,
            parentColumns = ["id"],
            childColumns = ["serviceId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["serviceId"])]
)
data class FinancialRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val serviceId: Long? = null,
    val date: String, // yyyy-MM-dd
    val offeringAmount: Double = 0.0,
    val titheAmount: Double = 0.0,
    val specialOfferingAmount: Double = 0.0,
    val notes: String = "",
    val recordedBy: Long = 0, // User ID
    val createdAt: Long = System.currentTimeMillis()
) {
    fun totalAmount(): Double = offeringAmount + titheAmount + specialOfferingAmount
}
