package com.churchtrack.app.viewmodel

import androidx.lifecycle.*
import com.churchtrack.app.data.database.entities.FinancialRecord
import com.churchtrack.app.data.repository.FinancialRepository
import com.churchtrack.app.util.DateUtil
import kotlinx.coroutines.launch

class FinancialViewModel(private val repository: FinancialRepository) : ViewModel() {

    val allRecords = repository.allRecords
    val todayTotal = repository.getTodayTotal()
    val monthTotal = repository.getMonthTotal()
    val yearTotal = repository.getYearTotal()

    private val _operationResult = MutableLiveData<Result<Unit>>()
    val operationResult: LiveData<Result<Unit>> = _operationResult

    private val _selectedPeriod = MutableLiveData<Pair<String, String>>(
        Pair(DateUtil.firstDayOfMonth(), DateUtil.lastDayOfMonth())
    )

    val periodRecords: LiveData<List<FinancialRecord>> = _selectedPeriod.switchMap { (start, end) ->
        repository.getRecordsBetweenDates(start, end)
    }

    fun setPeriod(startDate: String, endDate: String) {
        _selectedPeriod.value = Pair(startDate, endDate)
    }

    fun addRecord(
        date: String,
        offeringAmount: Double,
        titheAmount: Double,
        specialAmount: Double = 0.0,
        serviceId: Long? = null,
        notes: String = "",
        recordedBy: Long = 0
    ) = viewModelScope.launch {
        try {
            val record = FinancialRecord(
                date = date,
                serviceId = serviceId,
                offeringAmount = offeringAmount,
                titheAmount = titheAmount,
                specialOfferingAmount = specialAmount,
                notes = notes,
                recordedBy = recordedBy
            )
            repository.insertRecord(record)
            _operationResult.postValue(Result.success(Unit))
        } catch (e: Exception) {
            _operationResult.postValue(Result.failure(e))
        }
    }

    fun updateRecord(record: FinancialRecord) = viewModelScope.launch {
        try {
            repository.updateRecord(record)
            _operationResult.postValue(Result.success(Unit))
        } catch (e: Exception) {
            _operationResult.postValue(Result.failure(e))
        }
    }

    fun deleteRecord(record: FinancialRecord) = viewModelScope.launch {
        repository.deleteRecord(record)
    }

    suspend fun getTodayTotalSync() = repository.getTodayTotalSync()

    class Factory(private val repository: FinancialRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return FinancialViewModel(repository) as T
        }
    }
}
