package com.churchtrack.app.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.churchtrack.app.data.database.entities.Project
import com.churchtrack.app.data.database.entities.ProjectContribution

@Dao
interface ProjectDao {

    @Query("SELECT * FROM projects ORDER BY createdAt DESC")
    fun getAllProjects(): LiveData<List<Project>>

    @Query("SELECT * FROM projects WHERE status = :status ORDER BY createdAt DESC")
    fun getProjectsByStatus(status: String): LiveData<List<Project>>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getProjectById(id: Long): Project?

    @Query("SELECT COUNT(*) FROM projects WHERE status = 'IN_PROGRESS'")
    fun getActiveProjectCount(): LiveData<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: Project): Long

    @Update
    suspend fun updateProject(project: Project)

    @Delete
    suspend fun deleteProject(project: Project)

    @Query("UPDATE projects SET collectedAmount = collectedAmount + :amount WHERE id = :projectId")
    suspend fun addContribution(projectId: Long, amount: Double)

    // Project Contributions
    @Query("SELECT * FROM project_contributions WHERE projectId = :projectId ORDER BY date DESC")
    fun getContributionsForProject(projectId: Long): LiveData<List<ProjectContribution>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContribution(contribution: ProjectContribution): Long

    @Delete
    suspend fun deleteContribution(contribution: ProjectContribution)
}
