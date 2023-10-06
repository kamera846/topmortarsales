package com.topmortar.topmortarsales.adapter.recyclerview

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.topmortar.topmortarsales.databinding.ItemReportsBinding
import com.topmortar.topmortarsales.model.ReportModel

class ReportsRecyclerViewAdapter() : RecyclerView.Adapter<ReportsRecyclerViewAdapter.ViewHolder>() {
    private var listItem: ArrayList<ReportModel> = ArrayList()
    private var context: Context? = null

    fun setList(data: ArrayList<ReportModel>) {
        listItem = data
    }

    inner class ViewHolder(private val binding: ItemReportsBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ReportModel) {
            binding.title.text = item.title
            binding.description.text = item.description
            binding.date.text = item.date
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding = ItemReportsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = parent.context
        return ViewHolder(binding)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listItem[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = listItem.size
}