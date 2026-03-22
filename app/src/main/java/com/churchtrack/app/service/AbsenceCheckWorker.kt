package com.churchtrack.app.service

import android.content.Context
import androidx.work.*
import com.churchtrack.app.ChurchTrackApp
import com.churchtrack.app.data.database.entities.AbsenceAlert
import com.churchtrack.app.util.NotificationUtil
import com.churchtrack.app.util.SessionManager
import java.util.concurrent.TimeUnit

class AbsenceCheckWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val threshold = SessionManager.getAbsenceThreshold(context)
        runCheck(context, threshold)
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
            val db = app.database
            val alertRepo = app.absenceAlertRepository

            // Need at least `threshold` recent services to check
            val recentServices = db.worshipServiceDao().getRecentServices(threshold + 2)
            if (recentServices.size < threshold) return

            // Only check members who have fingerprint registered (active attendance members)
            val members = db.memberDao().getMembersWithFingerprint()
                .ifEmpty { return }

            members.forEach { member ->
                val consecutiveAbsences = db.attendanceDao()
                    .getConsecutiveAbsenceCount(member.id, threshold)

                if (consecutiveAbsences >= threshold) {
                    val existingAlert = alertRepo.getLatestAlertForMember(member.id)
                    val lastDate = db.attendanceDao()
                        .getLastAttendanceDateForMember(member.id) ?: ""

                    when {
                        existingAlert == null || existingAlert.status == "RESOLVED" -> {
                            val alert = AbsenceAlert(
                                memberId = member.id,
                                consecutiveAbsences = consecutiveAbsences,
                                lastAttendanceDate = lastDate
                            )
                            val alertId = alertRepo.insertAlert(alert)
                            NotificationUtil.showAbsenceAlert(
                                context, member.fullName(),
                                consecutiveAbsences, alertId.toInt()
                            )
                        }
                        existingAlert.consecutiveAbsences < consecutiveAbsences -> {
                            alertRepo.updateAlert(
                                existingAlert.copy(
                                    consecutiveAbsences = consecutiveAbsences,
                                    lastAttendanceDate = lastDate
                                )
                            )
                        }
                    }
                } else if (consecutiveAbsences == 0) {
                    // Member came back — resolve any open alert
                    alertRepo.resolveAlertsForMember(member.id)
                }
            }
        }
    }
}
