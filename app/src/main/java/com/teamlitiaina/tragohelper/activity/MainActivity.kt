package com.teamlitiaina.tragohelper.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import com.teamlitiaina.tragohelper.data.LocationData
import com.teamlitiaina.tragohelper.data.UserData
import com.teamlitiaina.tragohelper.databinding.ActivityMainBinding
import com.teamlitiaina.tragohelper.firebase.FirebaseObject
import com.teamlitiaina.tragohelper.fragment.FragmentChanger
import com.teamlitiaina.tragohelper.fragment.HomeFragment

class MainActivity : AppCompatActivity(), FirebaseObject.Companion.FirebaseCallback {

    private lateinit var binding: ActivityMainBinding

    companion object {
        var currentUserEmail: String? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        )
        setContentView(binding.root)

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
        FragmentChanger.replaceFragment(this@MainActivity, HomeFragment(), binding.dashboardLayout.id)
    }
    override fun onDataReceived(data: UserData) {
        binding.currentUserNameTextView.text = "Hi, ${data.name}"
        currentUserEmail = data.email.toString()
    }

    override fun onLocationDataReceived(data: LocationData) {}

}