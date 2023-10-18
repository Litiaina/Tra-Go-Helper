package com.teamlitiaina.tragohelper.validation

import android.content.Context
import android.util.Patterns
import android.widget.Toast

class Validation {
    companion object {
        fun isTextEmpty(context: Context, vararg values: String, message: String): Boolean {
            for(value in values) {
                if (value.isEmpty()) {
                    Toast.makeText(context, message , Toast.LENGTH_SHORT).show()
                    return true
                }
            }
            return false
        }
        fun passwordNotValid(context: Context, password: String, confirmPassword: String, firstMessage: String, secondMessage: String): Boolean {
            if (password != confirmPassword) {
                Toast.makeText(context, firstMessage , Toast.LENGTH_SHORT).show()
                return true
            } else if (password.length < 6) {
                Toast.makeText(context, secondMessage , Toast.LENGTH_SHORT).show()
                return true
            }
            return false
        }
        fun emailNotValid(context: Context, target: String, message: String): Boolean {
            if(!Patterns.EMAIL_ADDRESS.matcher(target).matches()) {
                Toast.makeText(context, message , Toast.LENGTH_SHORT).show()
                return true
            }
            return false
        }
    }
}