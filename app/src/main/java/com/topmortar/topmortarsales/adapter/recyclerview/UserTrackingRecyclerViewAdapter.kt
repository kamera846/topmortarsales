package com.topmortar.topmortarsales.adapter.recyclerview

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.utils.convertDpToPx
import com.topmortar.topmortarsales.databinding.ItemUserTrackingBinding
import com.topmortar.topmortarsales.model.UserAbsentModel

class UserTrackingRecyclerViewAdapter : RecyclerView.Adapter<UserTrackingRecyclerViewAdapter.ViewHolder>() {
    private var listItem: ArrayList<UserAbsentModel> = ArrayList()
    private var context: Context? = null
    private var withName: Boolean? = null
    private var isCourier = false

    interface OnItemClickListener {
        fun onItemClick(item: UserAbsentModel)
    }
    private var listener: OnItemClickListener? = null
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }
    fun setList(data: ArrayList<UserAbsentModel>) {
        listItem = data
    }
    fun setIsCourier(data: Boolean) {
        isCourier = data
    }
    fun setWithName(withName: Boolean?) {
        this.withName = withName
    }

    inner class ViewHolder(private val binding: ItemUserTrackingBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: UserAbsentModel, position: Int) {

            if (position == 0) itemView.setPadding(convertDpToPx(16, context!!),0,0,0)
            else if (position == (listItem.size - 1)) itemView.setPadding(0,0,convertDpToPx(16, context!!),0)

            binding.userName.text = item.fullname
            if (item.isOnline) {
                binding.userProfile.setBackgroundResource(R.drawable.bg_light_round_online)
            } else {
                binding.userProfile.setBackgroundResource(R.drawable.bg_light_round)
            }

            itemView.setOnClickListener {
                listener?.onItemClick(item)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding = ItemUserTrackingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = parent.context
        return ViewHolder(binding)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listItem[position]
        holder.bind(item, position)
    }

    override fun getItemCount(): Int = listItem.size
}