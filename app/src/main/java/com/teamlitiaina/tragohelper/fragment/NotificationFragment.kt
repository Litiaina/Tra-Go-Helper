package com.teamlitiaina.tragohelper.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.teamlitiaina.tragohelper.activity.MainActivity
import com.teamlitiaina.tragohelper.adapter.NotificationAdapter
import com.teamlitiaina.tragohelper.data.NotificationData
import com.teamlitiaina.tragohelper.databinding.FragmentNotificationBinding
import com.teamlitiaina.tragohelper.firebase.NotificationFirebaseBackend

class NotificationFragment : Fragment(), NotificationFirebaseBackend.Companion.NotificationFirebaseCallback {

    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.notificationRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        NotificationFirebaseBackend.retrieveNotificationByEmail(MainActivity.currentUser?.email.toString(), this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onNotificationReceived(notificationData: List<NotificationData>) {
        if (!isAdded) {
            return
        }
        binding.noNotificationsViewRelativeLayout.isVisible = notificationData.isEmpty()
        Log.d("NotificationFragment", "Received ${notificationData.size} notifications")
        binding.notificationRecyclerView.adapter = NotificationAdapter(notificationData, requireActivity())
    }
}