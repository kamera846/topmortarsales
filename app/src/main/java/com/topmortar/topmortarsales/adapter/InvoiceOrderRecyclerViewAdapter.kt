package com.topmortar.topmortarsales.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.model.DetailSuratJalanModel

class InvoiceOrderRecyclerViewAdapter() : RecyclerView.Adapter<InvoiceOrderRecyclerViewAdapter.ViewHolder>() {
    private var listItem: ArrayList<DetailSuratJalanModel> = ArrayList()
    private var context: Context? = null

    fun setListItem(listItem: ArrayList<DetailSuratJalanModel>) {
        this.listItem = listItem
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvProductId: TextView = itemView.findViewById(R.id.tv_product_id)
        private val tvQtyNumber: TextView = itemView.findViewById(R.id.tv_qty_number)
        private val tvProductName: TextView = itemView.findViewById(R.id.tv_product_name)
        private val tvProductSerialNumber: TextView = itemView.findViewById(R.id.tv_product_serial_number)
        private val tvVoucher: TextView = itemView.findViewById(R.id.tv_voucher)

        fun bind(item: DetailSuratJalanModel) {

            tvProductId.text = item.id_produk
            tvQtyNumber.text = "[${item.qty_produk}]"
            tvProductName.text = item.nama_produk

            if (item.is_bonus == "1") {
                tvProductSerialNumber.text = context?.getString(R.string.free)
                tvProductSerialNumber.visibility = View.VISIBLE
            } else if (item.is_bonus == "2") {
                tvProductSerialNumber.text = context?.getString(R.string.free)
                tvProductSerialNumber.visibility = View.VISIBLE
            }

            if (!item.no_voucher.isNullOrEmpty()) {
                tvVoucher.text = "Nomor voucher: " + item.no_voucher
                tvVoucher.visibility = View.VISIBLE
            } else tvVoucher.visibility = View.GONE

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_invoice_order, parent, false)
        context = parent.context
        return ViewHolder(view)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = listItem[position]

        holder.bind(item)
        holder.itemView.startAnimation(AnimationUtils.loadAnimation(holder.itemView.context, R.anim.rv_item_fade_slide_up))

        if (position != RecyclerView.NO_POSITION) {
            val animateDuration = 200L

            val fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in)
            fadeIn.duration = animateDuration

//            val overlayView = holder.itemView.findViewById<LinearLayout>(R.id.overlay_view)
//
//            overlayView.alpha = 0.7f
//            overlayView.visibility = View.VISIBLE
//            overlayView.startAnimation(fadeIn)
//
//            Handler(Looper.getMainLooper()).postDelayed({
//                overlayView.alpha = 0f
//                overlayView.visibility = View.GONE
//            }, animateDuration)

        }

    }

    override fun getItemCount(): Int = listItem.size
}