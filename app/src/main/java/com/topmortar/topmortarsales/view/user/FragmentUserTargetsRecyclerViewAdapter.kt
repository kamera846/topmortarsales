package com.topmortar.topmortarsales.view.user

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.topmortar.topmortarsales.R

import com.topmortar.topmortarsales.view.user.placeholder.PlaceholderContent.PlaceholderItem
import com.topmortar.topmortarsales.databinding.ItemChatRoomBinding

/**
 * [RecyclerView.Adapter] that can display a [PlaceholderItem].
 * TODO: Replace the implementation with code for your data type.
 */
class FragmentUserTargetsRecyclerViewAdapter(
    private val values: List<PlaceholderItem>
) : RecyclerView.Adapter<FragmentUserTargetsRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            ItemChatRoomBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.idView.text = item.id
        holder.contentView.text = item.content
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: ItemChatRoomBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val idView: TextView = binding.tvContactName
        val contentView: TextView = binding.tvPhoneNumber

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }

}