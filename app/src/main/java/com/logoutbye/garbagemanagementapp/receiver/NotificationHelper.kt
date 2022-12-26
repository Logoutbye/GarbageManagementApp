package com.logoutbye.garbagemanagementapp.receiver

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.core.app.NotificationCompat
import com.logoutbye.garbagemanagementapp.R


class NotificationHelper(base: Context?, val pi: PendingIntent) : ContextWrapper(base) {
    private var mManager: NotificationManager? = null



    @TargetApi(Build.VERSION_CODES.O)
    private fun createChannel() {

        val channel = NotificationChannel(
            channelID,
            channelName,
            NotificationManager.IMPORTANCE_HIGH
        )
        manager!!.createNotificationChannel(channel)
    }

    val manager: NotificationManager?
        get() {
            if (mManager == null) {
                mManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            }
            return mManager
        }


    val channelNotification: NotificationCompat.Builder

        get() = NotificationCompat.Builder(
            applicationContext,
            channelID
        )
            .setContentTitle("Attendance Alert!")
            .setContentText("Please open app and mark your today's attendance")
            .setContentIntent(pi)
            .setSmallIcon(R.drawable.ic_attendance)


    companion object {
        const val channelID = "channelID"
        const val channelName = "Channel Name"
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }
    }
}