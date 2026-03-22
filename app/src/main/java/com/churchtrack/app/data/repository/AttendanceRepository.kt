package com.churchtrack.app.data.repository

import com.churchtrack.app.data.database.dao.AttendanceDao
import com.churchtrack.app.data.database.dao.WorshipServiceDao
import com.churchtrack.app.data.database.entities.Attendance
import com.churchtrack.app.data.database.entities.WorshipService

class AttendanceRepository(
    private val attendanceDao: AttendanceDao,
    private val worshipServiceDao: WorshipServiceDao
) {
    fun getAllServices() = worshipServiceDao.getAllServices()
    fun getServicesByDate(date: String) = worshipServiceDao.getServicesByDate(date)

    suspend fun getLatestService() = worshipServiceDao.getLatestService()
    suspend fun getServiceById(id: Long) = worshipServiceDao.getServiceById(id)

    fun getServicesBetweenDates(start: String, end: String) =
        worshipServiceDao.getServicesBetweenDates(start, end)

    suspend fun insertService(service: WorshipService) = worshipServiceDao.insertService(service)
    suspend fun updateService(service: WorshipService) = worshipServiceDao.updateService(service)
    suspend fun deleteService(service: WorshipService) = worshipServiceDao.deleteService(service)

    fun getAttendancesForService(serviceId: Long) = attendanceDao.getAttendancesForService(serviceId)
    fun getAttendancesForMember(memberId: Long) = attendanceDao.getAttendancesForMember(memberId)
    fun getPresentCountForService(serviceId: Long) = attendanceDao.getPresentCountForService(serviceId)

    suspend fun getPresentCountSync(serviceId: Long) = attendanceDao.getPresentCountForServiceSync(serviceId)

    suspend fun getAttendanceForMemberAndService(memberId: Long, serviceId: Long) =
        attendanceDao.getAttendanceForMemberAndService(memberId, serviceId)

    suspend fun insertAttendance(attendance: Attendance) = attendanceDao.insertAttendance(attendance)
    suspend fun updateAttendance(attendance: Attendance) = attendanceDao.updateAttendance(attendance)
    suspend fun deleteAttendance(attendance: Attendance) = attendanceDao.deleteAttendance(attendance)

    suspend fun getConsecutiveAbsences(memberId: Long, lastN: Int = 5) =
        attendanceDao.getConsecutiveAbsenceCount(memberId, lastN)

    suspend fun getLastAttendanceDate(memberId: Long) =
        attendanceDao.getLastAttendanceDateForMember(memberId)

    suspend fun getRecentServices(limit: Int = 10) = worshipServiceDao.getRecentServices(limit)
}
