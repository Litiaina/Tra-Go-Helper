package com.teamlitiaina.tragohelper.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import com.google.android.gms.maps.model.Polyline
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
    private var polyline: Polyline? = null
    private var destinationLatitude: Double = 0.0
    private var destinationLongitude: Double = 0.0
    private val locationPermissionRequestCode = 100

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
        requestLocationPermission()
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
            "https://maps.googleapis.com/maps/api/geocode/json?address=$address&key=${getString(R.string.MAPS_API_KEY)}"

        val geocodeRequest = JsonObjectRequest(
            Request.Method.GET, geocodeUrl, null,
            { response ->
                val results = response.getJSONArray("results")
                if (results.length() > 0) {
                    val result = results.getJSONObject(0)
                    val location = result.getJSONObject("geometry").getJSONObject("location")
                    destinationLatitude = location.getDouble("lat")
                    destinationLongitude = location.getDouble("lng")
                }
            },
            { error ->
                Log.e("Geocode API", "Error: ${error.message}")
            })

        Volley.newRequestQueue(requireContext()).add(geocodeRequest)
    }

    private fun updateDirections(origin: String, destination: String) {
        val directionsUrl =
            "https://maps.googleapis.com/maps/api/directions/json?" +
                    "origin=$origin&destination=$destination&key=${getString(R.string.MAPS_API_KEY)}"

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
                    polyline?.remove()
                    polyline = mMap?.addPolyline(PolylineOptions().addAll(points).color(Color.BLUE).width(10f))
                    mMap?.clear()
                    val destinationLatLng = LatLng(destinationLatitude, destinationLongitude)
                    mMap?.addMarker(MarkerOptions().position(destinationLatLng).title("Destination"))
                    if (points.isNotEmpty()) {
                        polyline = mMap?.addPolyline(PolylineOptions().addAll(points).color(Color.BLUE).width(10f))
                    }
                }
            },
            { error ->
                Log.e("Directions API", "Error: ${error.message}")
            })
        queue.add(directionsRequest)
    }

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
            mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))
            val origin = "${it.latitude},${it.longitude}"
            val destination = "$destinationLatitude,$destinationLongitude"
            updateDirections(origin, destination)
        }
    }
    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                // Explain why you need the permission
            } else {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    locationPermissionRequestCode
                )
            }
        }
    }
    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            locationPermissionRequestCode -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    createLocationRequest()
                    initializeLocationCallback()
                    getLastLocation()
                } else {
                    requireActivity().finish()
                }
                return
            }
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
            mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))
        }
        startLocationUpdates()
    }

}