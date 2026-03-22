package com.churchtrack.app.viewmodel

import androidx.lifecycle.*
import com.churchtrack.app.data.database.entities.Project
import com.churchtrack.app.data.repository.ProjectRepository
import com.churchtrack.app.util.DateUtil
import kotlinx.coroutines.launch

class ProjectViewModel(private val repository: ProjectRepository) : ViewModel() {

    val allProjects = repository.allProjects
    val activeProjectCount = repository.activeProjectCount

    val inProgressProjects = repository.getProjectsByStatus("IN_PROGRESS")
    val completedProjects = repository.getProjectsByStatus("COMPLETED")

    private val _operationResult = MutableLiveData<Result<Unit>>()
    val operationResult: LiveData<Result<Unit>> = _operationResult

    fun createProject(
        name: String,
        description: String,
        targetAmount: Double,
        startDate: String = DateUtil.today()
    ) = viewModelScope.launch {
        try {
            val project = Project(
                name = name,
                description = description,
                targetAmount = targetAmount,
                startDate = startDate
            )
            repository.insertProject(project)
            _operationResult.postValue(Result.success(Unit))
        } catch (e: Exception) {
            _operationResult.postValue(Result.failure(e))
        }
    }

    fun addContribution(projectId: Long, amount: Double, date: String, notes: String = "") =
        viewModelScope.launch {
            try {
                repository.addContribution(projectId, amount, date, notes)
                _operationResult.postValue(Result.success(Unit))
            } catch (e: Exception) {
                _operationResult.postValue(Result.failure(e))
            }
        }

    fun updateProject(project: Project) = viewModelScope.launch {
        try {
            repository.updateProject(project)
            _operationResult.postValue(Result.success(Unit))
        } catch (e: Exception) {
            _operationResult.postValue(Result.failure(e))
        }
    }

    fun deleteProject(project: Project) = viewModelScope.launch {
        repository.deleteProject(project)
    }

    fun getContributions(projectId: Long) = repository.getContributionsForProject(projectId)

    class Factory(private val repository: ProjectRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ProjectViewModel(repository) as T
        }
    }
}
