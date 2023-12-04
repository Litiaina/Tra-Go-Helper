package com.teamlitiaina.tragohelper.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.teamlitiaina.tragohelper.activity.MainActivity
import com.teamlitiaina.tragohelper.adapter.NearestServiceAdapter
import com.teamlitiaina.tragohelper.adapter.NonNearestServiceAdapter
import com.teamlitiaina.tragohelper.data.LocationData
import com.teamlitiaina.tragohelper.data.UserData
import com.teamlitiaina.tragohelper.databinding.FragmentHomeBinding
import com.teamlitiaina.tragohelper.firebase.FirebaseBackend
import com.teamlitiaina.tragohelper.utility.LocationUtils

class HomeFragment : Fragment(), FirebaseBackend.Companion.FirebaseCallback {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var nearestServiceAdapter: NearestServiceAdapter? = null
    private var nonNearestServiceAdapter: NonNearestServiceAdapter? = null
    private var serviceProviderData = mutableListOf<UserData>()
    private var filteredServiceProviderData = mutableListOf<UserData>()
    private var filteredAllServiceProviderData = mutableListOf<UserData>()
    private var serviceProviderLocationData = mutableListOf<LocationData>()
    private var beginRealTimeUpdate =  false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.nearbyServicesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.nonNearbyServicesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        initializeCallBack()

        binding.addressSearchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    if (MainActivity.currentUser?.email == it) {
                        Toast.makeText(activity, "Current User", Toast.LENGTH_SHORT).show()
                    } else {
                        MainActivity.mapFragment?.setDestinationRoute(it)
                    }
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        binding.clearDestinationImageButton.setOnClickListener {
            MainActivity.mapFragment?.cancelDirections()
        }

        binding.clearMapImageButton.setOnClickListener {
            clearData()
        }

        binding.undeterminedView.setOnClickListener {
            beginRealTimeUpdate = true
            binding.noActiveNonNearbyServiceViewRelativeLayout.isVisible = false
            initializeCallBack()
            initNearestServiceAdapter()
            initNonNearestServiceAdapter()
            MainActivity.mapFragment?.userData = serviceProviderData
            MainActivity.mapFragment?.locationData = serviceProviderLocationData
            MainActivity.mapFragment?.addOrUpdateMarkers(serviceProviderData, serviceProviderLocationData)
            binding.nearbyServicesRecyclerView.adapter = nearestServiceAdapter
            binding.nonNearbyServicesRecyclerView.adapter = nonNearestServiceAdapter
            filterNearest()
        }

        binding.refreshImageButton.setOnClickListener {

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        clearData()
        _binding = null
    }

    private fun clearData() {
        nearestServiceAdapter = null
        nonNearestServiceAdapter = null
        binding.nearbyServicesRecyclerView.adapter = null
        binding.nonNearbyServicesRecyclerView.adapter = null
        serviceProviderData.clear()
        serviceProviderLocationData.clear()
        filteredServiceProviderData.clear()
        beginRealTimeUpdate = false
        binding.noActiveNonNearbyServiceViewRelativeLayout.isVisible = true
        MainActivity.mapFragment?.clearMap()
        initializeCallBack()
        initNearestServiceAdapter()
        initNonNearestServiceAdapter()
    }

    private fun initializeCallBack() {
        FirebaseBackend.retrieveAllData("vehicleOwner", this)
        FirebaseBackend.retrievedAllLocationData("vehicleOwnerLocation", this)
    }

    private fun initNearestServiceAdapter() {
        nearestServiceAdapter = NearestServiceAdapter(filteredServiceProviderData, requireActivity())
        binding.nearbyServicesRecyclerView.adapter = nearestServiceAdapter
    }

    private fun initNonNearestServiceAdapter() {
        nonNearestServiceAdapter = NonNearestServiceAdapter(serviceProviderData, requireActivity())
        binding.nonNearbyServicesRecyclerView.adapter = nonNearestServiceAdapter
    }

    private fun filterNearest() {
        filteredServiceProviderData.clear()
        filteredAllServiceProviderData.clear()
        for(data in serviceProviderData) {
            val currentUser = data.email
            for (location in serviceProviderLocationData) {
                if (currentUser == location.email) {
                    LocationUtils.getDistanceHaversine(
                        MainActivity.sharedPreferences.getString("latitude", "")!!.toDouble(),
                        MainActivity.sharedPreferences.getString("longitude", "")!!.toDouble(),
                        location.latitude!!.toDouble(),
                        location.longitude!!.toDouble())
                    { distance ->
                        if (distance != null) {
                            if (distance < 4000.0) {
                                if (!filteredServiceProviderData.contains(data)) {
                                    filteredServiceProviderData.add(data)
                                }
                            } else {
                                if (!filteredAllServiceProviderData.contains(data)) {
                                    filteredAllServiceProviderData.add(data)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onUserDataReceived(data: UserData) {}

    override fun onLocationDataReceived(data: LocationData) {}

    override fun onAllDataReceived(dataArray: List<UserData>) {
        if (!isAdded) {
            return
        }
        serviceProviderData = dataArray.toMutableList()
    }

    override fun onAllLocationDataReceived(dataArray: List<LocationData>) {
        if (!isAdded) {
            return
        }
        serviceProviderLocationData = dataArray.toMutableList()
        binding.noSelectedServiceViewRelativeLayout.isVisible = filteredServiceProviderData.size <= 0
        if (beginRealTimeUpdate) {
            MainActivity.mapFragment?.addOrUpdateMarkers(serviceProviderData, serviceProviderLocationData)
            filterNearest()
            initNearestServiceAdapter()
            initNonNearestServiceAdapter()
        }
    }
}