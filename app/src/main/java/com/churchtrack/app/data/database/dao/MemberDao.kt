package com.churchtrack.app.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.churchtrack.app.data.database.entities.Member

@Dao
interface MemberDao {

    @Query("SELECT * FROM members WHERE isActive = 1 ORDER BY lastName, firstName")
    fun getAllActiveMembers(): LiveData<List<Member>>

    @Query("SELECT * FROM members ORDER BY lastName, firstName")
    fun getAllMembers(): LiveData<List<Member>>

    @Query("SELECT * FROM members WHERE id = :id")
    suspend fun getMemberById(id: Long): Member?

    @Query("SELECT * FROM members WHERE firstName LIKE :query OR lastName LIKE :query OR phone LIKE :query ORDER BY lastName, firstName")
    fun searchMembers(query: String): LiveData<List<Member>>

    @Query("SELECT COUNT(*) FROM members WHERE isActive = 1")
    fun getActiveMemberCount(): LiveData<Int>

    @Query("SELECT * FROM members WHERE hasFingerprintRegistered = 1")
    suspend fun getMembersWithFingerprint(): List<Member>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: Member): Long

    @Update
    suspend fun updateMember(member: Member)

    @Delete
    suspend fun deleteMember(member: Member)

    @Query("UPDATE members SET isActive = 0 WHERE id = :id")
    suspend fun deactivateMember(id: Long)

    @Query("SELECT * FROM members WHERE id NOT IN (SELECT memberId FROM attendances WHERE serviceId = :serviceId)")
    suspend fun getAbsenteesForService(serviceId: Long): List<Member>

    @Query("SELECT COUNT(*) FROM members WHERE isActive = 1")
    suspend fun getActiveMemberCountSync(): Int
}
