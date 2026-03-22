package com.churchtrack.app.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.churchtrack.app.data.database.entities.AppUser

@Dao
interface UserDao {

    @Query("SELECT * FROM app_users ORDER BY fullName")
    fun getAllUsers(): LiveData<List<AppUser>>

    @Query("SELECT * FROM app_users WHERE id = :id")
    suspend fun getUserById(id: Long): AppUser?

    @Query("SELECT * FROM app_users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): AppUser?

    @Query("SELECT * FROM app_users WHERE username = :username AND passwordHash = :passwordHash LIMIT 1")
    suspend fun authenticate(username: String, passwordHash: String): AppUser?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: AppUser): Long

    @Update
    suspend fun updateUser(user: AppUser)

    @Delete
    suspend fun deleteUser(user: AppUser)

    @Query("UPDATE app_users SET lastLogin = :timestamp WHERE id = :userId")
    suspend fun updateLastLogin(userId: Long, timestamp: Long)

    @Query("SELECT COUNT(*) FROM app_users")
    suspend fun getUserCount(): Int
}
