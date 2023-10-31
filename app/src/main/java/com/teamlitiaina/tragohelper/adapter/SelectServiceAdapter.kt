package com.teamlitiaina.tragohelper.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.teamlitiaina.tragohelper.R
import com.teamlitiaina.tragohelper.activity.MainActivity
import com.teamlitiaina.tragohelper.data.UserData
import com.teamlitiaina.tragohelper.databinding.ItemLayoutSelectServiceBinding
import com.teamlitiaina.tragohelper.fragment.FragmentChanger
import com.teamlitiaina.tragohelper.fragment.MapFragment

class SelectServiceAdapter (private val dataArray: List<UserData>, private val activity: Activity) : RecyclerView.Adapter<SelectServiceAdapter.ViewHolder>(){

    class ViewHolder(val binding: ItemLayoutSelectServiceBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemLayoutSelectServiceBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.nameTextView.text = dataArray[position].name
        holder.binding.addressTextView.text = dataArray[position].userUID
        holder.binding.distanceTextView.text = dataArray[position].phoneNumber
        holder.binding.viewCardView.setOnClickListener {
            MainActivity.mapFragment?.updateData(dataArray[position].email.toString())
        }
    }

    override fun getItemCount(): Int {
        return dataArray.size
    }

}