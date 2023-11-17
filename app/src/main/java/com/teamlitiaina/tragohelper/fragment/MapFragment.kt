package com.teamlitiaina.tragohelper.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context.SENSOR_SERVICE
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
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
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
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
import com.teamlitiaina.tragohelper.adapter.CustomInfoWindowAdapter
import com.teamlitiaina.tragohelper.constants.LocationUpdateConstants.Companion.FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
import com.teamlitiaina.tragohelper.constants.LocationUpdateConstants.Companion.MIN_UPDATE_DISTANCE_IN_METERS
import com.teamlitiaina.tragohelper.constants.LocationUpdateConstants.Companion.UPDATE_INTERVAL_IN_MILLISECONDS
import com.teamlitiaina.tragohelper.constants.LocationUpdateConstants.Companion.WILL_WAIT_FOR_ACCURATE_LOCATION
import com.teamlitiaina.tragohelper.constants.PermissionCodes.Companion.REQUEST_CHECK_SETTINGS
import com.teamlitiaina.tragohelper.datetime.DateTime.Companion.getCurrentTime
import com.teamlitiaina.tragohelper.data.LocationData
import com.teamlitiaina.tragohelper.data.UserData
import com.teamlitiaina.tragohelper.databinding.FragmentMapBinding
import com.teamlitiaina.tragohelper.dialog.LoadingDialog
import com.teamlitiaina.tragohelper.firebase.FirebaseObject
import com.teamlitiaina.tragohelper.utility.LocationUtils.Companion.calculateBearing
import com.teamlitiaina.tragohelper.utility.LocationUtils.Companion.formatDistance
import com.teamlitiaina.tragohelper.validation.Validation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.util.GeoPoint
import kotlin.math.*

