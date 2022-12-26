package com.logoutbye.garbagemanagementapp.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.logoutbye.garbagemanagementapp.MyApplication
import com.logoutbye.garbagemanagementapp.R
import com.logoutbye.garbagemanagementapp.activities.InboxForFcmActivity
import com.logoutbye.garbagemanagementapp.activities.SplashActivity
import com.logoutbye.garbagemanagementapp.utils.InBoxUtil
import com.logoutbye.garbagemanagementapp.utils.RoutPointClass
import java.util.*

open class MyFirebaseMessagingService : FirebaseMessagingService() {


    private val ADMIN_CHANNEL_ID = "admin_channel"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)


        var intent: Intent? = null

        Log.d("mmmm", "remoteMessage.action: " + remoteMessage.data.get("action"))
        Log.d("mmmm", "remoteMessage.lati: " + remoteMessage.data.get("lati"))
        Log.d("mmmm", "remoteMessage.longi: " + remoteMessage.data.get("longi"))
        Log.d("mmmm", "remoteMessage.message: " + remoteMessage.data.get("message"))

        val strAction = remoteMessage.data.get("action").toString()



        if (strAction == "trash") {

            intent = Intent(this, InboxForFcmActivity::class.java)

            val bundle = Bundle();

            val routePoints = RoutPointClass(
                0.0,
                0.0,
                remoteMessage.data.get("lati")!!.toDouble(),
                remoteMessage.data.get("longi")!!.toDouble(),
                remoteMessage.data.get("message"))


            val inboxPref = InBoxUtil()

            inboxPref.submitInBoxPrefrence(this,
                remoteMessage.data.get("lati"),
                remoteMessage.data.get("longi"),
                remoteMessage.data.get("message"))


            MyApplication.isTrashRequest = true

            bundle.putSerializable("model", routePoints);

            intent.putExtras(bundle)

        } else {
            intent = Intent(this, SplashActivity::class.java)
        }


        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        val notificationID = Random().nextInt(3000)

        /*
        Apps targeting SDK 26 or above (Android O) must implement notification channels and add its notifications
        to at least one of them.
      */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupChannels(notificationManager)
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT
        )

        val largeIcon = BitmapFactory.decodeResource(
            resources,
            R.drawable.ic_launcher2
        )

        val notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, ADMIN_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(largeIcon)
            .setContentTitle(remoteMessage.data["title"])
            .setContentText(remoteMessage.data["message"])
            .setAutoCancel(true)
            .setSound(notificationSoundUri)
            .setContentIntent(pendingIntent)

        //Set notification color to match your app color template
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.color =
                ContextCompat.getColor(applicationContext, android.R.color.background_dark)
        }
        notificationManager.notify(notificationID, notificationBuilder.build())
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun setupChannels(notificationManager: NotificationManager?) {
        val adminChannelName = "New notification"
        val adminChannelDescription = "Device to device notification"

        val adminChannel: NotificationChannel
        adminChannel = NotificationChannel(
            ADMIN_CHANNEL_ID,
            adminChannelName,
            NotificationManager.IMPORTANCE_HIGH
        )
        adminChannel.description = adminChannelDescription
        adminChannel.enableLights(true)
        adminChannel.lightColor = Color.RED
        adminChannel.enableVibration(true)
        notificationManager?.createNotificationChannel(adminChannel)
    }


}