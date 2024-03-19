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
import androidx.recyclerview.widget.RecyclerView
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.utils.DateFormat
import com.topmortar.topmortarsales.model.SuratJalanModel

class SuratJalanRecyclerViewAdapter(private val itemClickListener: ItemClickListener) : RecyclerView.Adapter<SuratJalanRecyclerViewAdapter.ViewHolder>() {
    private var listItem: ArrayList<SuratJalanModel> = ArrayList()
    private var context: Context? = null

    interface ItemClickListener {
        fun onItemClick(data: SuratJalanModel? = null)
    }

    fun setListItem(listItem: ArrayList<SuratJalanModel>) {
        this.listItem = listItem
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val ivProfile: ImageView = itemView.findViewById(R.id.iv_contact_profile)
        private val ivUpload: ImageView = itemView.findViewById(R.id.iv_upload)
        private val ivPrinter: ImageView = itemView.findViewById(R.id.iv_printer)
        private val tvContactName: TextView = itemView.findViewById(R.id.tv_contact_name)
        private val tvPhoneNumber: TextView = itemView.findViewById(R.id.tv_phone_number)

        fun bind(item: SuratJalanModel) {

//            ivUpload.visibility = View.VISIBLE
//            ivPrinter.visibility = View.VISIBLE

            ivProfile.setImageResource(R.drawable.file_list_red)
            tvContactName.text = item.no_surat_jalan
//            tvPhoneNumber.text = "${ item.order_number } - ${ DateFormat.format(item.delivery_date) }"
            tvPhoneNumber.text = DateFormat.format(dateString = item.dalivery_date, input = "yyyy-MM-dd HH:mm:ss", format = "dd MMMM yyyy HH.mm")

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_room, parent, false)
        context = parent.context
        return ViewHolder(view)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = listItem[position]

        holder.bind(item)
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

                Handler(Looper.getMainLooper()).postDelayed({
                    overlayView.alpha = 0f
                    overlayView.visibility = View.GONE
                }, animateDuration)

                val data = listItem[position]
                itemClickListener.onItemClick(data)

            }

        }

    }

    override fun getItemCount(): Int = listItem.size
}