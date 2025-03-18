package com.soft77reload.iptv

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class CanalesAdapter(private val channel:List<String>, private val onItemSelected: (String) -> Unit):RecyclerView.Adapter<CanalesViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CanalesViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return CanalesViewHolder(layoutInflater.inflate(R.layout.item_canal, parent, false))
    }

    override fun getItemCount(): Int = channel.size

    override fun onBindViewHolder(holder: CanalesViewHolder, position: Int) {
        holder.cargar(channel[position], onItemSelected)
    }

}
