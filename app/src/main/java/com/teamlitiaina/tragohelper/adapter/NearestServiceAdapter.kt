package com.teamlitiaina.tragohelper.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import com.teamlitiaina.tragohelper.activity.MainActivity
import com.teamlitiaina.tragohelper.data.LocationData
import com.teamlitiaina.tragohelper.data.UserData
import com.teamlitiaina.tragohelper.databinding.ItemLayoutSelectServiceBinding
import com.teamlitiaina.tragohelper.firebase.FirebaseBackend
import com.teamlitiaina.tragohelper.firebase.NotificationFirebaseBackend

class NearestServiceAdapter(private val dataArray: List<UserData>, private val activity: Activity) : RecyclerView.Adapter<NearestServiceAdapter.ViewHolder>() {

    interface DataReceivedListener {
        fun onDataReceived(distance: String, position: Int)
    }

    private var dataReceivedListener: DataReceivedListener? = null

    class ViewHolder(val binding: ItemLayoutSelectServiceBinding) : RecyclerView.ViewHolder(binding.root) {
        var currentData: UserData? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemLayoutSelectServiceBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = dataArray[position]
        holder.currentData = currentItem
        holder.binding.nameTextView.text = currentItem.name
        holder.binding.addressTextView.text = currentItem.phoneNumber
        holder.binding.viewCardView.setOnClickListener {
            if (MainActivity.currentUser?.email == currentItem.email) {
                Toast.makeText(activity, "Current User", Toast.LENGTH_SHORT).show()
            } else {
                MainActivity.mapFragment?.setDestinationRoute(currentItem.email.toString())
            }
        }
        FirebaseBackend.retrievedAllLocationData("vehicleOwnerLocation", object : FirebaseBackend.Companion.FirebaseCallback {
            override fun onUserDataReceived(data: UserData) {}
            override fun onLocationDataReceived(data: LocationData) {}
            override fun onAllDataReceived(dataArray: List<UserData>) {}
            override fun onAllLocationDataReceived(dataArray: List<LocationData>) {
                for (data in dataArray) {
                    if (data.email == currentItem.email) {
                        if(LatLng(MainActivity.sharedPreferences.getString("latitude", "")!!.toDouble(),MainActivity.sharedPreferences.getString("longitude", "")!!.toDouble()) != LatLng(data.latitude!!.toDouble(), data.longitude!!.toDouble())) {
                            MainActivity.mapFragment?.getDistance(
                                MainActivity.sharedPreferences.getString("latitude", "")!!.toDouble(),
                                MainActivity.sharedPreferences.getString("longitude", "")!!.toDouble(),
                                data.latitude.toDouble(),
                                data.longitude.toDouble()
                            ) { distance ->
                                if (distance != null && holder.currentData?.email == currentItem.email) {
                                    dataReceivedListener?.onDataReceived(distance, holder.adapterPosition)
                                }
                            }
                        }
                    }
                }
            }
        })
    }

    override fun getItemCount(): Int {
        return dataArray.size
    }

    fun setDataReceivedListener(listener: DataReceivedListener) {
        this.dataReceivedListener = listener
    }
}