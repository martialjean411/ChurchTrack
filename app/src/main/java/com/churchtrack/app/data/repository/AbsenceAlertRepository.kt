package com.churchtrack.app.data.repository

import com.churchtrack.app.data.database.dao.AbsenceAlertDao
import com.churchtrack.app.data.database.entities.AbsenceAlert

class AbsenceAlertRepository(private val alertDao: AbsenceAlertDao) {

    val activeAlerts = alertDao.getActiveAlerts()
    val allAlerts = alertDao.getAllAlerts()
    val pendingAlertCount = alertDao.getPendingAlertCount()

    suspend fun getLatestAlertForMember(memberId: Long) = alertDao.getLatestAlertForMember(memberId)

    suspend fun insertAlert(alert: AbsenceAlert) = alertDao.insertAlert(alert)
    suspend fun updateAlert(alert: AbsenceAlert) = alertDao.updateAlert(alert)

    suspend fun resolveAlertsForMember(memberId: Long) =
        alertDao.resolveAlertsForMember(memberId)

    suspend fun markAsFollowedUp(alertId: Long, notes: String = "") =
        alertDao.markAsFollowedUp(alertId, notes)

    suspend fun deleteAlert(alert: AbsenceAlert) = alertDao.deleteAlert(alert)
}
