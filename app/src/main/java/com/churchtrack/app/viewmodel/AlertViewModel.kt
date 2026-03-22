package com.churchtrack.app.viewmodel

import androidx.lifecycle.*
import com.churchtrack.app.data.database.entities.AbsenceAlert
import com.churchtrack.app.data.repository.AbsenceAlertRepository
import kotlinx.coroutines.launch

class AlertViewModel(private val repository: AbsenceAlertRepository) : ViewModel() {

    val activeAlerts = repository.activeAlerts
    val allAlerts = repository.allAlerts
    val pendingAlertCount = repository.pendingAlertCount

    fun markAsFollowedUp(alertId: Long, notes: String = "") = viewModelScope.launch {
        repository.markAsFollowedUp(alertId, notes)
    }

    fun resolveAlertsForMember(memberId: Long) = viewModelScope.launch {
        repository.resolveAlertsForMember(memberId)
    }

    fun deleteAlert(alert: AbsenceAlert) = viewModelScope.launch {
        repository.deleteAlert(alert)
    }

    class Factory(private val repository: AbsenceAlertRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return AlertViewModel(repository) as T
        }
    }
}
