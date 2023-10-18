package com.teamlitiaina.tragohelper.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.WindowCompat
import com.teamlitiaina.tragohelper.databinding.ActivityMainBinding
import com.teamlitiaina.tragohelper.firebase.FirebaseObject

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (FirebaseObject.auth.uid == null) {
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            finish()
        }

        binding.signOutButton.setOnClickListener {
            FirebaseObject.auth.signOut()
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            finish()
        }
    }
}