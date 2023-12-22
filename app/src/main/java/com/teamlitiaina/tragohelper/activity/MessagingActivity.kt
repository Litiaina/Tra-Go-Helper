package com.teamlitiaina.tragohelper.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.teamlitiaina.tragohelper.databinding.ActivityMessagingBinding

class MessagingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMessagingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessagingBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}