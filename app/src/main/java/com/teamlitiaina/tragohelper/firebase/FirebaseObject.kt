package com.teamlitiaina.tragohelper.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.teamlitiaina.tragohelper.data.UserData

class FirebaseObject {

    companion object {
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        interface FirebaseCallback {
            fun onDataReceived(data: UserData)
        }
        fun retrieveData(referencePath: String, firebaseCallback: FirebaseCallback) {
            database.getReference(referencePath).child(auth.currentUser?.uid.toString()).get().addOnSuccessListener {
                if (it.exists())
                    firebaseCallback.onDataReceived(it.getValue(UserData::class.java) ?: UserData())
            }
        }
        fun retrieveUserByEmail(path: String, email: String, firebaseCallback: FirebaseCallback) {
            database.getReference(path).orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (snapshot in dataSnapshot.children) {
                        val userData = snapshot.getValue(UserData::class.java)
                        if (userData != null) {
                            firebaseCallback.onDataReceived(userData)
                            break
                        }
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("FirebaseError", "Error: ${databaseError.message}")
                }
            })
        }
        fun retrieveLocationByEmail(email: String, firebaseCallback: FirebaseCallback) {
            database.getReference("vehicleOwnerLocation").orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (snapshot in dataSnapshot.children) {
                        val userData = snapshot.getValue(UserData::class.java)
                        if (userData != null) {
                            firebaseCallback.onDataReceived(userData)
                            break
                        }
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("FirebaseError", "Error: ${databaseError.message}")
                }
            })
        }
    }
}