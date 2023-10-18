package com.teamlitiaina.tragohelper.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.tasks.Task
import com.google.maps.android.PolyUtil
import com.teamlitiaina.tragohelper.R
import com.teamlitiaina.tragohelper.databinding.FragmentMapBinding

@Suppress("DEPRECATION")
class MapFragment : Fragment(), OnMapReadyCallback{

    private var mMap: GoogleMap? = null
    private var currentLocation: Location? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private lateinit var binding: FragmentMapBinding
    private lateinit var queue: RequestQueue

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
        queue = Volley.newRequestQueue(requireContext())
        createLocationRequest()
        initializeLocationCallback()
        getLastLocation()

        binding.addressSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { getDirections(it) }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Handle text changes if necessary
                return false
            }
        })
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 5000
            fastestInterval = 2000
        }
    }

    private fun getDirections(address: String) {
        val geocodeUrl =
            "https://maps.googleapis.com/maps/api/geocode/json?address=$address&key=AIzaSyBNKZo96e8T4FpfovK5yD1hWkePYE7XT0o"

        val geocodeRequest = JsonObjectRequest(
            Request.Method.GET, geocodeUrl, null,
            { response ->
                val results = response.getJSONArray("results")
                if (results.length() > 0) {
                    val result = results.getJSONObject(0)
                    val location = result.getJSONObject("geometry").getJSONObject("location")
                    val latitude = location.getDouble("lat")
                    val longitude = location.getDouble("lng")

                    val origin = "${currentLocation?.latitude},${currentLocation?.longitude}"
                    val destination = "$latitude,$longitude"

                    val directionsUrl =
                        "https://maps.googleapis.com/maps/api/directions/json?" +
                                "origin=$origin&destination=$destination&key=AIzaSyBNKZo96e8T4FpfovK5yD1hWkePYE7XT0o"

                    val queue = Volley.newRequestQueue(requireContext())
                    val directionsRequest = JsonObjectRequest(
                        Request.Method.GET, directionsUrl, null,
                        { response ->
                            val routes = response.getJSONArray("routes")
                            if (routes.length() > 0) {
                                val points = ArrayList<LatLng>()
                                val legs = routes.getJSONObject(0).getJSONArray("legs")
                                val steps = legs.getJSONObject(0).getJSONArray("steps")
                                for (i in 0 until steps.length()) {
                                    val step = steps.getJSONObject(i)
                                    val pointsString = step.getJSONObject("polyline").getString("points")
                                    points.addAll(PolyUtil.decode(pointsString))
                                }
                                val options = PolylineOptions().addAll(points).color(Color.BLUE).width(10f)
                                mMap?.clear()
                                mMap?.addPolyline(options)

                                // Add marker to the destination location
                                val destinationLatLng = LatLng(latitude, longitude)
                                mMap?.addMarker(MarkerOptions().position(destinationLatLng).title("Destination"))
                            }
                        },
                        { error ->
                            Toast.makeText(requireContext(), "$error", Toast.LENGTH_SHORT).show()
                        })
                    queue.add(directionsRequest)
                }
            },
            { error ->
                Toast.makeText(requireContext(), "$error", Toast.LENGTH_SHORT).show()
            })

        Volley.newRequestQueue(requireContext()).add(geocodeRequest)
    }


//    private fun getDirections() {
//        val latitude = 10.3385
//        val longitude = 123.9119
//        val origin = "${currentLocation?.latitude},${currentLocation?.longitude}"
//        val destination = "$latitude,$longitude" // Replace with the desired destination latitude and longitude
//
//        val url = "https://maps.googleapis.com/maps/api/directions/json?" +
//                "origin=$origin&destination=$destination" +
//                "&key=AIzaSyBNKZo96e8T4FpfovK5yD1hWkePYE7XT0o"
//
//        val queue = Volley.newRequestQueue(requireContext())
//        val request = JsonObjectRequest(
//            Request.Method.GET, url, null,
//            { response ->
//                val routes = response.getJSONArray("routes")
//                if (routes.length() > 0) {
//                    val points = ArrayList<LatLng>()
//                    val legs = routes.getJSONObject(0).getJSONArray("legs")
//                    val steps = legs.getJSONObject(0).getJSONArray("steps")
//                    for (i in 0 until steps.length()) {
//                        val step = steps.getJSONObject(i)
//                        val pointsString = step.getJSONObject("polyline").getString("points")
//                        points.addAll(PolyUtil.decode(pointsString))
//                    }
//                    val options = PolylineOptions().addAll(points).color(Color.BLUE).width(5f)
//                    mMap?.addPolyline(options)
//
//                    val destinationLatLng = LatLng(latitude, longitude)
//                    mMap?.addMarker(MarkerOptions().position(destinationLatLng).title("Destination"))
//                }
//            },
//            { error ->
//                Toast.makeText(requireContext(), "$error", Toast.LENGTH_SHORT).show()
//            })
//        queue.add(request)
//    }

    private fun initializeLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let {
                    currentLocation = it
                    updateMapLocation()
                }
            }
        }
    }

    private fun updateMapLocation() {
        currentLocation?.let {
            val latLng = LatLng(it.latitude, it.longitude)
            mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.5f))
        }
    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val task: Task<Location> = fusedLocationProviderClient.lastLocation
        task.addOnSuccessListener { location ->
            location?.let {
                currentLocation = it
                val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
                mapFragment?.getMapAsync(this)
                startLocationUpdates()
            }
        }
    }

    private fun startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        mMap?.isMyLocationEnabled = true
        currentLocation?.let {
            val latLng = LatLng(it.latitude, it.longitude)
            mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.5f))
        }
        startLocationUpdates()
    }
}