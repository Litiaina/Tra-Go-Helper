package com.teamlitiaina.tragohelper.firebase

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.teamlitiaina.tragohelper.constants.Constants
import com.teamlitiaina.tragohelper.data.RequestData

class RequestFirebaseBackend : FirebaseBackend(){
    companion object {
        interface RequestFirebaseCallback {
            fun onRequestReceived(requestData: List<RequestData>)
        }
        fun retrieveRequestByEmail(email: String, requestFirebaseCallback: RequestFirebaseCallback) {
            val dataList = mutableListOf<RequestData>()
            database.getReference(Constants.SERVICE_REQUEST_PATH).orderByChild("vehicleOwnerEmail").equalTo(email)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        dataList.clear()
                        for (childSnapshot in snapshot.children) {
                            childSnapshot.getValue(RequestData::class.java)?.let { dataList.add(it) }
                        }
                        Log.d("RequestFirebaseBackend", "Received ${dataList.size} notifications")
                        requestFirebaseCallback.onRequestReceived(dataList)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("RequestFirebaseBackend", "Error: ${error.message}")
                    }
                })
        }
        fun beingRequest(requestData: RequestData) {
            requestData.requestUID?.let { it ->
                database.getReference(Constants.SERVICE_REQUEST_PATH).child(it).setValue(requestData).addOnSuccessListener {
                    Log.i("RequestFirebaseBackend","Request sent to $it")
                }.addOnFailureListener {
                    Log.e("RequestFirebaseBackend","Failed to send request to: $it")
                }
            }
        }
    }
}