package com.teamlitiaina.tragohelper.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.teamlitiaina.tragohelper.databinding.CustomInfoWindowServiceProviderBinding

class CustomInfoWindowAdapter(private val context: Context) : GoogleMap.InfoWindowAdapter {

    private lateinit var binding: CustomInfoWindowServiceProviderBinding

    override fun getInfoContents(p0: Marker): View? {
        return null
    }

    override fun getInfoWindow(p0: Marker): View {
        binding = CustomInfoWindowServiceProviderBinding.inflate(LayoutInflater.from(context), null, false)
        binding.serviceProviderNameTextView.text = p0.title
        return binding.root
    }
}