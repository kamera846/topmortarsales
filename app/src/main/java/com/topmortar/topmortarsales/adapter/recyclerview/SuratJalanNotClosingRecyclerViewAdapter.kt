package com.topmortar.topmortarsales.adapter.recyclerview

import android.annotation.SuppressLint
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
import androidx.recyclerview.widget.RecyclerView
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.utils.DateFormat
import com.topmortar.topmortarsales.model.SuratJalanNotClosingModel

@SuppressLint("SetTextI18n")
class SuratJalanNotClosingRecyclerViewAdapter (private val listItem: ArrayList<SuratJalanNotClosingModel>, private val itemClickListener: ItemClickListener) : RecyclerView.Adapter<SuratJalanNotClosingRecyclerViewAdapter.ChatViewHolder>() {
    private var context: Context? = null

    interface ItemClickListener {
        fun onItemClick(data: SuratJalanNotClosingModel? = null)
    }

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvContactName: TextView = itemView.findViewById(R.id.tv_contact_name)
        private val tvPhoneNumber: TextView = itemView.findViewById(R.id.tv_phone_number)
        val checkListImage: ImageView = itemView.findViewById(R.id.checklist)
        private val icPhone: ImageView = itemView.findViewById(R.id.icPhoneNumber)
        private val deliveryStatus: LinearLayout = itemView.findViewById(R.id.deliveryStatus)
        private val deliveryStatusText: TextView = itemView.findViewById(R.id.deliveryStatusText)
        private val deliveryStatusIcon: ImageView = itemView.findViewById(R.id.deliveryStatusIcon)

        fun bind(item: SuratJalanNotClosingModel) {
            val dateProcessed = item.dateProcessed
            var dateCounter = item.created_at.let {
                if (it.isNotEmpty()) {
                    DateFormat.differenceDateNowDescCustom(it)
                } else -1
            }

            if (dateProcessed.isNotEmpty()) {
                deliveryStatusIcon.setImageResource(R.drawable.truck_fast_white_only)
                dateCounter = dateProcessed.let {
                    if (it.isNotEmpty()) {
                        DateFormat.differenceDateNowDescCustom(it)
                    } else -1
                }
            } else {
                deliveryStatusIcon.setImageResource(R.drawable.time_line_dark_white_only)
            }

            deliveryStatus.visibility = View.VISIBLE
            dateCounter.let { if (it != -1) {
                    deliveryStatusText.visibility = View.VISIBLE
                    deliveryStatusText.text = "$it hari"
                } else {
                    deliveryStatusText.visibility = View.GONE
                }
            }

            tvContactName.text = item.nama
            tvPhoneNumber.text = "${item.full_name} - ${item.kode_city}"

            icPhone.visibility = View.VISIBLE

            setupStatus(dateProcessed)

        }

        private fun setupStatus(status: String) {

            if (status.isNotEmpty()) tooltipHandler(checkListImage, "Sedang diproses")
            else tooltipHandler(checkListImage, "Menunggu untuk diproses")

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

        val item = listItem[position]

        holder.bind(item)
        holder.itemView.startAnimation(AnimationUtils.loadAnimation(holder.itemView.context, R.anim.rv_item_fade_slide_up))

        holder.itemView.setOnClickListener { onItemClick(holder, position) }
        holder.checkListImage.setOnClickListener { onItemClick(holder, position) }

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