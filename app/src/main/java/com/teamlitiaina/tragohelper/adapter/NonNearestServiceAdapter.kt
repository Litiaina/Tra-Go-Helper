package com.teamlitiaina.tragohelper.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.teamlitiaina.tragohelper.R
import com.teamlitiaina.tragohelper.activity.MainActivity
import com.teamlitiaina.tragohelper.activity.ServiceProviderDetailsActivity
import com.teamlitiaina.tragohelper.constants.Constants
import com.teamlitiaina.tragohelper.data.LocationData
import com.teamlitiaina.tragohelper.data.ServiceProviderData
import com.teamlitiaina.tragohelper.data.UserData
import com.teamlitiaina.tragohelper.databinding.ItemLayoutSelectServiceBinding
import com.teamlitiaina.tragohelper.firebase.FirebaseBackend
import com.teamlitiaina.tragohelper.utility.LocationUtils

class NonNearestServiceAdapter(private val dataArray: List<ServiceProviderData>, private val activity: Activity) : RecyclerView.Adapter<NonNearestServiceAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemLayoutSelectServiceBinding) : RecyclerView.ViewHolder(binding.root) {
        var currentData: ServiceProviderData? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemLayoutSelectServiceBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = dataArray[position]
        holder.currentData = currentItem
        holder.binding.nameTextView.text = currentItem.name
        holder.binding.addressTextView.text = currentItem.phoneNumber
        if (currentItem.profilePicture == "" || currentItem.profilePicture == null) {
            Glide.with(activity)
                .load(R.drawable.temporary_profile_picture)
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                .into(holder.binding.profilePictureCircleImageView)
        } else {
            Glide.with(activity)
                .load(currentItem.profilePicture)
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                .into(holder.binding.profilePictureCircleImageView)
        }
        holder.binding.viewCardView.setOnClickListener {
            if (MainActivity.currentUser?.email == currentItem.email) {
                Toast.makeText(activity, "Current User", Toast.LENGTH_SHORT).show()
            } else {
                MainActivity.mapFragment?.setDestinationRoute(currentItem.email.toString())
            }
        }
        holder.binding.parentCardView.setOnClickListener {
            val intent = Intent(activity, ServiceProviderDetailsActivity::class.java)
            val serviceProviderDataJson = Gson().toJson(holder.currentData)
            intent.putExtra("serviceProviderData", serviceProviderDataJson)
            activity.startActivity(intent)
        }
        FirebaseBackend.retrievedAllLocationData(Constants.SERVICE_PROVIDER_LOCATION_PATH, object : FirebaseBackend.Companion.FirebaseCallback {
            override fun onUserDataReceived(data: UserData) {}
            override fun onAllUserDataReceived(dataArray: List<UserData>) {}
            override fun onServiceProviderDataReceived(data: ServiceProviderData) {}
            override fun onLocationDataReceived(data: LocationData) {}
            override fun onAllServiceProviderDataReceived(dataArray: List<ServiceProviderData>) {}
            override fun onAllLocationDataReceived(dataArray: List<LocationData>) {
                for (data in dataArray) {
                    if (data.email == currentItem.email) {
                        if(LatLng(MainActivity.sharedPreferences.getString("latitude", "")!!.toDouble(),MainActivity.sharedPreferences.getString("longitude", "")!!.toDouble()) != LatLng(data.latitude!!.toDouble(), data.longitude!!.toDouble())) {
                            LocationUtils.getDistanceHaversine(
                                MainActivity.sharedPreferences.getString("latitude", "")!!.toDouble(),
                                MainActivity.sharedPreferences.getString("longitude", "")!!.toDouble(),
                                data.latitude.toDouble(),
                                data.longitude.toDouble()
                            ) { distance ->
                                if (distance != null && holder.currentData?.email == currentItem.email) {
                                    holder.binding.distanceTextView.text = LocationUtils.formatDistance(distance)
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

}