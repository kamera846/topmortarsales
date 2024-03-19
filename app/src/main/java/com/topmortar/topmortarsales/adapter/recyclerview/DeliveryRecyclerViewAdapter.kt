package com.topmortar.topmortarsales.adapter.recyclerview

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.model.DeliveryModel

class DeliveryRecyclerViewAdapter(private val chatList: ArrayList<DeliveryModel.Store>, private val itemClickListener: ItemClickListener) : RecyclerView.Adapter<DeliveryRecyclerViewAdapter.ChatViewHolder>() {
    private var context: Context? = null

    interface ItemClickListener {
        fun onItemClick(data: DeliveryModel.Store? = null)
    }

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val imgIcon: ImageView = itemView.findViewById(R.id.iv_contact_profile)
        val tvContactName: TextView = itemView.findViewById(R.id.tv_contact_name)
        val tvPhoneNumber: TextView = itemView.findViewById(R.id.tv_phone_number)

        fun bind(item: DeliveryModel.Store) {

            tvContactName.text = item.name
            tvPhoneNumber.text = "Dikirim oleh " + item.courier?.name

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_room, parent, false)
        context = parent.context
        return ChatViewHolder(view)

    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {

        val item = chatList[position]

        holder.bind(item)
        holder.itemView.startAnimation(AnimationUtils.loadAnimation(holder.itemView.context, R.anim.rv_item_fade_slide_up))

        holder.itemView.setOnClickListener { onItemClick(holder, position) }

    }

    override fun getItemCount(): Int {

        return chatList.size

    }

    private fun onItemClick(holder: ChatViewHolder, position: Int) {
        if (position != RecyclerView.NO_POSITION) {
            val animateDuration = 200L

            val fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in)
            fadeIn.duration = animateDuration

            val overlayView = holder.itemView.findViewById<LinearLayout>(R.id.overlay_view)

            overlayView.alpha = 0.7f
            overlayView.visibility = View.VISIBLE
            overlayView.startAnimation(fadeIn)

            Handler(Looper.getMainLooper()).postDelayed({
                overlayView.alpha = 0f
                overlayView.visibility = View.GONE
            }, animateDuration)

            val data = chatList[position]
            itemClickListener.onItemClick(data)

        }
    }
}