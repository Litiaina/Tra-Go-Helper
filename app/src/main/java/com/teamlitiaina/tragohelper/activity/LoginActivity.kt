package com.teamlitiaina.tragohelper.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.view.WindowCompat
import com.teamlitiaina.tragohelper.databinding.ActivityLoginBinding
import com.teamlitiaina.tragohelper.firebase.FirebaseObject
import com.teamlitiaina.tragohelper.validation.Validation

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.registerTextView.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }

        binding.loginButton.setOnClickListener {
            if (!Validation.isTextEmpty(this@LoginActivity, binding.emailEditText.text.toString(), binding.passwordEditText.text.toString(), message = "All fields are required")) {
                FirebaseObject.auth.signInWithEmailAndPassword(binding.emailEditText.text.toString(), binding.passwordEditText.text.toString()).addOnSuccessListener {
                    Toast.makeText(this@LoginActivity, "Login Success", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                }.addOnFailureListener { e ->
                    Toast.makeText(this@LoginActivity, "Invalid email or password", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}