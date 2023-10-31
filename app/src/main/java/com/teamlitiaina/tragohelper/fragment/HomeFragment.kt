package com.teamlitiaina.tragohelper.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.sevicesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        FirebaseObject.retrieveAllData("vehicleOwner", this)

        if (MainActivity.mapFragment == null) {
            MainActivity.mapFragment = MapFragment()
            FragmentChanger.replaceFragment(requireActivity(), MainActivity.mapFragment!!, binding.homeFragmentLayout.id)
        }

        binding.addressSearchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    if (MainActivity.currentUser?.email == it) {
                        Toast.makeText(activity, "Current User", Toast.LENGTH_SHORT).show()
                    } else {
                        MainActivity.mapFragment?.updateData(it)
                    }
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
        FirebaseObject.retrieveAllData("vehicleOwner", this)
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
        val adapter = SelectServiceAdapter(dataArray, requireActivity())
        adapter.setDataReceivedListener(this)
        binding.sevicesRecyclerView.adapter = adapter
        binding.refreshImageButton.setOnClickListener {
            binding.sevicesRecyclerView.adapter = adapter
        }
    }

    override fun onDataReceived(distance: String, position: Int) {
        if(!isAdded) {
            return
        }
        binding.sevicesRecyclerView.post {
            val holder = binding.sevicesRecyclerView.findViewHolderForAdapterPosition(position) as? SelectServiceAdapter.ViewHolder
            holder?.binding?.distanceTextView?.text = distance
        }
    }
}