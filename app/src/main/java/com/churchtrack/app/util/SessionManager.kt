package com.churchtrack.app.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object SessionManager {
    private const val PREF_NAME = "churchtrack_session"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USERNAME = "username"
    private const val KEY_FULL_NAME = "full_name"
    private const val KEY_ROLE = "role"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_ABSENCE_THRESHOLD = "absence_threshold"

    private fun getPrefs(context: Context): SharedPreferences {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context,
                PREF_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        }
    }

    fun saveSession(context: Context, userId: Long, username: String, fullName: String, role: String) {
        getPrefs(context).edit().apply {
            putLong(KEY_USER_ID, userId)
            putString(KEY_USERNAME, username)
            putString(KEY_FULL_NAME, fullName)
            putString(KEY_ROLE, role)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    fun isLoggedIn(context: Context): Boolean =
        getPrefs(context).getBoolean(KEY_IS_LOGGED_IN, false)

    fun getUserId(context: Context): Long =
        getPrefs(context).getLong(KEY_USER_ID, -1L)

    fun getUsername(context: Context): String =
        getPrefs(context).getString(KEY_USERNAME, "") ?: ""

    fun getFullName(context: Context): String =
        getPrefs(context).getString(KEY_FULL_NAME, "Utilisateur") ?: "Utilisateur"

    fun getRole(context: Context): String =
        getPrefs(context).getString(KEY_ROLE, "USER") ?: "USER"

    fun isAdmin(context: Context): Boolean = getRole(context) == "ADMIN"

    fun getAbsenceThreshold(context: Context): Int =
        getPrefs(context).getInt(KEY_ABSENCE_THRESHOLD, 2)

    fun setAbsenceThreshold(context: Context, threshold: Int) {
        getPrefs(context).edit().putInt(KEY_ABSENCE_THRESHOLD, threshold).apply()
    }

    fun clearSession(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
}
