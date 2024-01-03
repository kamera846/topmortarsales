package com.topmortar.topmortarsales.adapter.recyclerview

import android.content.Context
import android.os.Handler
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
import com.topmortar.topmortarsales.model.VoucherModel

class VoucherRecyclerViewAdapter(private val chatList: ArrayList<VoucherModel>, private val itemClickListener: ItemClickListener) : RecyclerView.Adapter<VoucherRecyclerViewAdapter.ChatViewHolder>() {
    private var context: Context? = null

    interface ItemClickListener {
        fun onItemClick(data: VoucherModel? = null)
    }

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val imgIcon: ImageView = itemView.findViewById(R.id.iv_contact_profile)
        val tvContactName: TextView = itemView.findViewById(R.id.tv_contact_name)
        val tvPhoneNumber: TextView = itemView.findViewById(R.id.tv_phone_number)
        private val imgChecklist: ImageView = itemView.findViewById(R.id.checklist)
        private val textVerified: TextView = itemView.findViewById(R.id.textVerified)

        fun bind(item: VoucherModel) {

            imgIcon.setImageResource(R.drawable.voucher_primary)
//            tvContactName.text = item.no_voucher + " (" + item.no_fisik.let { if (!it.isNullOrEmpty()) it else "Belum ada nomor fisik" } + ")"
            tvContactName.text = item.no_voucher
            if (item.exp_date.isNullOrEmpty()) tvPhoneNumber.text = "Berlaku " + DateFormat.format(item.date_voucher, format = "dd MMM yyyy") + " - belum ditentukan."
            else tvPhoneNumber.text = "Berlaku " + DateFormat.format(item.date_voucher, format = "dd MMM yyyy") + " - " + DateFormat.format(item.exp_date!!, format = "dd MMM yyyy")
//            tvPhoneNumber.text = item.no_voucher + " | Dibuat pada " + DateFormat.format(item.date_voucher, format = "dd MMM yyyy")
            if (item.is_claimed == "1") {
//                imgChecklist.visibility = View.VISIBLE
                textVerified.visibility = View.VISIBLE
                textVerified.text = "Diklaim"
            } else {
//                imgChecklist.visibility = View.GONE
                textVerified.visibility = View.GONE
                textVerified.text = ""
            }

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

//        holder.itemView.setOnClickListener { onItemClick(holder, position) }

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

            Handler().postDelayed({
                overlayView.alpha = 0f
                overlayView.visibility = View.GONE
            }, animateDuration)

            val data = chatList[position]
            itemClickListener.onItemClick(data)

        }
    }
}