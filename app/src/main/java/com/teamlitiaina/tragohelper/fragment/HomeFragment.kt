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
import com.teamlitiaina.tragohelper.adapter.SelectServiceAdapter
import com.teamlitiaina.tragohelper.data.LocationData
import com.teamlitiaina.tragohelper.data.UserData
import com.teamlitiaina.tragohelper.databinding.FragmentHomeBinding
import com.teamlitiaina.tragohelper.firebase.FirebaseObject

class HomeFragment : Fragment(), FirebaseObject.Companion.FirebaseCallback, SelectServiceAdapter.DataReceivedListener {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var userData = mutableListOf<UserData>()
    private var locationData = mutableListOf<LocationData>()
    private lateinit var nearestServiceAdapter: SelectServiceAdapter
    private var beginRealTimeUpdate =  false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.nearbyServicesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        FirebaseObject.retrieveAllData("vehicleOwner", this)
        FirebaseObject.retrievedAllLocationData("vehicleOwnerLocation", this)

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

        binding.undeterminedView.setOnClickListener {
            beginRealTimeUpdate = true
            binding.noSelectedServiceViewRelativeLayout.isVisible = false
            MainActivity.mapFragment?.userData = userData
            MainActivity.mapFragment?.locationData = locationData
            nearestServiceAdapter = SelectServiceAdapter(userData, requireActivity())
            nearestServiceAdapter.setDataReceivedListener(this)
            binding.nearbyServicesRecyclerView.adapter = nearestServiceAdapter
            MainActivity.mapFragment?.addOrUpdateMarkers(userData, locationData)
        }

        binding.refreshImageButton.setOnClickListener {
            FirebaseObject.retrieveAllData("vehicleOwner", this)
            FirebaseObject.retrievedAllLocationData("vehicleOwnerLocation", this)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onUserDataReceived(data: UserData) {}

    override fun onLocationDataReceived(data: LocationData) {}

    override fun onAllDataReceived(dataArray: List<UserData>) {
        if (!isAdded) {
            return
        }
        userData = dataArray.toMutableList()
    }

    override fun onAllLocationDataReceived(dataArray: List<LocationData>) {
        if (!isAdded) {
            return
        }
        locationData = dataArray.toMutableList()
        if (beginRealTimeUpdate) {
            MainActivity.mapFragment?.addOrUpdateMarkers(userData, locationData)
            nearestServiceAdapter = SelectServiceAdapter(userData, requireActivity())
        }
    }

    override fun onDataReceived(distance: String, position: Int) {
        if(!isAdded) {
            return
        }
        binding.nearbyServicesRecyclerView.post {
            val holder = binding.nearbyServicesRecyclerView.findViewHolderForAdapterPosition(position) as? SelectServiceAdapter.ViewHolder
            holder?.binding?.distanceTextView?.text = distance
        }
    }
}