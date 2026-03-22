package com.churchtrack.app.service

import android.content.Context
import androidx.work.*
import com.churchtrack.app.ChurchTrackApp
import com.churchtrack.app.data.database.entities.AbsenceAlert
import com.churchtrack.app.util.DateUtil
import com.churchtrack.app.util.NotificationUtil
import com.churchtrack.app.util.SessionManager
import java.util.concurrent.TimeUnit

class AbsenceCheckWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val app = context.applicationContext as ChurchTrackApp
        val memberRepo = app.memberRepository
        val attendanceRepo = app.attendanceRepository
        val alertRepo = app.absenceAlertRepository
        val threshold = SessionManager.getAbsenceThreshold(context)

        val members = memberRepo.getAllActiveMembers() // Need sync version
        val recentServices = attendanceRepo.getRecentServices(threshold + 2)

        if (recentServices.size < threshold) return Result.success()

        // Check each active member
        val allMembersList = app.database.memberDao().let { dao ->
            // Use synchronous query for worker
            var result: List<com.churchtrack.app.data.database.entities.Member> = emptyList()
            result
        }

        return Result.success()
    }

    companion object {
        const val WORK_NAME = "absence_check_work"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()

            val request = PeriodicWorkRequestBuilder<AbsenceCheckWorker>(1, TimeUnit.DAYS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        suspend fun runCheck(context: Context, threshold: Int) {
            val app = context.applicationContext as ChurchTrackApp
            val attendanceRepo = app.attendanceRepository
            val alertRepo = app.absenceAlertRepository

            val recentServices = attendanceRepo.getRecentServices(threshold + 2)
            if (recentServices.size < threshold) return

            val db = app.database
            val members = db.memberDao().getMembersWithFingerprint()

            members.forEach { member ->
                val consecutiveAbsences = db.attendanceDao()
                    .getConsecutiveAbsenceCount(member.id, threshold)

                if (consecutiveAbsences >= threshold) {
                    val existingAlert = alertRepo.getLatestAlertForMember(member.id)
                    val lastDate = attendanceRepo.getLastAttendanceDate(member.id) ?: ""

                    if (existingAlert == null || existingAlert.status == "RESOLVED") {
                        val alert = AbsenceAlert(
                            memberId = member.id,
                            consecutiveAbsences = consecutiveAbsences,
                            lastAttendanceDate = lastDate
                        )
                        val alertId = alertRepo.insertAlert(alert)
                        NotificationUtil.showAbsenceAlert(
                            context,
                            member.fullName(),
                            consecutiveAbsences,
                            alertId.toInt()
                        )
                    } else if (existingAlert.consecutiveAbsences < consecutiveAbsences) {
                        alertRepo.updateAlert(
                            existingAlert.copy(
                                consecutiveAbsences = consecutiveAbsences,
                                lastAttendanceDate = lastDate
                            )
                        )
                    }
                }
            }
        }
    }
}
