package com.topmortar.topmortarsales.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.utils.CurrencyFormat
import com.topmortar.topmortarsales.commons.utils.DateFormat
import com.topmortar.topmortarsales.model.InvoicePaymentModel

class InvoicePaymentRecyclerViewAdapter: RecyclerView.Adapter<InvoicePaymentRecyclerViewAdapter.ViewHolder>() {
    private var listItem: ArrayList<InvoicePaymentModel> = ArrayList()
    private var context: Context? = null

    fun setListItem(listItem: ArrayList<InvoicePaymentModel>) {
        this.listItem = listItem
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvName: TextView = itemView.findViewById(R.id.tv_name)
        private val tvPrice: TextView = itemView.findViewById(R.id.tv_price)
        private val tvDescription: TextView = itemView.findViewById(R.id.tv_description)

        fun bind(item: InvoicePaymentModel) {

            tvName.text = DateFormat.format(dateString = item.date_payment, input = "yyyy-MM-dd HH:mm:ss", format = "EEEE, dd MMMM yyyy")
            tvPrice.text = CurrencyFormat.format(amount = item.amount_payment.toDouble())
//            tvDescription.text = item.debt.let { if (it != "0") "Remaining - ${CurrencyFormat.format(amount = it.toDouble())}" else "Paid"}
            tvDescription.text = DateFormat.format(dateString = item.date_payment, input = "yyyy-MM-dd HH:mm:ss", format = "HH:mm") + " WIB"

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_invoice_payment, parent, false)
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

        }

    }

    override fun getItemCount(): Int = listItem.size
}