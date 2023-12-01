package com.teamlitiaina.tragohelper.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.view.WindowCompat
import com.teamlitiaina.tragohelper.data.UserData
import com.teamlitiaina.tragohelper.databinding.ActivityRegisterBinding
import com.teamlitiaina.tragohelper.dialog.LoadingDialog
import com.teamlitiaina.tragohelper.firebase.FirebaseBackend
import com.teamlitiaina.tragohelper.validation.Validation

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginTextView.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.registerButton.setOnClickListener {
            if (!Validation.isTextEmpty(
                    this@RegisterActivity, binding.nameEditText.text.toString(),
                    binding.emailEditText.text.toString(),
                    binding.passwordEditText.text.toString(),
                    binding.confirmPasswordEditText.text.toString(),
                    binding.phoneNumberEditText.text.toString(),
                    message = "All fields are required") &&
                !Validation.emailNotValid(
                    this@RegisterActivity,
                    binding.emailEditText.text.toString(),
                    "Email invalid format") &&
                !Validation.passwordNotValid(
                    this@RegisterActivity,
                    binding.passwordEditText.text.toString(),
                    binding.confirmPasswordEditText.text.toString(),
                    firstMessage = "Password doesn't match" ,
                    secondMessage = "Password must at least 6 digits") &&
                !Validation.phoneNumberNotValid(
                    this@RegisterActivity,
                    binding.phoneNumberEditText.text.toString(),
                    "Phone number not valid") ) {
                val loadingDialog = LoadingDialog()
                loadingDialog.show(supportFragmentManager, "Loading")
                if (Validation.isInternetAvailable(this@RegisterActivity)) {
                    FirebaseBackend.auth.createUserWithEmailAndPassword(binding.emailEditText.text.toString(), binding.passwordEditText.text.toString()).addOnSuccessListener {
                        FirebaseBackend.database.getReference("vehicleOwner").child(FirebaseBackend.auth.currentUser?.uid.toString()).setValue(
                            UserData(FirebaseBackend.auth.currentUser?.uid.toString(),binding.nameEditText.text.toString(),binding.emailEditText.text.toString(),binding.phoneNumberEditText.text.toString(),"")
                        ).addOnSuccessListener {
                            with(MainActivity.sharedPreferences.edit()) {
                                putString("auth", FirebaseBackend.auth.uid)
                                apply()
                            }
                            Toast.makeText(this@RegisterActivity, "Register Success", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                            finish()
                            loadingDialog.dismiss()
                        }.addOnFailureListener { e ->
                            Toast.makeText(this@RegisterActivity, "Error! $e", Toast.LENGTH_SHORT).show()
                            loadingDialog.dismiss()
                        }
                    }.addOnFailureListener { e ->
                        Toast.makeText(this@RegisterActivity, "Error! $e", Toast.LENGTH_SHORT).show()
                        loadingDialog.dismiss()
                    }
                } else {
                    Toast.makeText(this@RegisterActivity, "No internet connection", Toast.LENGTH_SHORT).show()
                    loadingDialog.dismiss()
                }
            }
        }
    }
}