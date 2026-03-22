package com.churchtrack.app.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.churchtrack.app.data.database.entities.FinancialRecord

@Dao
interface FinancialDao {

    @Query("SELECT * FROM financial_records ORDER BY date DESC, createdAt DESC")
    fun getAllRecords(): LiveData<List<FinancialRecord>>

    @Query("SELECT * FROM financial_records WHERE id = :id")
    suspend fun getRecordById(id: Long): FinancialRecord?

    @Query("SELECT * FROM financial_records WHERE date = :date")
    fun getRecordsByDate(date: String): LiveData<List<FinancialRecord>>

    @Query("SELECT * FROM financial_records WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getRecordsBetweenDates(startDate: String, endDate: String): LiveData<List<FinancialRecord>>

    @Query("SELECT COALESCE(SUM(offeringAmount + titheAmount + specialOfferingAmount), 0) FROM financial_records WHERE date = :date")
    fun getTotalForDate(date: String): LiveData<Double>

    @Query("SELECT COALESCE(SUM(offeringAmount + titheAmount + specialOfferingAmount), 0) FROM financial_records WHERE date LIKE :monthYear")
    fun getTotalForMonth(monthYear: String): LiveData<Double> // monthYear format: "yyyy-MM"

    @Query("SELECT COALESCE(SUM(offeringAmount + titheAmount + specialOfferingAmount), 0) FROM financial_records WHERE date LIKE :year")
    fun getTotalForYear(year: String): LiveData<Double> // year format: "yyyy"

    @Query("SELECT COALESCE(SUM(offeringAmount), 0) FROM financial_records WHERE date >= :startDate AND date <= :endDate")
    suspend fun getTotalOfferingsSync(startDate: String, endDate: String): Double

    @Query("SELECT COALESCE(SUM(titheAmount), 0) FROM financial_records WHERE date >= :startDate AND date <= :endDate")
    suspend fun getTotalTithesSync(startDate: String, endDate: String): Double

    @Query("SELECT * FROM financial_records ORDER BY date DESC LIMIT :limit")
    suspend fun getRecentRecords(limit: Int = 5): List<FinancialRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: FinancialRecord): Long

    @Update
    suspend fun updateRecord(record: FinancialRecord)

    @Delete
    suspend fun deleteRecord(record: FinancialRecord)

    @Query("SELECT COALESCE(SUM(offeringAmount + titheAmount + specialOfferingAmount), 0) FROM financial_records WHERE date = :today")
    suspend fun getTodayTotalSync(today: String): Double
}
