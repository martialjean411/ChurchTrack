package com.churchtrack.app.viewmodel

import androidx.lifecycle.*
import com.churchtrack.app.data.database.entities.Attendance
import com.churchtrack.app.data.database.entities.WorshipService
import com.churchtrack.app.data.repository.AttendanceRepository
import com.churchtrack.app.util.DateUtil
import kotlinx.coroutines.launch

class AttendanceViewModel(private val repository: AttendanceRepository) : ViewModel() {

    val allServices = repository.getAllServices()

    private val _selectedServiceId = MutableLiveData<Long>(-1L)
    val selectedServiceId: LiveData<Long> = _selectedServiceId

    val selectedServiceAttendances: LiveData<List<Attendance>> = _selectedServiceId.switchMap { id ->
        if (id > 0) repository.getAttendancesForService(id)
        else MutableLiveData(emptyList())
    }

    val presentCount: LiveData<Int> = _selectedServiceId.switchMap { id ->
        if (id > 0) repository.getPresentCountForService(id)
        else MutableLiveData(0)
    }

    private val _operationResult = MutableLiveData<Result<Unit>>()
    val operationResult: LiveData<Result<Unit>> = _operationResult

    fun selectService(serviceId: Long) {
        _selectedServiceId.value = serviceId
    }

    fun createService(date: String, type: String, title: String = "") = viewModelScope.launch {
        try {
            val service = WorshipService(date = date, serviceType = type, title = title)
            val id = repository.insertService(service)
            _selectedServiceId.postValue(id)
            _operationResult.postValue(Result.success(Unit))
        } catch (e: Exception) {
            _operationResult.postValue(Result.failure(e))
        }
    }

    fun markAttendance(memberId: Long, serviceId: Long, method: String = "MANUAL") = viewModelScope.launch {
        try {
            val existing = repository.getAttendanceForMemberAndService(memberId, serviceId)
            if (existing == null) {
                repository.insertAttendance(
                    Attendance(memberId = memberId, serviceId = serviceId, method = method)
                )
            }
            _operationResult.postValue(Result.success(Unit))
        } catch (e: Exception) {
            _operationResult.postValue(Result.failure(e))
        }
    }

    fun removeAttendance(attendance: Attendance) = viewModelScope.launch {
        repository.deleteAttendance(attendance)
    }

    fun deleteService(service: WorshipService) = viewModelScope.launch {
        repository.deleteService(service)
    }

    suspend fun getLatestService() = repository.getLatestService()

    suspend fun getPresentCountForService(serviceId: Long) =
        repository.getPresentCountSync(serviceId)

    class Factory(private val repository: AttendanceRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return AttendanceViewModel(repository) as T
        }
    }
}
