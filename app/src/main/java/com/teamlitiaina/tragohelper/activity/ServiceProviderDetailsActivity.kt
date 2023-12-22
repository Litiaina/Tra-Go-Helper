package com.teamlitiaina.tragohelper.activity

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.teamlitiaina.tragohelper.R
import com.teamlitiaina.tragohelper.adapter.ServiceOffersAdapter
import com.teamlitiaina.tragohelper.data.RequestData
import com.teamlitiaina.tragohelper.data.ServiceProviderData
import com.teamlitiaina.tragohelper.databinding.ActivityServiceProviderDetailsBinding
import com.teamlitiaina.tragohelper.firebase.RequestFirebaseBackend
import com.teamlitiaina.tragohelper.fragment.FragmentChanger
import com.teamlitiaina.tragohelper.fragment.ServiceProviderDetailsFragment
import com.teamlitiaina.tragohelper.utility.NumberUtils.Companion.generateRandom10DigitNumber

class ServiceProviderDetailsActivity : AppCompatActivity() {

    private lateinit var binding : ActivityServiceProviderDetailsBinding

    companion object {
        var currentServiceProviderData: ServiceProviderData? = null
        var serviceProviderDetailsFragment: ServiceProviderDetailsFragment? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServiceProviderDetailsBinding.inflate(layoutInflater)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        )
        setContentView(binding.root)

        serviceProviderDetailsFragment = ServiceProviderDetailsFragment()

        FragmentChanger.replaceFragment(this, serviceProviderDetailsFragment!!, binding.shopDetailsLayout.id)

        val serviceProviderDataJson = intent.getStringExtra("serviceProviderData")
        if (serviceProviderDataJson != null) {
            val serviceProviderData = Gson().fromJson(serviceProviderDataJson, ServiceProviderData::class.java)
            currentServiceProviderData = serviceProviderData
            binding.serviceProviderHeaderNameTextView.text = serviceProviderData.name?.split(" ")?.firstOrNull() ?: ""
            binding.serviceProviderNameTextView.text = serviceProviderData.name
            if (serviceProviderData.profilePicture == "" || serviceProviderData.profilePicture == null) {
                Glide.with(this)
                    .load(R.drawable.temporary_profile_picture)
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                    .into(binding.serviceProviderProfilePictureImageView)
            } else {
                Glide.with(this)
                    .load(serviceProviderData.profilePicture)
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                    .into(binding.serviceProviderProfilePictureImageView)
            }
        }

        binding.backImageButton.setOnClickListener { finish() }

        binding.requestNowButton.setOnClickListener {
            serviceProviderDetailsFragment?.getVehicleInformation { info ->
                if (info != "") {
                    serviceProviderDetailsFragment?.getOtherInformation { otherInfo ->
                        if (otherInfo != "") {
                            RequestFirebaseBackend.beingRequest(
                                RequestData(
                                    MainActivity.currentUser?.userUID,
                                    MainActivity.currentUser?.email,
                                    currentServiceProviderData?.email,
                                    info,
                                    otherInfo,
                                    "0"
                                )
                            )
                        } else {
                            RequestFirebaseBackend.beingRequest(
                                RequestData(
                                    MainActivity.currentUser?.userUID,
                                    MainActivity.currentUser?.email,
                                    currentServiceProviderData?.email,
                                    info,
                                    "none",
                                    "0"
                                )
                            )
                        }
                    }
                } else {
                    Toast.makeText(this, "Vehicle information must not be blank", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }
}