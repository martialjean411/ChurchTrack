package com.churchtrack.app.data.repository

import com.churchtrack.app.data.database.dao.FinancialDao
import com.churchtrack.app.data.database.entities.FinancialRecord
import com.churchtrack.app.util.DateUtil

class FinancialRepository(private val financialDao: FinancialDao) {

    val allRecords = financialDao.getAllRecords()

    fun getRecordsByDate(date: String) = financialDao.getRecordsByDate(date)

    fun getRecordsBetweenDates(start: String, end: String) =
        financialDao.getRecordsBetweenDates(start, end)

    fun getTodayTotal() = financialDao.getTotalForDate(DateUtil.today())
    fun getMonthTotal() = financialDao.getTotalForMonth(DateUtil.currentMonthYear())
    fun getYearTotal() = financialDao.getTotalForYear(DateUtil.currentYear())

    suspend fun getTodayTotalSync() = financialDao.getTodayTotalSync(DateUtil.today())

    suspend fun getMonthlyOfferingsSync(startDate: String, endDate: String) =
        financialDao.getTotalOfferingsSync(startDate, endDate)

    suspend fun getMonthlyTithesSync(startDate: String, endDate: String) =
        financialDao.getTotalTithesSync(startDate, endDate)

    suspend fun getRecentRecords(limit: Int = 5) = financialDao.getRecentRecords(limit)

    suspend fun insertRecord(record: FinancialRecord) = financialDao.insertRecord(record)
    suspend fun updateRecord(record: FinancialRecord) = financialDao.updateRecord(record)
    suspend fun deleteRecord(record: FinancialRecord) = financialDao.deleteRecord(record)
    suspend fun getRecordById(id: Long) = financialDao.getRecordById(id)
}
