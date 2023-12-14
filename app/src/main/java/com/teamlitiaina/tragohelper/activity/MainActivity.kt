package com.teamlitiaina.tragohelper.activity

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.firebase.messaging.FirebaseMessaging
import com.teamlitiaina.tragohelper.R
import com.teamlitiaina.tragohelper.constants.PermissionCodes.Companion.LOCATION_PERMISSION_REQUEST_CODE
import com.teamlitiaina.tragohelper.constants.PermissionCodes.Companion.SETTINGS_REQUEST_CODE
import com.teamlitiaina.tragohelper.data.LocationData
import com.teamlitiaina.tragohelper.data.NotificationData
import com.teamlitiaina.tragohelper.data.UserData
import com.teamlitiaina.tragohelper.databinding.ActivityMainBinding
import com.teamlitiaina.tragohelper.firebase.FirebaseBackend
import com.teamlitiaina.tragohelper.firebase.FirebaseMessagingServiceBackend
import com.teamlitiaina.tragohelper.firebase.NotificationFirebaseBackend
import com.teamlitiaina.tragohelper.fragment.FragmentChanger
import com.teamlitiaina.tragohelper.fragment.HomeFragment
import com.teamlitiaina.tragohelper.fragment.MapFragment
import com.teamlitiaina.tragohelper.fragment.NotificationFragment
import com.teamlitiaina.tragohelper.fragment.ProfileFragment
import com.teamlitiaina.tragohelper.fragment.RequestFragment

class MainActivity : AppCompatActivity(), FirebaseBackend.Companion.FirebaseCallback, NotificationFirebaseBackend.Companion.NotificationFirebaseCallback {

    private lateinit var binding: ActivityMainBinding
    private lateinit var homeFragment: HomeFragment
    private lateinit var requestFragment: RequestFragment
    private lateinit var profileFragment: ProfileFragment
    private lateinit var notificationFragment: NotificationFragment
    private var notificationsList = mutableListOf<NotificationData>()

    companion object {
        var currentUser: UserData? = null
        var mapFragment: MapFragment? = null
        lateinit var sharedPreferences: SharedPreferences
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        )
        setContentView(binding.root)

        binding.notificationCountCardView.isVisible = false

        FirebaseBackend.retrieveData("vehicleOwner", this)
        sharedPreferences = getSharedPreferences("currentUserLocation", Context.MODE_PRIVATE)

