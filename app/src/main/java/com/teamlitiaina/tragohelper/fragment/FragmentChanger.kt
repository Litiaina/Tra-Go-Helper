package com.teamlitiaina.tragohelper.fragment

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

class FragmentChanger : AppCompatActivity() {
    companion object {
        fun replaceFragment(activity: AppCompatActivity, fragment: Fragment, id: Int) {
            val fragmentManager: FragmentManager = activity.supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.replace(id, fragment)
            fragmentTransaction.commit()
        }
    }
}
