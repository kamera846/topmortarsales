package com.topmortar.topmortarsales.adapter.recyclerview

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.utils.CustomUtility
import com.topmortar.topmortarsales.commons.utils.DateFormat
import com.topmortar.topmortarsales.databinding.ItemAllUserTrackingBinding
import com.topmortar.topmortarsales.model.UserAbsentModel

class AllUserTrackingRVA : RecyclerView.Adapter<AllUserTrackingRVA.ViewHolder>() {
    private var listItem: ArrayList<UserAbsentModel> = ArrayList()
    private var context: Context? = null

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

    inner class ViewHolder(private val binding: ItemAllUserTrackingBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: UserAbsentModel) {

            binding.tvName.text = item.fullname
            binding.tvDescription.text = "Terakhir dilacak " + DateFormat.format("${item.lastTracking}", "yyyy-MM-dd HH:mm:ss", "dd MMM yyyy, HH.mm")
            binding.initialName.text = CustomUtility(context!!).getInitials(item.fullname)

            if (item.isOnline) {
                binding.userProfile.setBackgroundResource(R.drawable.bg_light_round_online)
            } else {
                binding.userProfile.setBackgroundResource(R.drawable.bg_light_round)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding = ItemAllUserTrackingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = parent.context
        return ViewHolder(binding)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listItem[position]
        holder.bind(item)
        holder.itemView.setOnClickListener {
            listener?.onItemClick(item)
        }
    }

    override fun getItemCount(): Int = listItem.size
}