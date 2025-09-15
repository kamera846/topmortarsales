package com.topmortar.topmortarsales.adapter.recyclerview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
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
import com.topmortar.topmortarsales.commons.utils.DateFormat
import com.topmortar.topmortarsales.model.DeliveryModel

@SuppressLint("SetTextI18n")
class HistoryDeliveryRecyclerViewAdapter(private val listItem: ArrayList<DeliveryModel.History>, private val itemClickListener: ItemClickListener) : RecyclerView.Adapter<HistoryDeliveryRecyclerViewAdapter.ChatViewHolder>() {
    private var context: Context? = null
    private var isHistoryCourier: Boolean = false

    interface ItemClickListener {
        fun onItemClick(data: DeliveryModel.History? = null)
    }

    fun isHistoryCourier(state: Boolean) {
        this.isHistoryCourier = state
    }

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvContactName: TextView = itemView.findViewById(R.id.tv_contact_name)
        private val tvPhoneNumber: TextView = itemView.findViewById(R.id.tv_phone_number)
        private val tvNotes: TextView = itemView.findViewById(R.id.tv_notes)
        val tooltipStatus: ImageView = itemView.findViewById(R.id.tooltip_status)

        fun bind(item: DeliveryModel.History) {
            var itemDescription = "Diselesaikan pada " + DateFormat.format(item.endDatetime, "yyyy-MM-dd HH:mm:ss", "dd MMM, HH:mm")

            if (!isHistoryCourier) {
                itemDescription = if (item.sj != null) {
                    "${item.sj?.no_surat_jalan ?: ""} - ${item.full_name}"
                } else {
                    item.full_name
                }

                val itemClosingDate = if (item.sj != null) {
                    item.sj?.date_closing ?: ""
                } else {
                    item.endDatetime
                }

                tvNotes.text = "* Diclosing pada ${DateFormat.format(itemClosingDate, "yyyy-MM-dd HH:mm:ss", "dd MMM, HH:mm")}"
                tvNotes.setTypeface(null, Typeface.NORMAL)

                tvNotes.visibility = View.VISIBLE
            }

            tvContactName.text = item.nama
            tvPhoneNumber.text = itemDescription

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_room, parent, false)
        context = parent.context
        return ChatViewHolder(view)

    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {

        val item = listItem[position]

        holder.bind(item)
        holder.itemView.startAnimation(AnimationUtils.loadAnimation(holder.itemView.context, R.anim.rv_item_fade_slide_up))

        holder.itemView.setOnClickListener { onItemClick(holder, position) }
        holder.tooltipStatus.setOnClickListener { onItemClick(holder, position) }

    }

    override fun getItemCount(): Int {

        return listItem.size

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

            val data = listItem[position]
            itemClickListener.onItemClick(data)

        }
    }
}