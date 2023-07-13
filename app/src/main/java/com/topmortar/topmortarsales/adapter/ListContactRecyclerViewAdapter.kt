package com.topmortar.topmortarsales.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.model.ContactModel

class ListContactRecyclerViewAdapter(private val chatList: ArrayList<ContactModel>, private val itemClickListener: ItemClickListener) : RecyclerView.Adapter<ListContactRecyclerViewAdapter.ChatViewHolder>() {

    interface ItemClickListener {
        fun onItemClick(data: ContactModel? = null)
    }

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvContactName: TextView = itemView.findViewById(R.id.tv_contact_name)
        private val tvPhoneNumber: TextView = itemView.findViewById(R.id.tv_phone_number)

        fun bind(chatItem: ContactModel) {

            tvContactName.text = chatItem.nama
            tvPhoneNumber.text = if (chatItem.nomorhp != "") "+${ chatItem.nomorhp }" else ""

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_room, parent, false)
        return ChatViewHolder(view)

    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {

        val chatItem = chatList[position]

        holder.bind(chatItem)
        holder.itemView.startAnimation(AnimationUtils.loadAnimation(holder.itemView.context, R.anim.rv_item_fade_slide_up))

        holder.itemView.setOnClickListener {

            if (position != RecyclerView.NO_POSITION) {
                val data = chatList[position]
                itemClickListener.onItemClick(data)
            }

        }

    }

    override fun getItemCount(): Int {

        return chatList.size

    }
}