package com.teamlitiaina.tragohelper.firebase

import android.content.Context
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.teamlitiaina.tragohelper.data.VehicleOwnerUserData

class FirebaseObject {

    companion object {
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        interface FirebaseCallback {
            fun onDataReceived(data: VehicleOwnerUserData)
        }
        fun retrieveData(path: String, firebaseCallback: FirebaseCallback) {
            val databaseReference: DatabaseReference = database.getReference(path)
            databaseReference.child(auth.currentUser?.uid.toString()).get().addOnSuccessListener {
                if (it.exists())
                    firebaseCallback.onDataReceived(it.getValue(VehicleOwnerUserData::class.java) ?: VehicleOwnerUserData())
            }
        }

    }
}