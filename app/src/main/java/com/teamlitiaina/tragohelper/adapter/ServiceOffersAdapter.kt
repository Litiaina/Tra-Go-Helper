package com.teamlitiaina.tragohelper.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.teamlitiaina.tragohelper.databinding.ItemLayoutServiceOfferBinding

class ServiceOffersAdapter(private val stringArray: List<String>) : RecyclerView.Adapter<ServiceOffersAdapter.ViewHolder>(){

    class ViewHolder(val binding: ItemLayoutServiceOfferBinding) : RecyclerView.ViewHolder(binding.root) {
        var currentString: String? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemLayoutServiceOfferBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return stringArray.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = stringArray[position]
        holder.currentString = currentItem
        holder.binding.serviceOfferName.text = currentItem
    }

}