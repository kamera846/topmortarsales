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
import com.topmortar.topmortarsales.commons.utils.DateFormat
import com.topmortar.topmortarsales.model.DeliveryModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HistoryDeliveryRecyclerViewAdapter(private val listItem: ArrayList<DeliveryModel.History>, private val itemClickListener: ItemClickListener) : RecyclerView.Adapter<HistoryDeliveryRecyclerViewAdapter.ChatViewHolder>() {
    private var context: Context? = null

    interface ItemClickListener {
        fun onItemClick(data: DeliveryModel.History? = null)
    }

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvContactName: TextView = itemView.findViewById(R.id.tv_contact_name)
        private val tvPhoneNumber: TextView = itemView.findViewById(R.id.tv_phone_number)
        val tooltipStatus: ImageView = itemView.findViewById(R.id.tooltip_status)

        fun bind(item: DeliveryModel.History) {
            var dateEnded = item.endDatetime

            if (dateEnded.isNotEmpty()) {
                val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(item.endDatetime)
                dateEnded = if (date != null) {
                    val calendar = Calendar.getInstance()
                    val currentYear = calendar.get(Calendar.YEAR)
                    val dateYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(date)

                    if (currentYear == dateYear.toInt()) DateFormat.format(item.endDatetime, "yyyy-MM-dd HH:mm:ss", "dd MMM, HH:mm")
                    else DateFormat.format(item.endDatetime, "yyyy-MM-dd HH:mm:ss", "dd MMM yyyy, HH:mm")
                } else DateFormat.format(item.endDatetime, "yyyy-MM-dd HH:mm:ss", "dd MMM, HH:mm")
            }

            tvContactName.text = item.nama
//            tvPhoneNumber.text = "Diselesaikan pada $dateEnded"
            tvPhoneNumber.text = "Diselesaikan oleh " + item.full_name

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