        if (sharedPreferences.getString("auth", "") == "") {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        mapFragment = MapFragment()
        homeFragment = HomeFragment()
        requestFragment = RequestFragment()
        profileFragment = ProfileFragment()
        notificationFragment = NotificationFragment()
        requestPermissions()
        if (!areNotificationsEnabled()) {
            showNotificationSettingsDialog()
        }

        FragmentChanger.replaceFragment(this@MainActivity, homeFragment, binding.dashboardLayout.id)

        binding.navigationBarBottomNavigationView.setOnItemSelectedListener { item ->
            val currentFragment = supportFragmentManager.findFragmentById(binding.dashboardLayout.id)
            when (item.itemId) {
                R.id.navigation_home -> {
                    if(currentFragment !is HomeFragment) {
                        FragmentChanger.replaceFragment(this@MainActivity, homeFragment, binding.dashboardLayout.id)
                        binding.topConstraintLayout.isVisible = true
                        binding.viewMapFrameLayout.isVisible = true
                        binding.topSpacerView.isVisible = true
                        changeLayoutHeight(R.dimen.home_map_dimens, binding.bottomSpacerView)
                    }
                    true
                }
                R.id.navigation_current_request -> {
                    if(currentFragment !is RequestFragment) {
                        FragmentChanger.replaceFragment(this@MainActivity, requestFragment, binding.dashboardLayout.id)
                        binding.topConstraintLayout.isVisible = false
                        binding.viewMapFrameLayout.isVisible = true
                        binding.topSpacerView.isVisible = false
                        changeLayoutHeight(R.dimen.current_request_map_dimens, binding.bottomSpacerView)
                    }
                    true
                }
                R.id.navigation_notifications -> {
                    if(currentFragment !is NotificationFragment) {
                        FragmentChanger.replaceFragment(this@MainActivity, notificationFragment, binding.dashboardLayout.id)
                        binding.topConstraintLayout.isVisible = false
                        binding.viewMapFrameLayout.isVisible = false
                        binding.topSpacerView.isVisible = false
                    }
                    true
                }
                R.id.navigation_profile -> {
                    if(currentFragment !is ProfileFragment) {
                        FragmentChanger.replaceFragment(this@MainActivity, profileFragment, binding.dashboardLayout.id)
                        binding.topConstraintLayout.isVisible = false
                        binding.viewMapFrameLayout.isVisible = false
                        binding.topSpacerView.isVisible = false
                    }
                    true
                }
                else -> {
                    false
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mapFragment = null
    }

    override fun onAllDataReceived(dataArray: List<UserData>) {}

    override fun onAllLocationDataReceived(dataArray: List<LocationData>) {}

    override fun onUserDataReceived(data: UserData) {
        binding.currentUserNameTextView.text = data.name
        currentUser = data
        FirebaseBackend.retrieveLocationDataByEmailRealTime(currentUser?.email.toString(), this)
        NotificationFirebaseBackend.retrieveNotificationByEmail(currentUser?.email.toString(), this)

        if (FirebaseBackend.auth.currentUser?.uid != null) {
            FirebaseMessaging.getInstance().token
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val token = task.result
                        Log.d("MyFirebaseMessaging", "Manually retrieved token: $token")
                        FirebaseMessagingServiceBackend.updateTokenInFirebase(token)
                    } else {
                        Log.e("MyFirebaseMessaging", "Failed to retrieve token: ${task.exception}")
                    }
                }
        }

    }

    override fun onLocationDataReceived(data: LocationData) {
        with(sharedPreferences.edit()) {
            putString("latitude", data.latitude.toString())
            putString("longitude", data.longitude.toString())
            apply()
        }
    }

    private fun changeLayoutHeight(dimen: Int, view: View) {
        val newHeight = resources.getDimensionPixelOffset(dimen)
        val params = view.layoutParams
        params.height = newHeight
        view.layoutParams = params
    }

    private fun showPermissionRationaleDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Permission needed")
        builder.setMessage("This app requires location permission to function properly. You can grant the permission in the app settings.")
        builder.setPositiveButton("Go to settings") { _, _ ->
            openAppSettings()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
            finish()
        }
        builder.show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivityIfNeeded(intent, SETTINGS_REQUEST_CODE)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SETTINGS_REQUEST_CODE) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PERMISSION_GRANTED) {
                mapFragment?.let {
                    FragmentChanger.replaceFragment(this@MainActivity, it, binding.viewMapFrameLayout.id)
                }
            } else {
                finish()
            }
        }
    }

    private fun requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                showPermissionRationaleDialog()
            } else {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        } else {
            mapFragment?.let {
                FragmentChanger.replaceFragment(this@MainActivity, it, binding.viewMapFrameLayout.id)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) {
                    mapFragment?.let {
                        FragmentChanger.replaceFragment(this@MainActivity, it, binding.viewMapFrameLayout.id)
                    }
                } else {
                    showPermissionRationaleDialog()
                }
            }
        }
    }

    override fun onNotificationReceived(notificationData: List<NotificationData>) {
        notificationsList.clear()
        for (data in notificationData) {
            if (!data.isSeen) {
                notificationsList.add(data)
            }
        }
        binding.notificationCountCardView.isVisible = notificationsList.size != 0
        binding.notificationCountTextView.text = notificationsList.size.toString()
    }

    private fun areNotificationsEnabled(): Boolean {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "notification_channel"
        val channel = notificationManager.getNotificationChannel(channelId)

        return NotificationManagerCompat.from(this).areNotificationsEnabled() && (channel == null || channel.importance != NotificationManager.IMPORTANCE_NONE)
    }

    private fun showNotificationSettingsDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Notification Settings")
            .setMessage("Notifications are disabled. Do you want to enable them?")
            .setPositiveButton("Yes") { dialog, _ ->
                openNotificationSettings()
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun openNotificationSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

}