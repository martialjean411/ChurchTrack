package com.churchtrack.app.viewmodel

import androidx.lifecycle.*
import com.churchtrack.app.data.database.entities.Member
import com.churchtrack.app.data.repository.MemberRepository
import kotlinx.coroutines.launch

class MemberViewModel(private val repository: MemberRepository) : ViewModel() {

    val allActiveMembers = repository.allActiveMembers
    val activeMemberCount = repository.activeMemberCount

    private val _searchQuery = MutableLiveData<String>("")
    val searchResults: LiveData<List<Member>> = _searchQuery.switchMap { query ->
        if (query.isBlank()) repository.allActiveMembers
        else repository.searchMembers(query)
    }

    private val _operationResult = MutableLiveData<Result<Unit>>()
    val operationResult: LiveData<Result<Unit>> = _operationResult

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun insertMember(member: Member) = viewModelScope.launch {
        try {
            repository.insertMember(member)
            _operationResult.postValue(Result.success(Unit))
        } catch (e: Exception) {
            _operationResult.postValue(Result.failure(e))
        }
    }

    fun updateMember(member: Member) = viewModelScope.launch {
        try {
            repository.updateMember(member)
            _operationResult.postValue(Result.success(Unit))
        } catch (e: Exception) {
            _operationResult.postValue(Result.failure(e))
        }
    }

    fun deleteMember(member: Member) = viewModelScope.launch {
        repository.deactivateMember(member.id)
    }

    suspend fun getMemberById(id: Long) = repository.getMemberById(id)

    class Factory(private val repository: MemberRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return MemberViewModel(repository) as T
        }
    }
}
