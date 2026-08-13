package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

class NotificationHelper(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Budget Alerts & Spending Warnings",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when spending approaches or exceeds category budget limits"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendBudgetAlert(
        categoryName: String,
        spentAmount: Double,
        limitAmount: Double,
        currencySymbol: String,
        percent: Int
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val isExceeded = spentAmount >= limitAmount
        val title = if (isExceeded) {
            "🚨 Budget Exceeded: $categoryName"
        } else {
            "⚠️ Budget Warning: $categoryName ($percent%)"
        }

        val message = if (isExceeded) {
            "You have spent $currencySymbol%.2f of your $currencySymbol%.2f monthly budget limit!".format(
                spentAmount,
                limitAmount
            )
        } else {
            "You have reached $percent% of your $categoryName budget ($currencySymbol%.2f / $currencySymbol%.2f)."
                .format(spentAmount, limitAmount)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        notificationManager.notify(categoryName.hashCode(), builder.build())
    }

    fun sendSyncNotification(deviceCount: Int, itemsSynced: Int) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("☁️ Multi-Device Cloud Sync Complete")
            .setContentText("Successfully synced $itemsSynced transactions across $deviceCount linked devices.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        notificationManager.notify(999, builder.build())
    }

    companion object {
        const val CHANNEL_ID = "vault_budget_alerts"
    }
}
