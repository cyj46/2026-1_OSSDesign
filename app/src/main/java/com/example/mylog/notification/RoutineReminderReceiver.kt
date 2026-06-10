package com.example.mylog.notification

import android.Manifest
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.mylog.R

class RoutineReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        RoutineNotificationScheduler().ensureChannel(context)
        val userId = intent.getLongExtra(RoutineNotificationScheduler.EXTRA_USER_ID, 0L)
        val message = intent.getStringExtra(RoutineNotificationScheduler.EXTRA_MESSAGE)
            ?: "오늘의 루틴을 체크해 보세요."
        val notification = NotificationCompat.Builder(context, RoutineNotificationScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Mylog 루틴 알림")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(userId.toInt().coerceAtLeast(1), notification)
    }
}
