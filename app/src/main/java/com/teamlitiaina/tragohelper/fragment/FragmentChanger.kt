package com.teamlitiaina.tragohelper.fragment

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

class FragmentChanger {
    companion object {
        fun replaceFragment(activity: Activity, fragment: Fragment, id: Int) {
            val fragmentManager: FragmentManager = (activity as AppCompatActivity).supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.replace(id, fragment)
            fragmentTransaction.commit()
        }
    }
}
