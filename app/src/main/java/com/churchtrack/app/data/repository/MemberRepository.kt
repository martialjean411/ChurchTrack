package com.churchtrack.app.data.repository

import com.churchtrack.app.data.database.dao.MemberDao
import com.churchtrack.app.data.database.entities.Member

class MemberRepository(private val memberDao: MemberDao) {

    val allActiveMembers = memberDao.getAllActiveMembers()
    val allMembers = memberDao.getAllMembers()
    val activeMemberCount = memberDao.getActiveMemberCount()

    fun searchMembers(query: String) = memberDao.searchMembers("%$query%")

    suspend fun getMemberById(id: Long) = memberDao.getMemberById(id)

    suspend fun insertMember(member: Member) = memberDao.insertMember(member)

    suspend fun updateMember(member: Member) = memberDao.updateMember(member)

    suspend fun deleteMember(member: Member) = memberDao.deleteMember(member)

    suspend fun deactivateMember(id: Long) = memberDao.deactivateMember(id)

    suspend fun getMembersWithFingerprint() = memberDao.getMembersWithFingerprint()

    suspend fun getAbsenteesForService(serviceId: Long) = memberDao.getAbsenteesForService(serviceId)

    suspend fun getActiveMemberCountSync() = memberDao.getActiveMemberCountSync()
}
