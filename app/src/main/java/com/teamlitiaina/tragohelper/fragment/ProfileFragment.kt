package com.teamlitiaina.tragohelper.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.teamlitiaina.tragohelper.activity.LoginActivity
import com.teamlitiaina.tragohelper.activity.MainActivity
import com.teamlitiaina.tragohelper.databinding.FragmentProfileBinding
import com.teamlitiaina.tragohelper.firebase.FirebaseObject


class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.nameTextView.text = MainActivity.currentUser?.name
        binding.emailTextView.text = MainActivity.currentUser?.email

        binding.signOutImageButton.setOnClickListener {
            FirebaseObject.auth.signOut()
            MainActivity.mapFragment = null
            MainActivity.currentUser = null
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}