package com.teamlitiaina.tragohelper.fragment

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.teamlitiaina.tragohelper.R
import com.teamlitiaina.tragohelper.activity.MainActivity
import com.teamlitiaina.tragohelper.data.LocationData
import com.teamlitiaina.tragohelper.data.RequestData
import com.teamlitiaina.tragohelper.data.ServiceProviderData
import com.teamlitiaina.tragohelper.data.UserData
import com.teamlitiaina.tragohelper.databinding.CustomPopupRequestProgressInfoBinding
import com.teamlitiaina.tragohelper.databinding.FragmentRequestBinding
import com.teamlitiaina.tragohelper.dialog.PopupManager.Companion.displayInformationPopupWindow
import com.teamlitiaina.tragohelper.firebase.FirebaseBackend
import com.teamlitiaina.tragohelper.firebase.FirebaseBackend.Companion.retrieveServiceProviderDataByEmailRealTime
import com.teamlitiaina.tragohelper.firebase.FirebaseBackend.Companion.retrieveServiceProviderLocationDataByEmailRealTime
import com.teamlitiaina.tragohelper.firebase.RequestFirebaseBackend

class RequestFragment : Fragment(), RequestFirebaseBackend.Companion.RequestFirebaseCallback, FirebaseBackend.Companion.FirebaseCallback {

    private var _binding: FragmentRequestBinding? = null
    private val binding get() = _binding!!
    private var currentServiceProviderData = mutableListOf<ServiceProviderData>()
    private var currentServiceProviderLocation = mutableListOf<LocationData>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRequestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        RequestFirebaseBackend.retrieveRequestByEmail(MainActivity.currentUser?.email.toString(), this)

        binding.trackProgressConstraintLayout.setOnClickListener {
            val popupBinding: CustomPopupRequestProgressInfoBinding = CustomPopupRequestProgressInfoBinding.inflate(LayoutInflater.from(requireContext()))
            val popupWindow = displayInformationPopupWindow(requireContext(), popupBinding, true, 0)
            popupBinding.okCardView.setOnClickListener {
                popupWindow.dismiss()
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        clearData()
        _binding = null
    }

    override fun onRequestReceived(requestData: List<RequestData>) {
        if (!isAdded) {
            return
        }
        for (data in requestData) {
            if (data.vehicleOwnerEmail == MainActivity.currentUser?.email) {
                data.serviceProviderEmail?.let {
                    retrieveServiceProviderDataByEmailRealTime(it, this)
                    retrieveServiceProviderLocationDataByEmailRealTime(it, this)
                    binding.requestInfoCardView.visibility = View.VISIBLE
                }
                data.progress?.let {
                    binding.autoPartsProgressTrackingProgressBar.progress = it.toInt()
                    if (it.toInt() >= 25) {
                        binding.receivedCardView.visibility = View.INVISIBLE
                        binding.receivedImageView.setColorFilter(Color.WHITE)
                        binding.receivedStageLinearLayout.visibility = View.VISIBLE
                        binding.requestStageLinearLayout.visibility = View.GONE
                        binding.vehicleDetailsEditText.setText(data.vehicleType)
                        binding.otherGeneralInformationEditText.setText(data.otherNotes)
                    } else {
                        binding.receivedCardView.visibility = View.VISIBLE
                        binding.receivedImageView.setColorFilter(Color.BLACK)
                        binding.receivedStageLinearLayout.visibility = View.GONE
                        binding.requestStageLinearLayout.visibility = View.VISIBLE
                    }
                    if (it.toInt() >= 50) {
                        binding.enRouteCardView.visibility = View.INVISIBLE
                        binding.enRouteImageView.setColorFilter(Color.WHITE)
                    } else {
                        binding.enRouteCardView.visibility = View.VISIBLE
                        binding.enRouteImageView.setColorFilter(Color.BLACK)
                    }
                    if (it.toInt() >= 75) {
                        binding.repairCardView.visibility = View.INVISIBLE
                        binding.repairImageView.setColorFilter(Color.WHITE)
                    } else {
                        binding.repairCardView.visibility = View.VISIBLE
                        binding.repairImageView.setColorFilter(Color.BLACK)
                    }
                    if (it.toInt() >= 100) {
                        binding.completeCardView.visibility = View.INVISIBLE
                        binding.completeImageView.setColorFilter(Color.WHITE)
                    } else {
                        binding.completeCardView.visibility = View.VISIBLE
                        binding.completeImageView.setColorFilter(Color.BLACK)
                    }
                }
            }
        }
    }
    @SuppressLint("SetTextI18n")
    override fun onServiceProviderDataReceived(data: ServiceProviderData) {
        if (!isAdded) {
            return
        }
        currentServiceProviderData.add(data)
        binding.serviceAddressTextView.text = data.address
        binding.servicePhoneTextView.text = data.phoneNumber
        binding.serviceProviderNameTextView.text = data.name
        binding.serviceTypeTextView.text = "Service Type: ${data.type}"
        if (data.profilePicture == "" || data.profilePicture == null) {
            Glide.with(this)
                .load(R.drawable.temporary_profile_picture)
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                .into(binding.serviceProviderProfilePictureImageView)
        } else {
            Glide.with(this)
                .load(data.profilePicture)
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                .into(binding.serviceProviderProfilePictureImageView)
        }
    }

    override fun onLocationDataReceived(data: LocationData) {
        if (!isAdded) {
            return
        }
        currentServiceProviderLocation.add(data)
        binding.noRequestRelativeLayout.isVisible = currentServiceProviderData.size <= 0
        MainActivity.mapFragment?.filteredServiceProviderData = currentServiceProviderData
        MainActivity.mapFragment?.filteredServiceProviderLocation = currentServiceProviderLocation
        MainActivity.mapFragment?.addOrUpdateMarkers(currentServiceProviderData, currentServiceProviderLocation)
        data.email?.let {
            MainActivity.mapFragment?.setDestinationRoute(it)
        }
    }

    override fun onUserDataReceived(data: UserData) {}
    override fun onAllUserDataReceived(dataArray: List<UserData>) {}
    override fun onAllServiceProviderDataReceived(dataArray: List<ServiceProviderData>) {}
    override fun onAllLocationDataReceived(dataArray: List<LocationData>) {}

    private fun clearData() {
        retrieveServiceProviderDataByEmailRealTime("", this)
        retrieveServiceProviderLocationDataByEmailRealTime("", this)
        currentServiceProviderData.clear()
        currentServiceProviderLocation.clear()
        MainActivity.mapFragment?.filteredServiceProviderData?.clear()
        MainActivity.mapFragment?.filteredServiceProviderLocation?.clear()
        MainActivity.mapFragment?.clearMap()
    }

}