package com.churchtrack.app

import android.app.Application
import com.churchtrack.app.data.database.AppDatabase
import com.churchtrack.app.data.repository.*
import com.churchtrack.app.util.NotificationUtil

class ChurchTrackApp : Application() {

    val database by lazy { AppDatabase.getInstance(this) }

    val memberRepository by lazy { MemberRepository(database.memberDao()) }
    val attendanceRepository by lazy {
        AttendanceRepository(database.attendanceDao(), database.worshipServiceDao())
    }
    val financialRepository by lazy { FinancialRepository(database.financialDao()) }
    val projectRepository by lazy { ProjectRepository(database.projectDao()) }
    val userRepository by lazy { UserRepository(database.userDao()) }
    val absenceAlertRepository by lazy { AbsenceAlertRepository(database.absenceAlertDao()) }

    override fun onCreate() {
        super.onCreate()
        NotificationUtil.createNotificationChannels(this)
    }
}
