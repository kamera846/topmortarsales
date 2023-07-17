package com.topmortar.topmortarsales.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.model.ModalSearchModel

class ListSearchModalRecyclerViewAdapter(private val items: ArrayList<ModalSearchModel>, private val itemClickListener: ItemClickListener): RecyclerView.Adapter<ListSearchModalRecyclerViewAdapter.ViewHolder>() {
    private var filteredItemList: ArrayList<ModalSearchModel> = ArrayList(items)

    interface ItemClickListener {
        fun onItemClick(data: ModalSearchModel? = null)
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        private val title = itemView.findViewById<TextView>(R.id.title)
        fun bind(item: ModalSearchModel) { title.text = item.title }

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ListSearchModalRecyclerViewAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_search_modal, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ListSearchModalRecyclerViewAdapter.ViewHolder,
        position: Int
    ) {
        val item = filteredItemList[position]
        holder.bind(item)
        holder.itemView.startAnimation(AnimationUtils.loadAnimation(holder.itemView.context, R.anim.rv_item_fade_slide_up))
        holder.itemView.setOnClickListener {
            if (position != RecyclerView.NO_POSITION) {
                val data = filteredItemList[position]
                itemClickListener.onItemClick(data)
            }
        }
    }

    override fun getItemCount(): Int = filteredItemList.size

    fun filter(filterText: String) {
        filteredItemList.clear()

        if (filterText.isBlank()) {
            filteredItemList.addAll(items) // Display the original list when the filter text is empty or blank
        } else {
            for (item in items) {
                if (item.title.contains(filterText, ignoreCase = true)) {
                    filteredItemList.add(item)
                    Log.d("SEARCH MODAL", "$filteredItemList")
                }
            }
        }

        notifyDataSetChanged()
    }
}