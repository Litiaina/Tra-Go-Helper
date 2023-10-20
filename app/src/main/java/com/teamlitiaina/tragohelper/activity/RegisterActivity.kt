package com.teamlitiaina.tragohelper.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.view.WindowCompat
import com.teamlitiaina.tragohelper.data.UserData
import com.teamlitiaina.tragohelper.databinding.ActivityRegisterBinding
import com.teamlitiaina.tragohelper.firebase.FirebaseObject
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
                FirebaseObject.auth.createUserWithEmailAndPassword(binding.emailEditText.text.toString(), binding.passwordEditText.text.toString()).addOnSuccessListener {
                    FirebaseObject.database.getReference("vehicleOwner").child(FirebaseObject.auth.currentUser?.uid.toString()).setValue(
                        UserData(FirebaseObject.auth.currentUser?.uid.toString(),binding.nameEditText.text.toString(),binding.emailEditText.text.toString(),binding.phoneNumberEditText.text.toString(),"")
                    ).addOnSuccessListener {
                        Toast.makeText(this@RegisterActivity, "Register Success", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                        finish()
                    }.addOnFailureListener { e ->
                        Toast.makeText(this@RegisterActivity, "Error! $e", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { e ->
                    Toast.makeText(this@RegisterActivity, "Error! $e", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}