package com.teamlitiaina.tragohelper.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.teamlitiaina.tragohelper.data.NotificationData
import com.teamlitiaina.tragohelper.databinding.ItemLayoutNotificationBinding

class NotificationAdapter(private var dataArray: List<NotificationData>, private val activity: Activity) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemLayoutNotificationBinding) : RecyclerView.ViewHolder(binding.root)
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemLayoutNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = dataArray[position]
        holder.binding.notificationTitleTextView.text = currentItem.title
        holder.binding.notificationInformationTextView.text = currentItem.information
        holder.binding.dateTimeTextView.text = currentItem.datetime
        holder.binding.viewNotificationTextView.setOnClickListener {

        }
    }

    override fun getItemCount(): Int {
        return dataArray.size
    }

}