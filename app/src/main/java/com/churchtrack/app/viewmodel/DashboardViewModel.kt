package com.churchtrack.app.viewmodel

import androidx.lifecycle.*
import com.churchtrack.app.data.repository.*
import com.churchtrack.app.util.DateUtil
import kotlinx.coroutines.launch

data class DashboardStats(
    val totalMembers: Int = 0,
    val todayPresent: Int = 0,
    val todayFinancial: Double = 0.0,
    val activeProjects: Int = 0,
    val pendingAlerts: Int = 0
)

class DashboardViewModel(
    private val memberRepo: MemberRepository,
    private val attendanceRepo: AttendanceRepository,
    private val financialRepo: FinancialRepository,
    private val projectRepo: ProjectRepository,
    private val alertRepo: AbsenceAlertRepository
) : ViewModel() {

    val totalMembers: LiveData<Int> = memberRepo.activeMemberCount
    val activeProjectCount = projectRepo.activeProjectCount
    val pendingAlertCount = alertRepo.pendingAlertCount
    val todayFinancial = financialRepo.getTodayTotal()
    val monthFinancial = financialRepo.getMonthTotal()
    val recentServices = attendanceRepo.getAllServices()

    private val _todayPresentCount = MutableLiveData<Int>(0)
    val todayPresentCount: LiveData<Int> = _todayPresentCount

    init {
        refreshTodayPresent()
    }

    fun refreshTodayPresent() = viewModelScope.launch {
        val latestService = attendanceRepo.getLatestService()
        if (latestService != null && latestService.date == DateUtil.today()) {
            val count = attendanceRepo.getPresentCountSync(latestService.id)
            _todayPresentCount.postValue(count)
        }
    }

    class Factory(
        private val memberRepo: MemberRepository,
        private val attendanceRepo: AttendanceRepository,
        private val financialRepo: FinancialRepository,
        private val projectRepo: ProjectRepository,
        private val alertRepo: AbsenceAlertRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(memberRepo, attendanceRepo, financialRepo, projectRepo, alertRepo) as T
        }
    }
}
