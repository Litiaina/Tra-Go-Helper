package com.teamlitiaina.tragohelper.firebase

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.teamlitiaina.tragohelper.data.NotificationData
import com.teamlitiaina.tragohelper.datetime.DateTime

class NotificationFirebaseBackend : FirebaseBackend() {
    companion object {
        interface NotificationFirebaseCallback {
            fun onNotificationReceived(notificationData: List<NotificationData>)
        }
        fun retrieveNotificationByEmail(email: String, notificationFirebaseCallback: NotificationFirebaseCallback) {
            val dataList = mutableListOf<NotificationData>()
            database.getReference("vehicleOwnerNotification").orderByChild("receiverEmail").equalTo(email)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        dataList.clear()
                        for (childSnapshot in snapshot.children) {
                            childSnapshot.getValue(NotificationData::class.java)?.let { dataList.add(it) }
                        }
                        Log.d("NotificationFirebaseBackend", "Received ${dataList.size} notifications")
                        notificationFirebaseCallback.onNotificationReceived(dataList)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("FirebaseError", "Error: ${error.message}")
                    }
                })
        }
        fun addNotificationToUser(receiverEmail: String, title: String, information: String) {
            database.getReference("vehicleOwnerNotification").child(DateTime.getCurrentDateTime()).setValue(
                NotificationData(receiverEmail, title, information, DateTime.getCurrentDateTime()
            )).addOnSuccessListener {
                Log.i("NotificationFirebaseBackend","Succeeded to send notification to: $receiverEmail")
            }.addOnFailureListener {
                Log.e("NotificationFirebaseBackendError","Failed to send notification to: $receiverEmail")
            }
        }
    }
}