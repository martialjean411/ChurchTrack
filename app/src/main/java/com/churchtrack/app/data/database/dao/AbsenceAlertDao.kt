package com.churchtrack.app.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.churchtrack.app.data.database.entities.AbsenceAlert

@Dao
interface AbsenceAlertDao {

    @Query("SELECT * FROM absence_alerts WHERE status != 'RESOLVED' ORDER BY createdAt DESC")
    fun getActiveAlerts(): LiveData<List<AbsenceAlert>>

    @Query("SELECT * FROM absence_alerts ORDER BY createdAt DESC")
    fun getAllAlerts(): LiveData<List<AbsenceAlert>>

    @Query("SELECT * FROM absence_alerts WHERE memberId = :memberId ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestAlertForMember(memberId: Long): AbsenceAlert?

    @Query("SELECT COUNT(*) FROM absence_alerts WHERE status = 'PENDING'")
    fun getPendingAlertCount(): LiveData<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: AbsenceAlert): Long

    @Update
    suspend fun updateAlert(alert: AbsenceAlert)

    @Query("UPDATE absence_alerts SET status = 'RESOLVED', resolvedAt = :timestamp WHERE memberId = :memberId")
    suspend fun resolveAlertsForMember(memberId: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE absence_alerts SET isFollowedUp = 1, status = 'CONTACTED', followUpNotes = :notes WHERE id = :alertId")
    suspend fun markAsFollowedUp(alertId: Long, notes: String = "")

    @Delete
    suspend fun deleteAlert(alert: AbsenceAlert)
}
