package com.teamlitiaina.tragohelper.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.teamlitiaina.tragohelper.activity.MainActivity
import com.teamlitiaina.tragohelper.data.LocationData
import com.teamlitiaina.tragohelper.data.UserData
import com.teamlitiaina.tragohelper.databinding.ItemLayoutSelectServiceBinding
import com.teamlitiaina.tragohelper.firebase.FirebaseObject

class SelectServiceAdapter(private val dataArray: List<UserData>, private val activity: Activity) : RecyclerView.Adapter<SelectServiceAdapter.ViewHolder>() {

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
        holder.binding.addressTextView.text = currentItem.userUID
        holder.binding.viewCardView.setOnClickListener {
            if (MainActivity.currentUser?.email == currentItem.email) {
                Toast.makeText(activity, "Current User", Toast.LENGTH_SHORT).show()
            } else {
                MainActivity.mapFragment?.updateData(currentItem.email.toString())
            }
        }
        FirebaseObject.retrieveLocationDataByEmailRealTime(currentItem.email.toString(), object : FirebaseObject.Companion.FirebaseCallback {
            override fun onUserDataReceived(data: UserData) {}

            override fun onLocationDataReceived(data: LocationData) {
                if (MainActivity.currentUserLatitude != null && MainActivity.currentUserLongitude != null) {
                    MainActivity.mapFragment?.getDistanceHaversine(
                        MainActivity.currentUserLatitude!!.toDouble(),
                        MainActivity.currentUserLongitude!!.toDouble(),
                        data.latitude!!.toDouble(),
                        data.longitude!!.toDouble()
                    ) { distance ->
                        if (distance != null && holder.currentData?.email == currentItem.email) {
                            MainActivity.mapFragment?.formatDistance(distance)
                                ?.let {
                                    dataReceivedListener?.onDataReceived(
                                        it,
                                        holder.adapterPosition
                                    )
                                }
                        }
                    }
                }
//                --- Usage will increase cpu but will get accurate distance
//                if (MainActivity.currentUserLatitude != null && MainActivity.currentUserLongitude != null) {
//                    MainActivity.mapFragment?.getDistance(
//                        MainActivity.currentUserLatitude!!.toDouble(), MainActivity.currentUserLongitude!!.toDouble(), data.latitude!!.toDouble(), data.longitude!!.toDouble()
//                    ) { distance ->
//                        if (distance != null && holder.currentData?.email == currentItem.email) {
//                            dataReceivedListener?.onDataReceived(distance, holder.adapterPosition)
//                        }
//                    }
//                }
//                ---
            }

            override fun onAllDataReceived(dataArray: List<UserData>) {}
        })
    }

    override fun getItemCount(): Int {
        return dataArray.size
    }

    fun setDataReceivedListener(listener: DataReceivedListener) {
        this.dataReceivedListener = listener
    }
}