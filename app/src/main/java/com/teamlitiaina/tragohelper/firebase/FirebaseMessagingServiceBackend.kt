package com.teamlitiaina.tragohelper.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_ONE_SHOT
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.teamlitiaina.tragohelper.R
import com.teamlitiaina.tragohelper.activity.MainActivity
import kotlin.random.Random

const val CHANNEL_ID = "notification_channel"

class FirebaseMessagingServiceBackend : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val intent = Intent(this, MainActivity::class.java)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationID = Random.nextInt()

        createNotificationChannel(notificationManager)

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(message.data["title"])
            .setContentText(message.data["message"])
            .setSmallIcon(R.drawable.tragohelper_logo_img)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationID, notification)
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channelName = "TraGoHelperNotificationChannel"
        val channel = NotificationChannel(CHANNEL_ID, channelName, IMPORTANCE_HIGH).apply {
            description = "Notifications for the TraGoHelper users"
            enableLights(true)
            lightColor = Color.GREEN
        }
        notificationManager.createNotificationChannel(channel)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        Log.d("MyFirebaseMessaging", "Refreshed token: $token")

        if (FirebaseBackend.auth.currentUser?.uid != null) {
            updateTokenInFirebase(token)
        }
    }

    companion object {
        fun updateTokenInFirebase(token: String) {
            val userId = FirebaseBackend.auth.currentUser?.uid.toString()
            val database = FirebaseBackend.database
            val userReference = database.getReference("vehicleOwner").child(userId)

            userReference.child("token").setValue(token)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("MyFirebaseMessaging", "Token updated successfully")
                    } else {
                        Log.e("MyFirebaseMessaging", "Failed to update token: ${task.exception}")
                    }
                }
        }
    }

}