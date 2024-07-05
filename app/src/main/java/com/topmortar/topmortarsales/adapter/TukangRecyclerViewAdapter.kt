package com.topmortar.topmortarsales.adapter

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
import androidx.appcompat.widget.TooltipCompat
import androidx.core.content.ContextCompat.getDrawable
import androidx.recyclerview.widget.RecyclerView
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.STATUS_CONTACT_ACTIVE
import com.topmortar.topmortarsales.commons.STATUS_CONTACT_BLACKLIST
import com.topmortar.topmortarsales.commons.STATUS_CONTACT_DATA
import com.topmortar.topmortarsales.commons.STATUS_CONTACT_PASSIVE
import com.topmortar.topmortarsales.model.TukangModel

class TukangRecyclerViewAdapter(private val chatList: ArrayList<TukangModel>, private val itemClickListener: ItemClickListener) : RecyclerView.Adapter<TukangRecyclerViewAdapter.ChatViewHolder>() {
    private var context: Context? = null

    interface ItemClickListener {
        fun onItemClick(data: TukangModel? = null)
    }

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val ivProfile: ImageView = itemView.findViewById(R.id.iv_contact_profile)
        private val tvContactName: TextView = itemView.findViewById(R.id.tv_contact_name)
        private val tvPhoneNumber: TextView = itemView.findViewById(R.id.tv_phone_number)
        val tooltipStatus: ImageView = itemView.findViewById(R.id.tooltip_status)

        fun bind(chatItem: TukangModel) {

            ivProfile.setImageResource(R.drawable.person_red)
            tvContactName.text = chatItem.nama
            val phoneNumber = "+${ chatItem.nomorhp }"
            val skillCode = if (chatItem.kode_skill.isNotEmpty()) "(${chatItem.kode_skill})" else ""
            tvPhoneNumber.text = if (chatItem.nomorhp != "") "$phoneNumber $skillCode" else ""
            setupStatus(chatItem.tukang_status)

        }

        private fun setupStatus(status: String? = null) {
            tooltipStatus.visibility = View.VISIBLE
            when (status) {
                STATUS_CONTACT_DATA -> {
                    tooltipStatus.setImageDrawable(context?.let { getDrawable(it, R.drawable.status_data) })
                    tooltipHandler(tooltipStatus, "Tukang Status Data")
                }
                STATUS_CONTACT_PASSIVE -> {
                    tooltipStatus.setImageDrawable(context?.let { getDrawable(it, R.drawable.status_passive) })
                    tooltipHandler(tooltipStatus, "Tukang Status Passive")
                }
                STATUS_CONTACT_ACTIVE -> {
                    tooltipStatus.setImageDrawable(context?.let { getDrawable(it, R.drawable.status_active) })
                    tooltipHandler(tooltipStatus, "Tukang Status Active")
                }
                STATUS_CONTACT_BLACKLIST -> {
                    tooltipStatus.setImageDrawable(context?.let { getDrawable(it, R.drawable.status_blacklist) })
                    tooltipHandler(tooltipStatus, "Tukang Status Blacklist")
                }
                else -> {
                    tooltipStatus.visibility = View.GONE
                }
            }
        }

        private fun tooltipHandler(content: ImageView, text: String) {
            content.setOnLongClickListener {
                TooltipCompat.setTooltipText(content, text)
                false
            }
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

        holder.itemView.setOnClickListener { onItemClick(holder, position) }
        holder.tooltipStatus.setOnClickListener { onItemClick(holder, position) }

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