package com.churchtrack.app.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.churchtrack.app.data.database.entities.WorshipService

@Dao
interface WorshipServiceDao {

    @Query("SELECT * FROM worship_services ORDER BY date DESC, createdAt DESC")
    fun getAllServices(): LiveData<List<WorshipService>>

    @Query("SELECT * FROM worship_services WHERE date = :date ORDER BY createdAt DESC")
    fun getServicesByDate(date: String): LiveData<List<WorshipService>>

    @Query("SELECT * FROM worship_services ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestService(): WorshipService?

    @Query("SELECT * FROM worship_services WHERE id = :id")
    suspend fun getServiceById(id: Long): WorshipService?

    @Query("SELECT * FROM worship_services WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getServicesBetweenDates(startDate: String, endDate: String): LiveData<List<WorshipService>>

    @Query("SELECT * FROM worship_services WHERE serviceType = :type ORDER BY date DESC")
    fun getServicesByType(type: String): LiveData<List<WorshipService>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertService(service: WorshipService): Long

    @Update
    suspend fun updateService(service: WorshipService)

    @Delete
    suspend fun deleteService(service: WorshipService)

    @Query("SELECT COUNT(*) FROM worship_services")
    fun getTotalServiceCount(): LiveData<Int>

    @Query("SELECT * FROM worship_services ORDER BY date DESC LIMIT :limit")
    suspend fun getRecentServices(limit: Int = 10): List<WorshipService>
}
