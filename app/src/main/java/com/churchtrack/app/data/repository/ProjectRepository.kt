package com.churchtrack.app.data.repository

import com.churchtrack.app.data.database.dao.ProjectDao
import com.churchtrack.app.data.database.entities.Project
import com.churchtrack.app.data.database.entities.ProjectContribution

class ProjectRepository(private val projectDao: ProjectDao) {

    val allProjects = projectDao.getAllProjects()
    val activeProjectCount = projectDao.getActiveProjectCount()

    fun getProjectsByStatus(status: String) = projectDao.getProjectsByStatus(status)

    fun getContributionsForProject(projectId: Long) = projectDao.getContributionsForProject(projectId)

    suspend fun getProjectById(id: Long) = projectDao.getProjectById(id)

    suspend fun insertProject(project: Project) = projectDao.insertProject(project)
    suspend fun updateProject(project: Project) = projectDao.updateProject(project)
    suspend fun deleteProject(project: Project) = projectDao.deleteProject(project)

    suspend fun addContribution(projectId: Long, amount: Double, date: String, notes: String = "") {
        projectDao.addContribution(projectId, amount)
        projectDao.insertContribution(ProjectContribution(projectId = projectId, amount = amount, date = date, notes = notes))
    }

    suspend fun deleteContribution(contribution: ProjectContribution) = projectDao.deleteContribution(contribution)
}
