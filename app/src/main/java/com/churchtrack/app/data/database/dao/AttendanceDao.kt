package com.churchtrack.app.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.churchtrack.app.data.database.entities.Attendance

@Dao
interface AttendanceDao {

    @Query("SELECT * FROM attendances WHERE serviceId = :serviceId")
    fun getAttendancesForService(serviceId: Long): LiveData<List<Attendance>>

    @Query("SELECT * FROM attendances WHERE memberId = :memberId ORDER BY checkInTime DESC")
    fun getAttendancesForMember(memberId: Long): LiveData<List<Attendance>>

    @Query("SELECT COUNT(*) FROM attendances WHERE serviceId = :serviceId AND isPresent = 1")
    fun getPresentCountForService(serviceId: Long): LiveData<Int>

    @Query("SELECT COUNT(*) FROM attendances WHERE serviceId = :serviceId AND isPresent = 1")
    suspend fun getPresentCountForServiceSync(serviceId: Long): Int

    @Query("SELECT * FROM attendances WHERE memberId = :memberId AND serviceId = :serviceId LIMIT 1")
    suspend fun getAttendanceForMemberAndService(memberId: Long, serviceId: Long): Attendance?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: Attendance): Long

    @Update
    suspend fun updateAttendance(attendance: Attendance)

    @Delete
    suspend fun deleteAttendance(attendance: Attendance)

    @Query("""
        SELECT COUNT(*) FROM worship_services ws
        WHERE ws.id NOT IN (
            SELECT serviceId FROM attendances WHERE memberId = :memberId AND isPresent = 1
        )
        AND ws.id IN (
            SELECT id FROM worship_services ORDER BY createdAt DESC LIMIT :lastN
        )
    """)
    suspend fun getConsecutiveAbsenceCount(memberId: Long, lastN: Int = 5): Int

    @Query("""
        SELECT s.date FROM worship_services s
        INNER JOIN attendances a ON a.serviceId = s.id
        WHERE a.memberId = :memberId AND a.isPresent = 1
        ORDER BY s.createdAt DESC
        LIMIT 1
    """)
    suspend fun getLastAttendanceDateForMember(memberId: Long): String?

    @Query("SELECT * FROM attendances WHERE memberId = :memberId ORDER BY checkInTime DESC LIMIT :lastN")
    suspend fun getLastAttendancesForMember(memberId: Long, lastN: Int): List<Attendance>
}
