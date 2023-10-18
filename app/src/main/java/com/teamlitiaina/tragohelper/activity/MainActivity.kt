package com.teamlitiaina.tragohelper.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.WindowCompat
import com.teamlitiaina.tragohelper.data.UserData
import com.teamlitiaina.tragohelper.databinding.ActivityMainBinding
import com.teamlitiaina.tragohelper.firebase.FirebaseObject
import com.teamlitiaina.tragohelper.fragment.FragmentChanger
import com.teamlitiaina.tragohelper.fragment.MapFragment

class MainActivity : AppCompatActivity(), FirebaseObject.Companion.FirebaseCallback {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(binding.root)

        if (FirebaseObject.auth.uid == null) {
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            finish()
        }

        FirebaseObject.retrieveData("vehicle_owner_user", this)
        FragmentChanger.replaceFragment(this@MainActivity, MapFragment(), binding.dashboardLayout.id)
    }
    override fun onDataReceived(data: UserData) {
        binding.currentUserNameTextView.text = "Hi, ${data.name}" ?: "null"
    }

}