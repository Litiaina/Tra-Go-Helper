package com.teamlitiaina.tragohelper.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.teamlitiaina.tragohelper.data.UserData

class FirebaseObject {

    companion object {
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        private lateinit var databaseReference: DatabaseReference
        interface FirebaseCallback {
            fun onDataReceived(data: UserData)
        }
        fun retrieveData(referencePath: String, firebaseCallback: FirebaseCallback) {
            databaseReference = database.getReference(referencePath)
            databaseReference.child(auth.currentUser?.uid.toString()).get().addOnSuccessListener {
                if (it.exists())
                    firebaseCallback.onDataReceived(it.getValue(UserData::class.java) ?: UserData())
            }
        }
        fun retrieveUserByEmail(email: String, firebaseCallback: FirebaseCallback) {
            databaseReference = database.getReference("vehicleOwner")
            val query = databaseReference.orderByChild("email").equalTo(email)
            query.addListenerForSingleValueEvent(object : ValueEventListener {
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