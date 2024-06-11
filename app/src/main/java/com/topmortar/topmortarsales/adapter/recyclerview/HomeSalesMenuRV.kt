package com.topmortar.topmortarsales.adapter.recyclerview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.databinding.ItemMenuHomeSalesBinding
import com.topmortar.topmortarsales.model.HomeMenuSalesModel

class HomeSalesMenuRV : RecyclerView.Adapter<HomeSalesMenuRV.ViewHolder>() {

    private var listItem: ArrayList<HomeMenuSalesModel> = ArrayList()
    private var context: Context? = null

    inner class ViewHolder(private val binding: ItemMenuHomeSalesBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind (item: HomeMenuSalesModel, position: Int) {
            binding.itemIcon.setBackgroundResource(item.bgColor!!)
            binding.itemIcon.setImageResource(item.icon!!)
            binding.itemTitle.text = item.title

            binding.itemContainer.alpha = if (item.isLocked) 0.5f else 1f
            binding.itemChevron.setImageResource(if (item.isLocked) R.drawable.lock_dark else R.drawable.chevron_right_dark)
            if (position == listItem.size - 1) binding.itemLine.visibility = View.GONE

            println("Item status is ${item.isLocked}")
            binding.itemContainer.setOnClickListener {
                listener?.onItemClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMenuHomeSalesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = parent.context
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = listItem.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listItem[position]
        holder.bind(item, position)
    }

    interface OnItemClickListener {
        fun onItemClick(item: HomeMenuSalesModel)
    }
    private var listener: OnItemClickListener? = null
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }
    fun setList(data: ArrayList<HomeMenuSalesModel>) {
        listItem = data
    }

}