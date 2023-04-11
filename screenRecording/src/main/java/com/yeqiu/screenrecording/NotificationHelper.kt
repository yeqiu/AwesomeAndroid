package com.yeqiu.screenrecording

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat

class NotificationHelper {

    private val channelName = "录屏通知"


    fun create(context: Context, icon: Int = R.drawable.icon_app): Notification {

        //创建通知渠道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(channelName, channelName, NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.setShowBadge(false)
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(notificationChannel)
        }


        val appName = context.packageManager.getApplicationInfo(
            context.packageName,
            PackageManager.GET_META_DATA
        ).loadLabel(context.packageManager).toString()

        return NotificationCompat.Builder(context, channelName)
            .setContentTitle(appName)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(icon)
            .setOngoing(true)
            .setContentText("屏幕录制服务开启")
            .build()
    }


}