class MapFragment : Fragment(), OnMapReadyCallback, SensorEventListener, FirebaseObject.Companion.FirebaseCallback {

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
    private var directionsJob: Job? = null
    private var polyline: Polyline? = null
    private var destinationMarker: Marker? = null
    private var destinationName: String? = null
    private var destinationLatitude: Double = 0.0
    private var destinationLongitude: Double = 0.0
    private lateinit var sensorManager: SensorManager
    private var loadingDialog: LoadingDialog? = null
    private var cancelDirectionsUpdate = false
    private var isFollowingCamera = false
    private var followDirectionCamera = false
    private val emailMarkerMap = mutableMapOf<String, Marker>()
    var userData = mutableListOf<UserData>()
    var locationData = mutableListOf<LocationData>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = LoadingDialog()
        loadingDialog?.show(parentFragmentManager, "Loading")
        FirebaseObject.retrieveAllData("vehicleOwner", this)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
        createLocationRequest()
        checkLocationSettings()
        initializeLocationCallback()
        getLastLocation()
        sensorManager = requireActivity().getSystemService(SENSOR_SERVICE) as SensorManager
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_NORMAL)
        binding.followDirectionCameraCardView.isVisible = false

        binding.cameraSwitch.setOnCheckedChangeListener { _, isChecked ->
            isFollowingCamera = isChecked
        }

        binding.followDirectionCameraSwitch.setOnCheckedChangeListener { _, isChecked ->
            followDirectionCamera = isChecked
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
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL_IN_MILLISECONDS).apply {
            setMinUpdateIntervalMillis(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS)
            setMinUpdateDistanceMeters(MIN_UPDATE_DISTANCE_IN_METERS)
            setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            setWaitForAccurateLocation(WILL_WAIT_FOR_ACCURATE_LOCATION)
        }.build()
    }

    private fun checkLocationSettings() {
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(requireContext())
        client.checkLocationSettings(builder.build()).addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    exception.startResolutionForResult(requireActivity(), REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.e("Enable Location", "Error: ${sendEx.message}")
                }
            }
        }
    }

    fun setDestinationRoute(email: String) {
        cancelDirections()
        binding.cameraSwitch.isChecked = false
        binding.cameraCardView.isVisible = false
        binding.followDirectionCameraCardView.isVisible = true
        cancelDirectionsUpdate = false
        if(Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            FirebaseObject.retrieveUserDataByEmailRealTime(email, this@MapFragment)
            FirebaseObject.retrieveLocationDataByEmailRealTime(email,this@MapFragment)
        } else {
            getDirections(email)
        }
    }

    private fun clearDirectionsJobs() {
        if (directionsJob?.isActive == true) {
            Log.d("Directions API", "Previous job is still active. Cancelling...")
            directionsJob!!.cancel()
        }
    }

    fun cancelDirections() {
        cancelDirectionsUpdate = true
        polyline?.remove()
        destinationMarker?.remove()
        polyline = null
        destinationMarker = null
        destinationName = null
        destinationLatitude = 0.0
        destinationLongitude = 0.0
        binding.distanceTextView.text = "0 m"
        clearDirectionsJobs()
    }

    fun clearMap() {
        binding.followDirectionCameraCardView.isVisible = false
        binding.cameraCardView.isVisible = true
        emailMarkerMap.values.forEach { it.remove() }
        emailMarkerMap.clear()
        userData.clear()
        locationData.clear()
        cancelDirections()
        mMap?.clear()
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
                    if (destinationMarker == null) {
                        destinationMarker = mMap?.addMarker(MarkerOptions().position(LatLng(destinationLatitude, destinationLongitude)).title(destinationName))
                    } else {
                        destinationMarker?.position = LatLng(destinationLatitude, destinationLongitude)
                        destinationMarker?.title = address
                    }
                }
            }, { error -> Log.e("Geocode API", "Error: ${error.message}") })
        )
    }
    @Deprecated("This is a directions api from google, usage is not free of charge. Use getDirectionOriginToDestination() instead.")
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

    private fun getDirectionOriginToDestination(originLatitude: Double, originLongitude: Double, destinationLatitude: Double, destinationLongitude: Double) {
        if (!isAdded) {
            return
        }
        if (!Validation.isInternetAvailable(requireContext())) {
            Log.e("Network", "No internet connection")
            cancelDirections()
            return
        }
        try {
            directionsJob = lifecycleScope.launch(Dispatchers.Main) {
                val roadManager: RoadManager = OSRMRoadManager(requireContext(), "TraGoHelper")
                val waypoints = ArrayList<GeoPoint>()
                waypoints.add(GeoPoint(originLatitude, originLongitude))
                val endPoint = GeoPoint(destinationLatitude, destinationLongitude)
                waypoints.add(endPoint)

                val road = withContext(Dispatchers.IO) {
                    roadManager.getRoad(waypoints)
                }

                val roadOverlay = RoadManager.buildRoadOverlay(road)

                val newPoints = roadOverlay.actualPoints.map { LatLng(it.latitude, it.longitude) }

                if (polyline == null) {
                    polyline = mMap?.addPolyline(
                        PolylineOptions()
                            .addAll(newPoints)
                            .color(Color.parseColor("#80b3ff"))
                            .width((7f * resources.displayMetrics.density))
                            .geodesic(true)
                    )
                } else {
                    if (!arePolyLinesEqual(newPoints, polyline?.points)) {
                        polyline?.points = newPoints
                    }
                    destinationMarker?.position = LatLng(destinationLatitude, destinationLongitude)
                }

                if (followDirectionCamera) {
                    val firstSegmentBearing = calculateBearing(
                        LatLng(roadOverlay.actualPoints[0].latitude, roadOverlay.actualPoints[0].longitude),
                        LatLng(roadOverlay.actualPoints[1].latitude, roadOverlay.actualPoints[1].longitude)
                    )
                    mMap?.animateCamera(
                        CameraUpdateFactory.newCameraPosition(
                            CameraPosition.Builder()
                                .target(LatLng(roadOverlay.actualPoints[0].latitude, roadOverlay.actualPoints[0].longitude))
                                .zoom(18.5f)
                                .bearing(firstSegmentBearing)
                                .tilt(30f)
                                .build()
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("Directions API", "Error: ${e.message}", e)
        }
    }

    private fun arePolyLinesEqual(polyline1: List<LatLng>?, polyline2: List<LatLng>?): Boolean {
        val polyline1Pairs = polyline1?.map { it.latitude to it.longitude }
        val polyline2Pairs = polyline2?.map { it.latitude to it.longitude }

        return polyline1Pairs?.toTypedArray()?.contentDeepEquals(polyline2Pairs?.toTypedArray() ?: emptyArray()) == true
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

    @Deprecated("This function is deprecated. Use getDistance() instead.")
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

    // Use only when trying to get accurate distance in directions
     fun getDistance(originLatitude: Double, originLongitude: Double, destinationLatitude: Double, destinationLongitude: Double, callback: (String?) -> Unit) {
        if (isAdded) {
            try {
                directionsJob = lifecycleScope.launch(Dispatchers.Main) {
                    val roadManager: RoadManager = OSRMRoadManager(requireContext(), "TraGoHelper")
                    val waypoints = ArrayList<GeoPoint>()
                    waypoints.add(GeoPoint(originLatitude, originLongitude))
                    val endPoint = GeoPoint(destinationLatitude, destinationLongitude)
                    waypoints.add(endPoint)
                    val road = withContext(Dispatchers.IO) {
                        roadManager.getRoad(waypoints)
                    }
                    callback(formatDistance(road.mLegs.sumOf { it.mLength }))
                }
            } catch (e: Exception) {
                Log.e("Directions API", "Error: ${e.message}", e)
                callback(null)
            }
        }
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
        if (currentLocation != null && isAdded && FirebaseObject.auth.uid != null && MainActivity.currentUser != null) {
            binding.loadingLocationLinearLayout.isVisible = false
            with(MainActivity.sharedPreferences.edit()) {
                putString("latitude", currentLocation!!.latitude.toString())
                putString("longitude", currentLocation!!.longitude.toString())
                apply()
            }
            updateMapDirections()
            updateMapStyle()
            addOrUpdateMarkers(userData, locationData)
            FirebaseObject.database.getReference("vehicleOwnerLocation").child(FirebaseObject.auth.currentUser?.uid.toString()).setValue(
                LocationData(FirebaseObject.auth.currentUser?.uid.toString(),MainActivity.currentUser?.email.toString(),currentLocation!!.latitude.toString(), currentLocation!!.longitude.toString())
            ).addOnFailureListener {
                Log.e("Update Location","${MainActivity.currentUser?.email.toString()}: Location update failed")
            }
        } else {
            Log.e("Update Location", "Current location is null or fragment not attached.")
        }
    }
    private fun getLastLocation() {
        checkLocationPermission()
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
        checkLocationPermission()
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    private fun checkLocationPermission(): Boolean {
        return (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
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

    private fun updateMapDirections() {
        if (!cancelDirectionsUpdate) {
            if(destinationLatitude != 0.0 && destinationLongitude != 0.0) {
                getDirectionOriginToDestination(currentLocation!!.latitude, currentLocation!!.longitude, destinationLatitude, destinationLongitude)
            }
//            --- Usage will increase cpu but will get accurate distance
            getDistance(currentLocation!!.latitude, currentLocation!!.longitude, destinationLatitude,destinationLongitude) { distance ->
                if(distance != null) {
                    binding.distanceTextView.text = distance
                }
            }
//            ---
//            getDistanceHaversine(currentLocation!!.latitude, currentLocation!!.longitude, destinationLatitude,destinationLongitude) { distance ->
//                if (distance != null) {
//                    if (destinationLatitude != 0.0 && destinationLongitude != 0.0) {
//                        binding.distanceTextView.text = formatDistance(distance)
//                    }
//                }
//            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        checkLocationPermission()
        mMap?.isMyLocationEnabled = true
        mMap?.uiSettings?.isMyLocationButtonEnabled = false
        mMap?.uiSettings?.isCompassEnabled = false
        currentLocation?.let {
            mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 18f))
        }
        startLocationUpdates()
        updateMapStyle()
        loadingDialog?.dismiss()
    }

    @SuppressLint("PotentialBehaviorOverride")
     fun addOrUpdateMarkers(userData: List<UserData>, locationData: List<LocationData>) {
        if (userData.isEmpty() || locationData.isEmpty()) {
            return
        }
        if (isAdded) {
            for (index in 0 until min(userData.size, locationData.size)) {
                val name = userData[index].name.toString()
                val existingMarker = emailMarkerMap[locationData[index].email]

                if (existingMarker != null) {
                    existingMarker.position = LatLng(locationData[index].latitude!!.toDouble(), locationData[index].longitude!!.toDouble())
                } else {
                    val newMarker = mMap?.addMarker(MarkerOptions().position(LatLng(locationData[index].latitude!!.toDouble(), locationData[index].longitude!!.toDouble())).title(name).icon(
                        BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(resources, org.osmdroid.library.R.drawable.person))))
                    newMarker?.let { marker ->
                        emailMarkerMap[locationData[index].email.toString()] = marker
                    }
                }
            }
            mMap?.setInfoWindowAdapter(CustomInfoWindowAdapter(requireContext()))
            mMap?.setOnInfoWindowClickListener { clickedMarker ->
                emailMarkerMap.forEach { (email, marker) ->
                    if (clickedMarker == marker) {
                        setDestinationRoute(email)
                    }
                }
            }
            mMap?.setOnMarkerClickListener { clickedMarker ->
                emailMarkerMap.forEach { (_, marker) ->
                    if (clickedMarker == marker) {
                        marker.showInfoWindow()
                        return@setOnMarkerClickListener true
                    }
                }
                false
            }
            binding.loadingMarkersLinearLayout.isVisible = false
        }
    }

    override fun onAllDataReceived(dataArray: List<UserData>) {
    }

    override fun onAllLocationDataReceived(dataArray: List<LocationData>) {}

    override fun onUserDataReceived(data: UserData) {
        if(currentLocation != null && isAdded && FirebaseObject.auth.uid != null) {
            destinationName = null
            destinationName = data.name
        } else {
            Log.e("Update Location", "Current location is null or fragment not attached or auth is null.")
        }
    }

    override fun onLocationDataReceived(data: LocationData) {
        if (currentLocation != null && isAdded && FirebaseObject.auth.uid != null && MainActivity.currentUser != null) {
            with(MainActivity.sharedPreferences.edit()) {
                putString("latitude", currentLocation!!.latitude.toString())
                putString("longitude", currentLocation!!.longitude.toString())
                apply()
            }
            destinationLatitude = data.latitude.toString().toDouble()
            destinationLongitude = data.longitude.toString().toDouble()
            updateMapDirections()
        } else {
            Log.e("Update Location", "Current location is null or fragment not attached or auth is null.")
        }
    }

    override fun onSensorChanged(p0: SensorEvent?) {
        if (currentLocation != null && mMap != null) {
            if (isFollowingCamera) {
                val rotationMatrix = FloatArray(9)
                SensorManager.getRotationMatrixFromVector(rotationMatrix, p0?.values)

                val orientationValues = FloatArray(3)
                SensorManager.getOrientation(rotationMatrix, orientationValues)

                mMap?.animateCamera(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.Builder()
                            .target(LatLng(currentLocation!!.latitude, currentLocation!!.longitude))
                            .zoom(19.5f)
                            .bearing(Math.toDegrees(orientationValues[0].toDouble()).toFloat())
                            .tilt(30f)
                            .build()
                    )
                )
            }
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}

    override fun onResume() {
        super.onResume()
        registerListeners()
        startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        unregisterListeners()
        stopLocationUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        cleanUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cleanUp()
        _binding = null
    }

    private fun registerListeners() {
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_NORMAL)
    }

    private fun unregisterListeners() {
        sensorManager.unregisterListener(this)
    }

    private fun cleanUp() {
        queue.cancelAll(this)
        clearDirectionsJobs()
    }
}