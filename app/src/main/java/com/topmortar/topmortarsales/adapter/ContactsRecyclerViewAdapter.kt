package com.topmortar.topmortarsales.adapter

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.TooltipCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getDrawable
import androidx.recyclerview.widget.RecyclerView
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.STATUS_CONTACT_ACTIVE
import com.topmortar.topmortarsales.commons.STATUS_CONTACT_BID
import com.topmortar.topmortarsales.commons.STATUS_CONTACT_BLACKLIST
import com.topmortar.topmortarsales.commons.STATUS_CONTACT_DATA
import com.topmortar.topmortarsales.commons.STATUS_CONTACT_PASSIVE
import com.topmortar.topmortarsales.model.ContactModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ContactsRecyclerViewAdapter(private val chatList: ArrayList<ContactModel>, private val itemClickListener: ItemClickListener) : RecyclerView.Adapter<ContactsRecyclerViewAdapter.ChatViewHolder>() {

    var callback: ((ArrayList<ContactModel>) -> Unit)? = null

    private var context: Context? = null
    private var isSelectedItemActive = false
//    private var selectedItems = SparseBooleanArray()
    private var selectedItemsId = mutableSetOf<String>()

    interface ItemClickListener {
        fun onItemClick(data: ContactModel? = null)
        fun updateSelectedItem(count: Int = 0, selectedItemsId: MutableSet<String> = mutableSetOf()) {

        }
    }

    fun setSelectItemState(state: Boolean) {
        this.isSelectedItemActive = state
    }

    fun setSelectedItemsId(items: MutableSet<String>) {
        this.selectedItemsId = items
    }

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val tvContactName: TextView = itemView.findViewById(R.id.tv_contact_name)
        val tvPhoneNumber: TextView = itemView.findViewById(R.id.tv_phone_number)
        val tooltipStatus: ImageView = itemView.findViewById(R.id.tooltip_status)
        private val icCake: ImageView = itemView.findViewById(R.id.icCake)
        private val imgProfile: ImageView = itemView.findViewById(R.id.iv_contact_profile)
        val checkBoxItem: CheckBox = itemView.findViewById(R.id.checkbox)

        fun bind(chatItem: ContactModel) {

            if (chatItem.is_birthday == "1") icCake.visibility = View.VISIBLE
            else icCake.visibility = View.GONE
            tvContactName.text = chatItem.nama
            tvPhoneNumber.text = chatItem.deliveryStatus.ifEmpty {
                if (chatItem.nomorhp.isNotEmpty()) "+${chatItem.nomorhp}" else chatItem.created_at
            }
            setupStatus(chatItem.store_status)

            if (isSelectedItemActive) {
                imgProfile.visibility = View.GONE
                checkBoxItem.visibility = View.VISIBLE
            } else {
                imgProfile.visibility = View.VISIBLE
                checkBoxItem.visibility = View.GONE
            }

//            checkBoxItem.isChecked = selectedItems.get(position, false)
            checkBoxItem.isChecked = selectedItemsId.contains(chatItem.id_contact)
            itemView.setBackgroundColor(
//                if (selectedItems.get(position, false)) ContextCompat.getColor(itemView.context, R.color.primary15)
                if (selectedItemsId.contains(chatItem.id_contact)) ContextCompat.getColor(itemView.context, R.color.primary15)
                else ContextCompat.getColor(itemView.context, R.color.baseBackground)
            )
        }

        private fun setupStatus(status: String? = null) {
            tooltipStatus.visibility = View.VISIBLE
            when (status) {
                STATUS_CONTACT_DATA -> {
                    tooltipStatus.setImageDrawable(context?.let { getDrawable(it, R.drawable.status_data) })
                    tooltipHandler(tooltipStatus, "Customer Status Data")
                }
                STATUS_CONTACT_PASSIVE -> {
                    tooltipStatus.setImageDrawable(context?.let { getDrawable(it, R.drawable.status_passive) })
                    tooltipHandler(tooltipStatus, "Customer Status Passive")
                }
                STATUS_CONTACT_ACTIVE -> {
                    tooltipStatus.setImageDrawable(context?.let { getDrawable(it, R.drawable.status_active) })
                    tooltipHandler(tooltipStatus, "Customer Status Active")
                }
                STATUS_CONTACT_BLACKLIST -> {
                    tooltipStatus.setImageDrawable(context?.let { getDrawable(it, R.drawable.status_blacklist) })
                    tooltipHandler(tooltipStatus, "Customer Status Blacklist")
                }
                STATUS_CONTACT_BID -> {
                    tooltipStatus.setImageDrawable(context?.let { getDrawable(it, R.drawable.status_bid) })
                    tooltipHandler(tooltipStatus, "Customer Status Bargained")
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

        val dateNow = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val dateBirthday = chatItem.tgl_lahir

        if (dateNow == dateBirthday) chatItem.is_birthday = "1"

        holder.bind(chatItem)
        holder.itemView.startAnimation(AnimationUtils.loadAnimation(holder.itemView.context, R.anim.rv_item_fade_slide_up))

        holder.itemView.setOnClickListener { if (isSelectedItemActive) toggleSelection(holder, position) else onItemClick(holder, position) }
        holder.tooltipStatus.setOnClickListener { onItemClick(holder, position) }
        holder.checkBoxItem.setOnClickListener { toggleSelection(holder, position) }

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

    private fun toggleSelection(holder: ContactsRecyclerViewAdapter.ChatViewHolder, position: Int) {
//        if (selectedItems.get(position, false)) {
//            selectedItems.delete(position)
//            holder.checkBoxItem.isChecked = false
//        } else {
//            selectedItems.put(position, true)
//            holder.checkBoxItem.isChecked = true
//        }
        val item = chatList[position]
        if (selectedItemsId.contains(item.id_contact)) {
            selectedItemsId.remove(item.id_contact)
            holder.checkBoxItem.isChecked = false
        } else {
            selectedItemsId.add(item.id_contact)
            holder.checkBoxItem.isChecked = true
        }
        itemClickListener.updateSelectedItem(selectedItemsId.size, selectedItemsId)
        notifyItemChanged(position)
    }

    fun clearSelections() {
        val selectedPositions = selectedItemsId.toMutableSet() // Clone untuk menghindari ConcurrentModificationException
        selectedItemsId.clear()
        for ((position, _) in selectedPositions.withIndex() ) {
            notifyItemChanged(position)
        }
    }


    fun getSelectedItems() {
//        val items = arrayListOf<ContactModel>()
//        for (i in 0 until selectedItems.size()) {
//            items.add(chatList[selectedItems.keyAt(i)])
//        }
//        callback?.invoke(items)
        callback?.invoke(
            chatList.filter {
                selectedItemsId.contains(it.id_contact)
            }.toCollection(ArrayList())
        )
    }
}