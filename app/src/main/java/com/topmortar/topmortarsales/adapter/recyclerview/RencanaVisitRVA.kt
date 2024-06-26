package com.topmortar.topmortarsales.adapter.recyclerview

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.utils.DateFormat
import com.topmortar.topmortarsales.model.RencanaVisitModel
import java.util.Locale

class RencanaVisitRVA (private val listItem: ArrayList<RencanaVisitModel>, private val itemClickListener: ItemClickListener) : RecyclerView.Adapter<RencanaVisitRVA.ChatViewHolder>() {

    var callback: ((ArrayList<RencanaVisitModel>) -> Unit)? = null

    private var context: Context? = null
    private var typeRencana: String? = "jatem"
    private var isSelectBarActive = false
    private var selectedItems = SparseBooleanArray()

    interface ItemClickListener {
        fun onItemClick(data: RencanaVisitModel? = null)
        fun updateSelectedCount(count: Int? = null)
    }

    fun setType(type: String) {
        this.typeRencana = type
    }

    fun setSelectBarActive(state: Boolean) {
        this.isSelectBarActive = state
    }

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvContactName: TextView = itemView.findViewById(R.id.tv_contact_name)
        private val tvPhoneNumber: TextView = itemView.findViewById(R.id.tv_phone_number)
        val checkListImage: ImageView = itemView.findViewById(R.id.checklist)
        private val imgProfile: ImageView = itemView.findViewById(R.id.iv_contact_profile)
        private val textVerified: TextView = itemView.findViewById(R.id.textVerified)
        private val badgeNew: TextView = itemView.findViewById(R.id.textCornerBadge)
        val checkBoxItem: CheckBox = itemView.findViewById(R.id.checkbox)

        fun bind(item: RencanaVisitModel) {

            val dateCounter = item.created_at.let {
                if (it != null) {
                    DateFormat.differenceDateNowDescCustom(it)
                } else -1
            }

            when (typeRencana) {
                "voucher" -> imgProfile.setImageResource(R.drawable.voucher_primary)
                "passive" -> imgProfile.setImageResource(R.drawable.store_primary)
                else -> imgProfile.setImageResource(R.drawable.time_primary)
            }

            if (isSelectBarActive) {
                imgProfile.visibility = View.GONE
                checkBoxItem.visibility = View.VISIBLE
            } else {
                imgProfile.visibility = View.VISIBLE
                checkBoxItem.visibility = View.GONE
            }

            checkBoxItem.isChecked = selectedItems.get(position, false)
            itemView.setBackgroundColor(
                if (selectedItems.get(position, false)) {
                    itemView.context.resources.getColor(R.color.primary15)
                } else {
                    if (dateCounter > 4 && (typeRencana == "jatemPenagihan1" || typeRencana == "jatemPenagihan2" || typeRencana == "jatemPenagihan3")) itemView.context.resources.getColor(R.color.primary15)
                    else itemView.context.resources.getColor(R.color.baseBackground)
                }
            )

            var dateJatem = when (typeRencana) {
                "voucher" -> "Didapatkan "
                "jatem","jatemPenagihan2","jatemPenagihan3", "passive", "tagihMingguan" -> "Terakhir divisit "
                else -> "Jatuh tempo "
            }

//            dateJatem += if (item.termin_payment.isNotEmpty() && item.termin_payment != "-1" && (typeRencana == "jatem" || typeRencana == "jatemPenagihan")) {
//                val terminPayment = item.termin_payment.toInt()
//                DateFormat.changeDateToDaysBeforeOrAfter(item.date_invoice, terminPayment, outputDateFormat = "dd MMMM yyyy")
//            } else DateFormat.format(item.created_at)
            dateJatem += if ((typeRencana == "jatemPenagihan1") && item.jatuh_tempo.isNotEmpty()) {
                DateFormat.format(dateString =  item.jatuh_tempo, inputFormat = "dd MMM yyyy", inputLocale = Locale.ENGLISH)
            } else DateFormat.format(item.created_at ?: "0000-00-00")

            if (!item.is_new.isNullOrEmpty()) {
                if (item.is_new == "1" || item.created_at == null) {
                    if (typeRencana == "jatem" || typeRencana == "jatemPenagihan2" || typeRencana == "jatemPenagihan3" || typeRencana == "passive" || typeRencana == "tagihMingguan") dateJatem = "Belum divisit"
                    badgeNew.visibility = View.VISIBLE
                } else badgeNew.visibility = View.GONE
            }

            tvContactName.text = item.nama
            tvPhoneNumber.text = dateJatem
            textVerified.text = dateCounter.let { if (it != -1) "$it hari" else "error" }
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
        if (!isSelectBarActive) holder.itemView.startAnimation(AnimationUtils.loadAnimation(holder.itemView.context, R.anim.rv_item_fade_slide_up))

        holder.itemView.setOnClickListener { if (isSelectBarActive) toggleSelection(holder, position) else onItemClick(holder, position) }
        holder.checkListImage.setOnClickListener { onItemClick(holder, position) }
        holder.checkBoxItem.setOnClickListener { toggleSelection(holder, position) }

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

    private fun toggleSelection(holder: ChatViewHolder, position: Int) {
        if (selectedItems.get(position, false)) {
            selectedItems.delete(position)
            holder.checkBoxItem.isChecked = false
        } else {
            selectedItems.put(position, true)
            holder.checkBoxItem.isChecked = true
        }
        itemClickListener.updateSelectedCount(selectedItems.size())
        notifyItemChanged(position)
    }

    fun clearSelections() {
        selectedItems.clear()
        notifyDataSetChanged()
    }

    fun getSelectedItems() {
        val items = arrayListOf<RencanaVisitModel>()
        for (i in 0 until selectedItems.size()) {
            items.add(listItem[selectedItems.keyAt(i)])
        }
        callback?.invoke(items)
    }
}