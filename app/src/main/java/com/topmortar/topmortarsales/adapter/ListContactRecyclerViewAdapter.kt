package com.topmortar.topmortarsales.adapter

import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.model.ContactModel

class ListContactRecyclerViewAdapter(private val chatList: ArrayList<ContactModel>, private val itemClickListener: ItemClickListener) : RecyclerView.Adapter<ListContactRecyclerViewAdapter.ChatViewHolder>() {
    private var context: Context? = null

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
        context = parent.context
        return ChatViewHolder(view)

    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {

        val chatItem = chatList[position]

        holder.bind(chatItem)
        holder.itemView.startAnimation(AnimationUtils.loadAnimation(holder.itemView.context, R.anim.rv_item_fade_slide_up))

        holder.itemView.setOnClickListener {

            if (position != RecyclerView.NO_POSITION) {
                val animateDuration = 200L

                val fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in)
                fadeIn.duration = animateDuration

                val overlayView = holder.itemView.findViewById<LinearLayout>(R.id.overlay_view)

                overlayView.alpha = 0.7f
                overlayView.visibility = View.VISIBLE
                overlayView.startAnimation(fadeIn)

                Handler().postDelayed({
                    overlayView.alpha = 0f
                    overlayView.visibility = View.GONE
                }, animateDuration)

                val data = chatList[position]
                itemClickListener.onItemClick(data)

            }

        }

    }

    override fun getItemCount(): Int {

        return chatList.size

    }
}