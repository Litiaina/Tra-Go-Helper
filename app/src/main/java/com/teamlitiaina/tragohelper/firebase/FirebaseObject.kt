package com.teamlitiaina.tragohelper.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.teamlitiaina.tragohelper.data.LocationData
import com.teamlitiaina.tragohelper.data.UserData

class FirebaseObject {

    companion object {
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        interface FirebaseCallback {
            fun onUserDataReceived(data: UserData)
            fun onLocationDataReceived(data: LocationData)
        }
        fun retrieveData(referencePath: String, firebaseCallback: FirebaseCallback) {
            database.getReference(referencePath).child(auth.currentUser?.uid.toString()).get().addOnSuccessListener {
                if (it.exists())
                    firebaseCallback.onUserDataReceived(it.getValue(UserData::class.java) ?: UserData())
            }
        }
        fun retrieveUserDataByEmail(path: String, email: String, firebaseCallback: FirebaseCallback) {
            database.getReference(path).orderByChild("email").equalTo(email).addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    snapshot.getValue(UserData::class.java)?.let { firebaseCallback.onUserDataReceived(it) }
                }
                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    snapshot.getValue(UserData::class.java)?.let { firebaseCallback.onUserDataReceived(it) }
                }
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", "Error: ${error.message}")
                }
            })
        }
        fun retrieveLocationDataByEmail(email: String, firebaseCallback: FirebaseCallback) {
            database.getReference("vehicleOwnerLocation").orderByChild("email").equalTo(email).addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    snapshot.getValue(LocationData::class.java)?.let { firebaseCallback.onLocationDataReceived(it) }
                }
                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    snapshot.getValue(LocationData::class.java)?.let { firebaseCallback.onLocationDataReceived(it) }
                }
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", "Error: ${error.message}")
                }
            })
        }
    }
}