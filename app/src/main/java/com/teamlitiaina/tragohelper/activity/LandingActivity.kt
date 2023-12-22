package com.teamlitiaina.tragohelper.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import com.teamlitiaina.tragohelper.R
import com.teamlitiaina.tragohelper.databinding.ActivityLandingBinding

class LandingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLandingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLandingBinding.inflate(layoutInflater)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        )
        setContentView(binding.root)
    }
}