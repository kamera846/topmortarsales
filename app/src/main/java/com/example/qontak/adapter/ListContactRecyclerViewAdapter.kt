package com.example.qontak.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.qontak.NewRoomChatFormActivity
import com.example.qontak.R
import com.example.qontak.commons.ET_NAME
import com.example.qontak.commons.ET_PHONE
import com.example.qontak.commons.MAIN_ACTIVITY_REQUEST_CODE
import com.example.qontak.model.ContactModel

class ListContactRecyclerViewAdapter(private val ctx: Context, private val chatList: ArrayList<ContactModel>) : RecyclerView.Adapter<ListContactRecyclerViewAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

//        private val ivContactProfile: ImageView = itemView.findViewById(R.id.iv_contact_profile)
        private val tvContactName: TextView = itemView.findViewById(R.id.tv_contact_name)
        private val tvPhoneNumber: TextView = itemView.findViewById(R.id.tv_phone_number)

        fun bind(chatItem: ContactModel) {

//            ivContactProfile.setImageResource(chatItem.profileImage)
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

        holder.itemView.setOnClickListener {

            val intent = Intent(ctx, NewRoomChatFormActivity::class.java)

            intent.putExtra(ET_NAME, chatItem.nama)
            intent.putExtra(ET_PHONE, chatItem.nomorhp)

            if (ctx is Activity) ctx.startActivityForResult(intent, MAIN_ACTIVITY_REQUEST_CODE)

        }

    }

    override fun getItemCount(): Int {

        return chatList.size

    }
}