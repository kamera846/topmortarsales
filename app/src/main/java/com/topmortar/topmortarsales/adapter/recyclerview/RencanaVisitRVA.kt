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
import com.topmortar.topmortarsales.model.RencanaVisitModel
import java.util.Locale

class RencanaVisitRVA (private val listItem: ArrayList<RencanaVisitModel>, private val itemClickListener: ItemClickListener) : RecyclerView.Adapter<RencanaVisitRVA.ChatViewHolder>() {
    private var context: Context? = null
    private var typeRencana: String? = "jatem"

    interface ItemClickListener {
        fun onItemClick(data: RencanaVisitModel? = null)
    }

    fun setType(type: String) {
        this.typeRencana = type
    }

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvContactName: TextView = itemView.findViewById(R.id.tv_contact_name)
        private val tvPhoneNumber: TextView = itemView.findViewById(R.id.tv_phone_number)
        val checkListImage: ImageView = itemView.findViewById(R.id.checklist)
        private val imgProfile: ImageView = itemView.findViewById(R.id.iv_contact_profile)
        private val textVerified: TextView = itemView.findViewById(R.id.textVerified)
        private val badgeNew: TextView = itemView.findViewById(R.id.textCornerBadge)

        fun bind(item: RencanaVisitModel) {

            when (typeRencana) {
                "voucher" -> imgProfile.setImageResource(R.drawable.voucher_primary)
                "passive" -> imgProfile.setImageResource(R.drawable.store_primary)
                else -> imgProfile.setImageResource(R.drawable.time_primary)
            }

            val dateCounter = DateFormat.differenceDateNowDescCustom(item.created_at)

            if (dateCounter > 4 && typeRencana == "jatemPenagihan") itemView.setBackgroundColor(context!!.getColor(R.color.primary15))
            else itemView.setBackgroundColor(context!!.getColor(R.color.baseBackground))

            var dateJatem = when (typeRencana) {
                "voucher" -> "Didapatkan "
                "passive", "tagihMingguan" -> "Terakhir divisit "
                else -> "Jatuh tempo "
            }

//            dateJatem += if (item.termin_payment.isNotEmpty() && item.termin_payment != "-1" && (typeRencana == "jatem" || typeRencana == "jatemPenagihan")) {
//                val terminPayment = item.termin_payment.toInt()
//                DateFormat.changeDateToDaysBeforeOrAfter(item.date_invoice, terminPayment, outputDateFormat = "dd MMMM yyyy")
//            } else DateFormat.format(item.created_at)
            dateJatem += if ((typeRencana == "jatem" || typeRencana == "jatemPenagihan") && item.jatuh_tempo.isNotEmpty()) {
                DateFormat.format(dateString =  item.jatuh_tempo, inputFormat = "dd MMM yyyy", inputLocale = Locale.ENGLISH)
            } else DateFormat.format(item.created_at)

            if (!item.is_new.isNullOrEmpty()) {
                if (item.is_new == "1") {
                    if (typeRencana == "passive" || typeRencana == "tagihMingguan") dateJatem = "Belum divisit"
                    badgeNew.visibility = View.VISIBLE
                } else badgeNew.visibility = View.GONE
            }

            tvContactName.text = item.nama
            tvPhoneNumber.text = dateJatem
            textVerified.text = "$dateCounter hari"
            textVerified.setBackgroundResource(R.drawable.bg_passive_round)
            textVerified.visibility = View.VISIBLE

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