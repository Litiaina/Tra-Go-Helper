package com.teamlitiaina.tragohelper.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.teamlitiaina.tragohelper.constants.Constants
import com.teamlitiaina.tragohelper.data.LocationData
import com.teamlitiaina.tragohelper.data.ServiceProviderData
import com.teamlitiaina.tragohelper.data.UserData

open class FirebaseBackend {
    companion object {
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val storageReference = FirebaseStorage.getInstance().reference
        interface FirebaseCallback {
            fun onUserDataReceived(data: UserData)
            fun onAllUserDataReceived(dataArray: List<UserData>)
            fun onServiceProviderDataReceived(data: ServiceProviderData)
            fun onLocationDataReceived(data: LocationData)
            fun onAllServiceProviderDataReceived(dataArray: List<ServiceProviderData>)
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

        fun retrieveAllUserData(referencePath: String, firebaseCallback: FirebaseCallback) {
            val dataList = mutableListOf<ServiceProviderData>()
            database.getReference(referencePath).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    dataList.clear()
                    for (snapshot in dataSnapshot.children) {
                        snapshot.getValue(ServiceProviderData::class.java)?.let { dataList.add(it) }
                    }
                    firebaseCallback.onAllServiceProviderDataReceived(dataList)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", "Error retrieving data: $error")
                }
            })
        }

        fun retrieveAllServiceProviderData(referencePath: String, firebaseCallback: FirebaseCallback) {
            val dataList = mutableListOf<ServiceProviderData>()
            database.getReference(referencePath).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    dataList.clear()
                    for (snapshot in dataSnapshot.children) {
                        snapshot.getValue(ServiceProviderData::class.java)?.let { dataList.add(it) }
                    }
                    firebaseCallback.onAllServiceProviderDataReceived(dataList)
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
        fun retrieveServiceProviderDataByEmailRealTime(email: String, firebaseCallback: FirebaseCallback) {
            database.getReference(Constants.SERVICE_PROVIDER_PATH).orderByChild("email").equalTo(email).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach { data ->
                        val userData = data.getValue(ServiceProviderData::class.java)
                        userData?.let { firebaseCallback.onServiceProviderDataReceived(it) }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", "Error: ${error.message}")
                }
            })
        }
        fun retrieveServiceProviderLocationDataByEmailRealTime(email: String, firebaseCallback: FirebaseCallback) {
            database.getReference(Constants.SERVICE_PROVIDER_LOCATION_PATH).orderByChild("email").equalTo(email).addValueEventListener(object : ValueEventListener {
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