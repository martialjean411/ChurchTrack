package com.churchtrack.app.data.repository

import com.churchtrack.app.data.database.dao.UserDao
import com.churchtrack.app.data.database.entities.AppUser
import com.churchtrack.app.util.PasswordUtil

class UserRepository(private val userDao: UserDao) {

    val allUsers = userDao.getAllUsers()

    suspend fun authenticate(username: String, password: String): AppUser? {
        val hash = PasswordUtil.hashPassword(password)
        return userDao.authenticate(username, hash)
    }

    suspend fun getUserById(id: Long) = userDao.getUserById(id)
    suspend fun getUserByUsername(username: String) = userDao.getUserByUsername(username)

    suspend fun createUser(username: String, password: String, fullName: String, role: String): Long {
        val user = AppUser(
            username = username,
            passwordHash = PasswordUtil.hashPassword(password),
            fullName = fullName,
            role = role
        )
        return userDao.insertUser(user)
    }

    suspend fun updateUser(user: AppUser) = userDao.updateUser(user)
    suspend fun deleteUser(user: AppUser) = userDao.deleteUser(user)

    suspend fun updateLastLogin(userId: Long) =
        userDao.updateLastLogin(userId, System.currentTimeMillis())

    suspend fun getUserCount() = userDao.getUserCount()
}
