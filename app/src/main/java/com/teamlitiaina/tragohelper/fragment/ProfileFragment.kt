package com.teamlitiaina.tragohelper.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.teamlitiaina.tragohelper.R
import com.teamlitiaina.tragohelper.activity.EditInformationActivity
import com.teamlitiaina.tragohelper.activity.LoginActivity
import com.teamlitiaina.tragohelper.activity.MainActivity
import com.teamlitiaina.tragohelper.data.LocationData
import com.teamlitiaina.tragohelper.data.UserData
import com.teamlitiaina.tragohelper.databinding.FragmentProfileBinding
import com.teamlitiaina.tragohelper.firebase.FirebaseBackend
import com.teamlitiaina.tragohelper.firebase.FirebaseMessagingServiceBackend


class ProfileFragment : Fragment(), FirebaseBackend.Companion.FirebaseCallback {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        FirebaseBackend.retrieveData("vehicleOwner", this)

        binding.signOutImageButton.setOnClickListener {
            FirebaseMessagingServiceBackend.updateTokenInFirebase("")
            FirebaseBackend.auth.signOut()
            with(MainActivity.sharedPreferences.edit()) {
                putString("auth", "")
                apply()
            }
            MainActivity.mapFragment = null
            MainActivity.currentUser = null
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }

        binding.personalInformationCardView.setOnClickListener {
            startActivity(Intent(requireContext(), EditInformationActivity::class.java))
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onUserDataReceived(data: UserData) {
        if (!isAdded) {
            return
        }
        binding.nameTextView.text = MainActivity.currentUser?.name
        binding.emailTextView.text = MainActivity.currentUser?.email
        if (data.profilePicture == "" || data.profilePicture == null) {
            Glide.with(this)
                .load(R.drawable.temporary_profile_picture)
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                .into(binding.profilePictureCircleImageView)
        } else {
            Glide.with(this)
                .load(data.profilePicture)
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                .into(binding.profilePictureCircleImageView)
        }
    }

    override fun onLocationDataReceived(data: LocationData) {}

    override fun onAllDataReceived(dataArray: List<UserData>) {}

    override fun onAllLocationDataReceived(dataArray: List<LocationData>) {}

}