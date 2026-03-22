package com.churchtrack.app.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "members")
data class Member(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val firstName: String,
    val lastName: String,
    val phone: String,
    val email: String = "",
    val gender: String, // "M" or "F"
    val birthDate: String = "",
    val address: String = "",
    val photoPath: String = "",
    val fingerprintData: ByteArray? = null,
    val hasFingerprintRegistered: Boolean = false,
    val memberSince: String = "",
    val isActive: Boolean = true,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Member
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    fun fullName(): String = "$firstName $lastName"
}
