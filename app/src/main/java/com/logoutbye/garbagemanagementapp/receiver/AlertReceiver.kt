package com.logoutbye.garbagemanagementapp.receiver

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.logoutbye.garbagemanagementapp.activities.EmployeeHomeActivity
import com.logoutbye.garbagemanagementapp.utils.AttendanceUtil

class AlertReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        val myIntentNotifiaction =
            Intent(context, EmployeeHomeActivity::class.java)
        myIntentNotifiaction.action = Intent.ACTION_MAIN
        myIntentNotifiaction.addCategory(Intent.CATEGORY_LAUNCHER)
        myIntentNotifiaction.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val pi = PendingIntent.getActivity(
            context,
            0,
            myIntentNotifiaction,
            PendingIntent.FLAG_CANCEL_CURRENT
        )

        val notificationHelper = NotificationHelper(context,pi)
        val nb =
            notificationHelper.channelNotification
        notificationHelper.manager!!.notify(1, nb.build())



        val attendanceUtil=AttendanceUtil()
        attendanceUtil.submitAttendancePrefrence(context,false)
    }
}