package com.teamlitiaina.tragohelper.dialog

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.teamlitiaina.tragohelper.databinding.CustomLayoutRequestInformationDialogBinding

class RequestInformationDialog(): DialogFragment() {

    private var _binding: CustomLayoutRequestInformationDialogBinding? = null
    private val binding get() = _binding!!
    private var dialog: AlertDialog? = null


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        _binding = CustomLayoutRequestInformationDialogBinding.inflate(LayoutInflater.from(requireContext()))
        builder.setView(binding.root)
        dialog = builder.create()

        binding.okCardView.setOnClickListener {
            dialog!!.dismiss()
        }

        return dialog!!
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        dialog?.dismiss()
    }

}