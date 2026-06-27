package com.topmortar.topmortarsales.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat.getColor
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.model.HobbyModel

class HobbyAdapter(
    private val onToggle: (item: HobbyModel, isSelected: Boolean) -> Unit
) : ListAdapter<HobbyModel, HobbyAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtHobby: TextView = view.findViewById(R.id.txtHobby)
        val txtPath: TextView = view.findViewById(R.id.txtPath)
    }

    class DiffCallback : DiffUtil.ItemCallback<HobbyModel>() {
        override fun areItemsTheSame(old: HobbyModel, new: HobbyModel) =
            old.id_hobi == new.id_hobi

        override fun areContentsTheSame(old: HobbyModel, new: HobbyModel) =
            old == new  // Pastikan HobbyModel adalah data class
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hobby, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val hobby = getItem(position)
        holder.txtHobby.text = hobby.name_hobi
        holder.txtPath.text = hobby.label
        updateBackground(holder.itemView, hobby.isSelected)
        holder.itemView.setOnClickListener {
            onToggle(hobby, !hobby.isSelected)
        }
    }

    private fun updateBackground(view: View, isSelected: Boolean) {
        val colorRes = if (isSelected) R.color.primary15 else R.color.baseBackground
        view.setBackgroundColor(getColor(view.context, colorRes))
    }
}