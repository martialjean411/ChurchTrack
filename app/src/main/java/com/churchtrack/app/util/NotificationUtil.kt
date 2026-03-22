package com.churchtrack.app.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.churchtrack.app.R
import com.churchtrack.app.ui.MainActivity

object NotificationUtil {
    const val CHANNEL_ID_ALERTS = "churchtrack_alerts"
    const val CHANNEL_ID_GENERAL = "churchtrack_general"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val alertChannel = NotificationChannel(
                CHANNEL_ID_ALERTS,
                "Alertes d'absences",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications pour les absences des fidèles"
                enableVibration(true)
            }

            val generalChannel = NotificationChannel(
                CHANNEL_ID_GENERAL,
                "Notifications générales",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications générales de ChurchTrack"
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannels(listOf(alertChannel, generalChannel))
        }
    }

    fun showAbsenceAlert(context: Context, memberName: String, absenceCount: Int, notificationId: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "alerts")
        }

        val pendingIntent = PendingIntent.getActivity(
            context, notificationId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_ALERTS)
            .setSmallIcon(R.drawable.ic_alert)
            .setContentTitle("⚠️ Alerte d'absence")
            .setContentText("$memberName est absent depuis $absenceCount cultes. Prenez de ses nouvelles.")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$memberName est absent depuis $absenceCount cultes consécutifs. Veuillez prendre de ses nouvelles."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }
}
