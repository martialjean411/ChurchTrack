package com.churchtrack.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.churchtrack.app.data.database.dao.*
import com.churchtrack.app.data.database.entities.*
import com.churchtrack.app.util.PasswordUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Member::class,
        Attendance::class,
        WorshipService::class,
        FinancialRecord::class,
        Project::class,
        ProjectContribution::class,
        AppUser::class,
        AbsenceAlert::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun memberDao(): MemberDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun worshipServiceDao(): WorshipServiceDao
    abstract fun financialDao(): FinancialDao
    abstract fun projectDao(): ProjectDao
    abstract fun userDao(): UserDao
    abstract fun absenceAlertDao(): AbsenceAlertDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "churchtrack_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    // Seed default admin user
                    val adminUser = AppUser(
                        username = "admin",
                        passwordHash = PasswordUtil.hashPassword("admin123"),
                        fullName = "Administrateur",
                        role = "ADMIN"
                    )
                    database.userDao().insertUser(adminUser)
                }
            }
        }
    }
}
