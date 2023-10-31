package com.teamlitiaina.tragohelper.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
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
        FragmentChanger.replaceFragment(this@MainActivity, HomeFragment(), binding.dashboardLayout.id)
        FirebaseObject.retrieveData("vehicleOwner", this)
    }

    override fun onAllDataReceived(dataArray: List<UserData>) {}

    override fun onUserDataReceived(data: UserData) {
        binding.currentUserNameTextView.text = "Hi, ${data.name}"
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

    override fun onDestroy() {
        super.onDestroy()
        mapFragment = null
    }
}