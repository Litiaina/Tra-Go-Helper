package com.teamlitiaina.tragohelper.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.teamlitiaina.tragohelper.data.LocationData
import com.teamlitiaina.tragohelper.data.UserData

open class FirebaseBackend {
    companion object {
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val storageReference = FirebaseStorage.getInstance().reference
        interface FirebaseCallback {
            fun onUserDataReceived(data: UserData)
            fun onLocationDataReceived(data: LocationData)
            fun onAllDataReceived(dataArray: List<UserData>)
            fun onAllLocationDataReceived(dataArray: List<LocationData>)
        }
        fun retrieveData(referencePath: String, firebaseCallback: FirebaseCallback) {
            database.getReference(referencePath).child(auth.currentUser?.uid.toString()).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val userData = snapshot.getValue(UserData::class.java) ?: UserData()
                        firebaseCallback.onUserDataReceived(userData)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", "Error retrieving data: $error")
                }
            })
        }

        fun retrieveAllData(referencePath: String, firebaseCallback: FirebaseCallback) {
            val dataList = mutableListOf<UserData>()
            database.getReference(referencePath).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    dataList.clear()
                    for (snapshot in dataSnapshot.children) {
                        snapshot.getValue(UserData::class.java)?.let { dataList.add(it) }
                    }
                    firebaseCallback.onAllDataReceived(dataList)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", "Error retrieving data: $error")
                }
            })
        }
        fun retrievedAllLocationData(referencePath: String, firebaseCallback: FirebaseCallback) {
            val dataList = mutableListOf<LocationData>()
            database.getReference(referencePath).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    dataList.clear()
                    for (snapshot in dataSnapshot.children) {
                        snapshot.getValue(LocationData::class.java)?.let { dataList.add(it) }
                    }
                    firebaseCallback.onAllLocationDataReceived(dataList)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", "Error retrieving data: $error")
                }
            })
        }

        fun retrieveUserDataByEmail(path: String, email: String, firebaseCallback: FirebaseCallback) {
            database.getReference(path).orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (childSnapshot in snapshot.children) {
                        val userData = childSnapshot.getValue(UserData::class.java)
                        userData?.let { firebaseCallback.onUserDataReceived(it) }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", "Error: ${error.message}")
                }
            })
        }
        fun retrieveUserDataByEmailRealTime(email: String, firebaseCallback: FirebaseCallback) {
            database.getReference("vehicleOwner").orderByChild("email").equalTo(email).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach { data ->
                        val userData = data.getValue(UserData::class.java)
                        userData?.let { firebaseCallback.onUserDataReceived(it) }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", "Error: ${error.message}")
                }
            })
        }
        fun retrieveLocationDataByEmail(email: String, firebaseCallback: FirebaseCallback) {
            database.getReference("vehicleOwnerLocation").orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (childSnapshot in snapshot.children) {
                        val locationData = childSnapshot.getValue(LocationData::class.java)
                        locationData?.let { firebaseCallback.onLocationDataReceived(it) }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", "Error: ${error.message}")
                }
            })
        }
        fun retrieveLocationDataByEmailRealTime(email: String, firebaseCallback: FirebaseCallback) {
            database.getReference("vehicleOwnerLocation").orderByChild("email").equalTo(email).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach { data ->
                        val locationData = data.getValue(LocationData::class.java)
                        locationData?.let { firebaseCallback.onLocationDataReceived(it) }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", "Error: ${error.message}")
                }
            })
        }
    }
}