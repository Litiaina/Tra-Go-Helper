package com.teamlitiaina.tragohelper.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.view.WindowCompat
import com.teamlitiaina.tragohelper.databinding.ActivityLoginBinding
import com.teamlitiaina.tragohelper.dialog.LoadingDialog
import com.teamlitiaina.tragohelper.firebase.FirebaseBackend
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
            val loadingDialog = LoadingDialog()
            loadingDialog.show(supportFragmentManager, "Loading")
            if (!Validation.isTextEmpty(this@LoginActivity, binding.emailEditText.text.toString(), binding.passwordEditText.text.toString(), message = "All fields are required")) {
                if (Validation.isInternetAvailable(this@LoginActivity)) {
                    FirebaseBackend.auth.signInWithEmailAndPassword(binding.emailEditText.text.toString(), binding.passwordEditText.text.toString()).addOnSuccessListener {
                        with(MainActivity.sharedPreferences.edit()) {
                            putString("auth", FirebaseBackend.auth.uid)
                            apply()
                        }
                        Toast.makeText(this@LoginActivity, "Login Success", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                        loadingDialog.dismiss()
                    }.addOnFailureListener { _ ->
                        Toast.makeText(this@LoginActivity, "Invalid email or password", Toast.LENGTH_SHORT).show()
                        loadingDialog.dismiss()
                    }
                } else {
                    Toast.makeText(this@LoginActivity, "No internet connection", Toast.LENGTH_SHORT).show()
                    loadingDialog.dismiss()
                }
            } else {
                loadingDialog.dismiss()
            }
        }
    }
}