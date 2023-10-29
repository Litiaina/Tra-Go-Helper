package com.teamlitiaina.tragohelper.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.teamlitiaina.tragohelper.activity.MainActivity
import com.teamlitiaina.tragohelper.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (MainActivity.mapFragment == null) {
            MainActivity.mapFragment = MapFragment()
            FragmentChanger.replaceFragment(requireActivity(), MainActivity.mapFragment!!, binding.homeFragmentLayout.id)
        }

        binding.addressSearchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    MainActivity.mapFragment = MapFragment(it)
                    FragmentChanger.replaceFragment(requireActivity(), MainActivity.mapFragment!!, binding.homeFragmentLayout.id)
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}