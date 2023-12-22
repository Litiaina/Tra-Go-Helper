package com.teamlitiaina.tragohelper.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.teamlitiaina.tragohelper.R
import com.teamlitiaina.tragohelper.activity.ServiceProviderDetailsActivity
import com.teamlitiaina.tragohelper.databinding.FragmentServiceProviderDetailsBinding

class ServiceProviderDetailsFragment : Fragment() {

    private var _binding: FragmentServiceProviderDetailsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentServiceProviderDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.serviceAddressTextView.text = ServiceProviderDetailsActivity.currentServiceProviderData?.address
        binding.servicePhoneTextView.text = ServiceProviderDetailsActivity.currentServiceProviderData?.phoneNumber
        binding.serviceTypeTextView.text = "Service Type: ${ServiceProviderDetailsActivity.currentServiceProviderData?.type}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    fun getVehicleInformation(callback: (String?) -> Unit) {
        callback(binding.vehicleDetailsEditText.text.toString())
    }
    fun getOtherInformation(callback: (String?) -> Unit) {
        callback(binding.otherGeneralInformationEditText.text.toString())
    }

}