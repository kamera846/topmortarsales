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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ReportsRecyclerViewAdapter : RecyclerView.Adapter<ReportsRecyclerViewAdapter.ViewHolder>() {
    private var listItem: ArrayList<ReportVisitModel> = ArrayList()
    private var context: Context? = null

    interface OnItemClickListener {
        fun onItemClick(item: ReportVisitModel)
    }
    private var listener: OnItemClickListener? = null
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }
    fun setList(data: ArrayList<ReportVisitModel>) {
        listItem = data
    }

    inner class ViewHolder(private val binding: ItemReportsBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ReportVisitModel, position: Int) {
            binding.title.text = item.distance_visit + " km dari titik"
            binding.description.text = item.laporan_visit

            val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(item.date_visit)
            if (date != null) {
                val calendar = Calendar.getInstance()
                val currentYear = calendar.get(Calendar.YEAR)
                val dateYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(date)

                if (currentYear == dateYear.toInt()) binding.date.text = DateFormat.format(item.date_visit, "yyyy-MM-dd HH:mm:ss", "dd MMMM, HH:mm")
                else binding.date.text = DateFormat.format(item.date_visit, "yyyy-MM-dd HH:mm:ss", "dd MMMM yyyy, HH:mm")
            } else {
                binding.date.text = DateFormat.format(item.date_visit, "yyyy-MM-dd HH:mm:ss", "dd MMMM, HH:mm")
            }

            val layoutParams = binding.root.layoutParams as ViewGroup.MarginLayoutParams
            if (position == 0 || position == 1) layoutParams.topMargin = convertDpToPx(16, context as Activity)
            if (position == listItem.size - 1 || position == listItem.size - 2) layoutParams.bottomMargin = convertDpToPx(16, context as Activity)

            itemView.setOnClickListener {
                listener?.onItemClick(item)
            }
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