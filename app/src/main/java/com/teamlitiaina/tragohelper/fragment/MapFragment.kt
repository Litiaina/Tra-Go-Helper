package com.teamlitiaina.tragohelper.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context.SENSOR_SERVICE
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.tasks.Task
import com.google.maps.android.PolyUtil
import com.teamlitiaina.tragohelper.R
import com.teamlitiaina.tragohelper.activity.MainActivity
import com.teamlitiaina.tragohelper.datetime.DateTime.Companion.getCurrentTime
import com.teamlitiaina.tragohelper.data.LocationData
import com.teamlitiaina.tragohelper.data.UserData
import com.teamlitiaina.tragohelper.databinding.FragmentMapBinding
import com.teamlitiaina.tragohelper.firebase.FirebaseObject

@Suppress("DEPRECATION")
class MapFragment : Fragment(), OnMapReadyCallback, FirebaseObject.Companion.FirebaseCallback {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private var mMap: GoogleMap? = null
    private var currentLocation: Location? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private val queue: RequestQueue by lazy {
        Volley.newRequestQueue(requireContext().applicationContext)
    }
    private var polyline: Polyline? = null
    private var destinationMarker: Marker? = null
    private var destinationName: String? = null
    private var destinationLatitude: Double = 0.0
    private var destinationLongitude: Double = 0.0
    private val locationPermissionRequestCode = 100
    private var isFollowingCamera = false
    private lateinit var sensorManager: SensorManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestLocationPermission()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
        createLocationRequest()
        initializeLocationCallback()
        getLastLocation()
        sensorManager = requireActivity().getSystemService(SENSOR_SERVICE) as SensorManager
        sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_NORMAL)

        binding.cameraSwtich.setOnCheckedChangeListener { _, isChecked ->
            isFollowingCamera = isChecked
        }

        binding.myLocationImageButton.setOnClickListener {
            mMap?.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.Builder()
                .target(LatLng(currentLocation!!.latitude, currentLocation!!.longitude))
                .zoom(18f)
                .tilt(0f)
                .build()))
        }
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 1000
            fastestInterval = 1000
        }
    }

    fun updateData(data: String) {
        polyline = null
        destinationMarker = null
        destinationName = null
        destinationLatitude = 0.0
        destinationLongitude = 0.0
        mMap?.clear()
        if(Patterns.EMAIL_ADDRESS.matcher(data).matches()) {
            FirebaseObject.retrieveUserDataByEmailRealTime(data, this@MapFragment)
            FirebaseObject.retrieveLocationDataByEmailRealTime(data,this@MapFragment)
        } else {
            getDirections(data)
        }
    }

    private fun getDirections(address: String) {
        if (!isAdded) {
            return
        }
        queue.add(JsonObjectRequest(
            Request.Method.GET, "https://maps.googleapis.com/maps/api/geocode/json?address=$address&key=${getString(R.string.MAPS_API_KEY)}", null,
            { response ->
                val results = response.getJSONArray("results")
                if (results.length() > 0) {
                    destinationName = results.getJSONObject(0).getString("formatted_address")
                    val location = results.getJSONObject(0).getJSONObject("geometry").getJSONObject("location")
                    destinationLatitude = location.getDouble("lat")
                    destinationLongitude = location.getDouble("lng")
                }
            }, { error -> Log.e("Geocode API", "Error: ${error.message}") })
        )
    }

    private fun getLatLngFromAddress(address: String, callback: (LatLng?) -> Unit) {
        if (!isAdded) {
            callback(null)
            return
        }
        queue.add(JsonObjectRequest(
            Request.Method.GET, "https://maps.googleapis.com/maps/api/geocode/json?address=$address&key=${getString(R.string.MAPS_API_KEY)}", null,
            { response ->
                val results = response.getJSONArray("results")
                if (results.length() > 0) {
                    val location = results.getJSONObject(0).getJSONObject("geometry").getJSONObject("location")
                    callback(LatLng(location.getDouble("lat"), location.getDouble("lng")))
                } else {
                    callback(null)
                }
            }, { error ->
                Log.e("Geocode API", "Error: ${error.message}")
                callback(null)
            })
        )
    }

    private fun updateDirections(origin: String, destination: String) {
        if (!isAdded) {
            return
        }
        queue.add(JsonObjectRequest(Request.Method.GET, "https://maps.googleapis.com/maps/api/directions/json?origin=$origin&destination=$destination&key=${getString(R.string.MAPS_API_KEY)}", null,
        { response ->
            val routes = response.getJSONArray("routes")
            if (routes.length() > 0) {
                val points = ArrayList<LatLng>()
                val steps = routes.getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONArray("steps")
                for (i in 0 until steps.length()) {
                    points.addAll(PolyUtil.decode(steps.getJSONObject(i).getJSONObject("polyline").getString("points")))
                }
                //binding.distanceTextView.text = routes.getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("distance").getString("text")
                if (polyline == null && destinationMarker == null) {
                    polyline = mMap?.addPolyline(PolylineOptions().addAll(points).color(Color.parseColor("#80b3ff")).width(10f))
                    destinationMarker = mMap?.addMarker(MarkerOptions().position(LatLng(destinationLatitude, destinationLongitude)).title(destinationName))
                } else {
                    polyline?.points = points
                    destinationMarker?.position = LatLng(destinationLatitude, destinationLongitude)
                    destinationMarker?.title = destinationName
                }
            }
        }, { error -> Log.e("Directions API", "Error: ${error.message}") }))
    }

    fun getCurrentDistance(origin: String, destination: String, callback: (String?) -> Unit) {
        if (!isAdded) {
            callback(null)
            return
        }
        queue.add(JsonObjectRequest(Request.Method.GET, "https://maps.googleapis.com/maps/api/directions/json?origin=$origin&destination=$destination&key=${getString(R.string.MAPS_API_KEY)}", null,
            { response ->
                val routes = response.getJSONArray("routes")
                if (routes.length() > 0) {
                    val currentDistance = routes.getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("distance").getString("text")
                    callback(currentDistance)
                } else {
                    callback(null)
                }
            }, { error ->
                Log.e("Directions API", "Error: ${error.message}")
                callback(null)
            }))
    }


    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (currentLocation != null && mMap != null) {
                if(isFollowingCamera) {
                    mMap?.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.Builder()
                        .target(LatLng(currentLocation!!.latitude, currentLocation!!.longitude))
                        .zoom(19.5f)
                        .bearing(event.values[0])
                        .tilt(30f)
                        .build()))
                }
            }
        }
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
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
        if (currentLocation != null && isAdded && FirebaseObject.auth.uid != null) {
            MainActivity.currentUserLongitude = currentLocation!!.latitude.toString()
            MainActivity.currentUserLongitude = currentLocation!!.longitude.toString()
            updateDirections("${currentLocation!!.latitude},${currentLocation!!.longitude}","${destinationLatitude},${destinationLongitude}")
            getCurrentDistance("${currentLocation!!.latitude},${currentLocation!!.longitude}","${destinationLatitude},${destinationLongitude}") {distance ->
                if(distance != null) {
                    binding.distanceTextView.text = distance
                }
            }
            updateMapStyle()
            FirebaseObject.database.getReference("vehicleOwnerLocation").child(FirebaseObject.auth.currentUser?.uid.toString()).setValue(
                LocationData(FirebaseObject.auth.currentUser?.uid.toString(),MainActivity.currentUser?.email.toString(),currentLocation!!.latitude.toString(), currentLocation!!.longitude.toString())
            ).addOnFailureListener {
                Log.e("Update Location","${MainActivity.currentUser?.email.toString()}: Location update failed")
            }

        } else {
            Log.e("Update Location", "Current location is null or fragment not attached.")
        }
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Explain why you need the permission
            } else {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionRequestCode)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
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
        checkSelfPermission()
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
        checkSelfPermission()
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun updateMapStyle() {
        mMap?.let { googleMap ->
            if (getCurrentTime() in 6..17) {
                googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                        requireContext(),
                        R.raw.retro_theme_map_style
                    )
                )
            } else {
                googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                        requireContext(),
                        R.raw.night_theme_map_style
                    )
                )
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        checkSelfPermission()
        mMap?.isMyLocationEnabled = true
        mMap?.uiSettings?.isMyLocationButtonEnabled = false
        mMap?.uiSettings?.isCompassEnabled = false
        currentLocation?.let {
            mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 18f))
        }
        addMarkers()
        startLocationUpdates()
        updateMapStyle()
    }

    private fun checkSelfPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
    }

    @SuppressLint("PotentialBehaviorOverride")
    private fun addMarkers() {
        val addresses = listOf("")
        val markers = mutableListOf<Marker>()

        for (address in addresses) {
            getLatLngFromAddress(address) { latLng ->
                latLng?.let {
                    val marker = mMap?.addMarker(MarkerOptions().position(it).title(address))
                    marker?.let { m ->
                        markers.add(m)
                    }
                }
            }
        }

        mMap?.setOnMarkerClickListener { clickedMarker ->
            markers.forEach { marker ->
                if (clickedMarker == marker) {
                    Toast.makeText(requireContext(), "Clicked on ${marker.title}", Toast.LENGTH_SHORT).show()
                    return@setOnMarkerClickListener true
                }
            }
            false
        }
    }

    override fun onAllDataReceived(dataArray: List<UserData>) {}

    override fun onUserDataReceived(data: UserData) {
        if(currentLocation != null && isAdded && FirebaseObject.auth.uid != null) {
            destinationName = null
            destinationName = data.name
        } else {
            Log.e("Update Location", "Current location is null or fragment not attached or auth is null.")
        }
    }

    override fun onLocationDataReceived(data: LocationData) {
        if (currentLocation != null && isAdded && FirebaseObject.auth.uid != null) {
            MainActivity.currentUserLongitude = currentLocation!!.latitude.toString()
            MainActivity.currentUserLongitude = currentLocation!!.longitude.toString()
            destinationLatitude = data.latitude.toString().toDouble()
            destinationLongitude = data.longitude.toString().toDouble()
            updateDirections("${currentLocation!!.latitude},${currentLocation!!.longitude}","${destinationLatitude},${destinationLongitude}")
            getCurrentDistance("${currentLocation!!.latitude},${currentLocation!!.longitude}","${destinationLatitude},${destinationLongitude}") { distance ->
                if(distance != null) {
                    binding.distanceTextView.text = distance
                }
            }
        } else {
            Log.e("Update Location", "Current location is null or fragment not attached or auth is null.")
        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(sensorListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        queue.cancelAll(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sensorManager.unregisterListener(sensorListener)
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        _binding = null
    }
}