package com.teamlitiaina.tragohelper.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.teamlitiaina.tragohelper.databinding.CustomPopupRequestProgressInfoBinding
import com.teamlitiaina.tragohelper.databinding.FragmentRequestBinding
import com.teamlitiaina.tragohelper.dialog.PopupManager.Companion.displayInformationPopupWindow

class RequestFragment : Fragment() {

    private var _binding: FragmentRequestBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRequestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.trackProgressConstraintLayout.setOnClickListener {
            val popupBinding: CustomPopupRequestProgressInfoBinding = CustomPopupRequestProgressInfoBinding.inflate(LayoutInflater.from(requireContext()))
            val popupWindow = displayInformationPopupWindow(requireContext(), popupBinding, true, 0)
            popupBinding.okCardView.setOnClickListener {
                popupWindow.dismiss()
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}