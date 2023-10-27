package com.topmortar.topmortarsales.adapter.recyclerview

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.utils.DateFormat
import com.topmortar.topmortarsales.commons.utils.convertDpToPx
import com.topmortar.topmortarsales.databinding.ItemReportsBinding
import com.topmortar.topmortarsales.model.ReportVisitModel

class ReportsRecyclerViewAdapter() : RecyclerView.Adapter<ReportsRecyclerViewAdapter.ViewHolder>() {
    private var listItem: ArrayList<ReportVisitModel> = ArrayList()
    private var context: Context? = null

    fun setList(data: ArrayList<ReportVisitModel>) {
        listItem = data
    }

    inner class ViewHolder(private val binding: ItemReportsBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ReportVisitModel, position: Int) {
            binding.title.text = item.distance_visit + " km dari titik toko"
            binding.description.text = item.laporan_visit
            binding.date.text = DateFormat.format(item.date_visit, "yyyy-MM-dd HH:mm:ss", "dd MMMM yyyy, HH:mm")

            val layoutParams = binding.root.layoutParams as ViewGroup.MarginLayoutParams
            if (position == 0 || position == 1) layoutParams.topMargin = convertDpToPx(16, context as Activity)
            if (position == listItem.size - 1 || position == listItem.size - 2) layoutParams.bottomMargin = convertDpToPx(16, context as Activity)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding = ItemReportsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = parent.context
        return ViewHolder(binding)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listItem[position]
        holder.bind(item, position)
    }

    override fun getItemCount(): Int = listItem.size
}