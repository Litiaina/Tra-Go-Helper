package com.teamlitiaina.tragohelper.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.teamlitiaina.tragohelper.R
import com.teamlitiaina.tragohelper.activity.MainActivity
import com.teamlitiaina.tragohelper.adapter.NearestServiceAdapter
import com.teamlitiaina.tragohelper.adapter.NonNearestServiceAdapter
import com.teamlitiaina.tragohelper.constants.Constants
import com.teamlitiaina.tragohelper.data.LocationData
import com.teamlitiaina.tragohelper.data.ServiceProviderData
import com.teamlitiaina.tragohelper.data.UserData
import com.teamlitiaina.tragohelper.databinding.FragmentHomeBinding
import com.teamlitiaina.tragohelper.firebase.FirebaseBackend
import com.teamlitiaina.tragohelper.utility.LocationUtils

class HomeFragment : Fragment(), FirebaseBackend.Companion.FirebaseCallback {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var nearestServiceAdapter: NearestServiceAdapter? = null
    private var nonNearestServiceAdapter: NonNearestServiceAdapter? = null
    private var serviceType: String? = null
    private var currentSelectServiceType: String? = null
    private var serviceProviderData = mutableListOf<ServiceProviderData>()
    private var filteredServiceProviderData = mutableListOf<ServiceProviderData>()
    private var filteredServiceProviderLocation = mutableListOf<LocationData>()
    private var filteredAllServiceProviderData = mutableListOf<ServiceProviderData>()
    private var filteredAllServiceProviderLocation = mutableListOf<LocationData>()
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
            onServiceSelectView(R.color.black,R.color.black,R.color.black,R.color.black,R.color.black)
            clearData()
            currentSelectServiceType = null
        }

        binding.undeterminedView.setOnClickListener {
            onServiceSelectView(R.color.brown,R.color.black,R.color.black,R.color.black,R.color.black)
            beginServices("undetermined")
            currentSelectServiceType = "undetermined"
        }

        binding.fuelView.setOnClickListener {
            onServiceSelectView(R.color.black,R.color.brown,R.color.black,R.color.black,R.color.black)
            beginServices("fuel")
            currentSelectServiceType = "fuel"
        }

        binding.tiresView.setOnClickListener {
            onServiceSelectView(R.color.black,R.color.black,R.color.brown,R.color.black,R.color.black)
            beginServices("tires")
            currentSelectServiceType = "tires"
        }

        binding.batteryView.setOnClickListener {
            onServiceSelectView(R.color.black,R.color.black,R.color.black,R.color.brown,R.color.black)
            beginServices("battery")
            currentSelectServiceType = "battery"
        }

        binding.brakeView.setOnClickListener {
            onServiceSelectView(R.color.black,R.color.black,R.color.black,R.color.black,R.color.brown)
            beginServices("break")
            currentSelectServiceType = "break"
        }

        binding.refreshImageButton.setOnClickListener {
            currentSelectServiceType?.let {
                beginServices(it)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        clearData()
        _binding = null
    }

    private fun onServiceSelectView(undetermined: Int, fuel: Int, tires: Int, batter: Int, brake: Int) {
        binding.undeterminedView.setBackgroundColor(ContextCompat.getColor(requireContext(), undetermined))
        binding.fuelView.setBackgroundColor(ContextCompat.getColor(requireContext(), fuel))
        binding.tiresView.setBackgroundColor(ContextCompat.getColor(requireContext(), tires))
        binding.batteryView.setBackgroundColor(ContextCompat.getColor(requireContext(), batter))
        binding.brakeView.setBackgroundColor(ContextCompat.getColor(requireContext(), brake))
    }

    private fun beginServices(serviceTypeSelected: String) {
        clearData()
        serviceType = serviceTypeSelected.lowercase()
        beginRealTimeUpdate = true
        binding.noActiveNonNearbyServiceViewRelativeLayout.isVisible = false
        binding.noSelectedServiceViewRelativeLayout.isVisible = false
        initializeCallBack()
        initNearestServiceAdapter()
        initNonNearestServiceAdapter()
        serviceType?.let {
            filterNearestAndServiceType(it)
        }
        MainActivity.mapFragment?.userData = filteredAllServiceProviderData
        MainActivity.mapFragment?.locationData = filteredAllServiceProviderLocation
        MainActivity.mapFragment?.addOrUpdateMarkers(filteredAllServiceProviderData, filteredAllServiceProviderLocation)
        binding.nearbyServicesRecyclerView.adapter = nearestServiceAdapter
        binding.nonNearbyServicesRecyclerView.adapter = nonNearestServiceAdapter
    }

    private fun clearData() {
        nearestServiceAdapter = null
        nonNearestServiceAdapter = null
        serviceType = null
        binding.nearbyServicesRecyclerView.adapter = null
        binding.nonNearbyServicesRecyclerView.adapter = null
        serviceProviderData.clear()
        serviceProviderLocationData.clear()
        filteredServiceProviderData.clear()
        filteredServiceProviderLocation.clear()
        filteredAllServiceProviderLocation.clear()
        beginRealTimeUpdate = false
        binding.noActiveNonNearbyServiceViewRelativeLayout.isVisible = true
        binding.noSelectedServiceViewRelativeLayout.isVisible = true
        MainActivity.mapFragment?.clearMap()
        initializeCallBack()
        initNearestServiceAdapter()
        initNonNearestServiceAdapter()
    }

    private fun initializeCallBack() {
        FirebaseBackend.retrieveAllServiceProviderData(Constants.SERVICE_PROVIDER_PATH, this)
        FirebaseBackend.retrievedAllLocationData(Constants.SERVICE_PROVIDER_LOCATION_PATH, this)
    }

    private fun initNearestServiceAdapter() {
        nearestServiceAdapter = NearestServiceAdapter(filteredServiceProviderData, requireActivity())
        binding.nearbyServicesRecyclerView.adapter = nearestServiceAdapter
    }

    private fun initNonNearestServiceAdapter() {
        nonNearestServiceAdapter = NonNearestServiceAdapter(filteredAllServiceProviderData, requireActivity())
        binding.nonNearbyServicesRecyclerView.adapter = nonNearestServiceAdapter
    }

    private fun filterNearestAndServiceType(type: String) {
        if (filteredAllServiceProviderData.size != 0) {
            Toast.makeText(requireContext(), "${filteredAllServiceProviderData.size} $type services found", Toast.LENGTH_SHORT).show()
        }
        filteredServiceProviderData.clear()
        filteredAllServiceProviderData.clear()
        filteredServiceProviderLocation.clear()
        filteredAllServiceProviderLocation.clear()
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
                        if (data.type == type) {
                            if (distance != null && distance < 4000.0) {
                                if (!filteredServiceProviderData.contains(data) || !filteredServiceProviderLocation.contains(location)) {
                                    filteredServiceProviderData.add(data)
                                    filteredServiceProviderLocation.add(location)
                                }
                            }
                            if (!filteredAllServiceProviderData.contains(data) || !filteredAllServiceProviderLocation.contains(location)) {
                                filteredAllServiceProviderData.add(data)
                                filteredAllServiceProviderLocation.add(location)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onUserDataReceived(data: UserData) {}
    override fun onAllUserDataReceived(dataArray: List<UserData>) {}
    override fun onServiceProviderDataReceived(data: ServiceProviderData) {}
    override fun onLocationDataReceived(data: LocationData) {}
    override fun onAllServiceProviderDataReceived(dataArray: List<ServiceProviderData>) {
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
        binding.noActiveNonNearbyServiceViewRelativeLayout.isVisible = filteredAllServiceProviderData.size <= 0
        if (beginRealTimeUpdate) {
            MainActivity.mapFragment?.addOrUpdateMarkers(filteredAllServiceProviderData, filteredAllServiceProviderLocation)
            serviceType?.let {
                filterNearestAndServiceType(it)
            }
            initNearestServiceAdapter()
            initNonNearestServiceAdapter()
        }
    }
}