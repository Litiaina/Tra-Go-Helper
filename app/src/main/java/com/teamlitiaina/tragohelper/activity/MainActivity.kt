package com.teamlitiaina.tragohelper.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.teamlitiaina.tragohelper.constants.PermissionCodes.Companion.LOCATION_PERMISSION_REQUEST_CODE
import com.teamlitiaina.tragohelper.constants.PermissionCodes.Companion.SETTINGS_REQUEST_CODE
import com.teamlitiaina.tragohelper.data.LocationData
import com.teamlitiaina.tragohelper.data.UserData
import com.teamlitiaina.tragohelper.databinding.ActivityMainBinding
import com.teamlitiaina.tragohelper.firebase.FirebaseObject
import com.teamlitiaina.tragohelper.fragment.FragmentChanger
import com.teamlitiaina.tragohelper.fragment.HomeFragment
import com.teamlitiaina.tragohelper.fragment.MapFragment

class MainActivity : AppCompatActivity(), FirebaseObject.Companion.FirebaseCallback {

    private lateinit var binding: ActivityMainBinding

    companion object {
        var currentUser: UserData? = null
        var mapFragment: MapFragment? = null
        var currentUserLatitude: String? = null
        var currentUserLongitude: String? = null
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

        requestPermissions()

        sharedPreferences = getSharedPreferences("currentUserLocation", Context.MODE_PRIVATE)
        currentUserLatitude = sharedPreferences.getString("latitude", "")
        currentUserLongitude = sharedPreferences.getString("longitude", "")

        if (FirebaseObject.auth.uid == null) {
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            finish()
        }

        //Debug
        binding.currentUserNameTextView.setOnClickListener {
            FirebaseObject.auth.signOut()
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            finish()
        }
        FirebaseObject.retrieveData("vehicleOwner", this)
    }

    override fun onDestroy() {
        super.onDestroy()
        mapFragment = null
    }

    override fun onAllDataReceived(dataArray: List<UserData>) {}

    override fun onUserDataReceived(data: UserData) {
        binding.currentUserNameTextView.text = data.name
        currentUser = data
        FirebaseObject.retrieveLocationDataByEmailRealTime(currentUser?.email.toString(), this)
    }

    override fun onLocationDataReceived(data: LocationData) {
        currentUserLatitude = data.latitude.toString()
        currentUserLongitude = data.longitude.toString()
        with(sharedPreferences.edit()) {
            putString("latitude", data.latitude.toString())
            putString("longitude", data.longitude.toString())
            apply()
        }
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
                FragmentChanger.replaceFragment(this@MainActivity, HomeFragment(), binding.dashboardLayout.id)
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
            FragmentChanger.replaceFragment(this@MainActivity, HomeFragment(), binding.dashboardLayout.id)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) {
                    FragmentChanger.replaceFragment(this@MainActivity, HomeFragment(), binding.dashboardLayout.id)
                } else {
                    showPermissionRationaleDialog()
                }
            }
        }
    }

}