package com.teamlitiaina.tragohelper.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.teamlitiaina.tragohelper.data.UserData

class FirebaseObject {

    companion object {
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        interface FirebaseCallback {
            fun onDataReceived(data: UserData)
        }
        fun retrieveData(path: String, firebaseCallback: FirebaseCallback) {
            val databaseReference: DatabaseReference = database.getReference(path)
            databaseReference.child(auth.currentUser?.uid.toString()).get().addOnSuccessListener {
                if (it.exists())
                    firebaseCallback.onDataReceived(it.getValue(UserData::class.java) ?: UserData())
            }
        }

    }
}