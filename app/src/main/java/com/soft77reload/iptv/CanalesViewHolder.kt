package com.soft77reload.iptv

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.soft77reload.iptv.databinding.ItemCanalBinding

class CanalesViewHolder (view: View):RecyclerView.ViewHolder(view){
    private val binding = ItemCanalBinding.bind(view)

    fun cargar(canal: String, onItemSelected: (String) -> Unit){
        binding.tvCanal.text = canal
        binding.root.setOnClickListener { onItemSelected(canal) }
    }
}