package com.topmortar.topmortarsales.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat.getColor
import androidx.recyclerview.widget.RecyclerView
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.model.HobbyModel

class HobbyAdapter(
    private val hobbies: List<HobbyModel>,
    private val onSelectionChanged: () -> Unit
) : RecyclerView.Adapter<HobbyAdapter.ViewHolder>() {

    class ViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {

        val txtCategory: TextView =
            view.findViewById(R.id.txtCategory)

        val txtPath: TextView =
            view.findViewById(R.id.txtPath)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.item_hobby,
                parent,
                false
            )

        return ViewHolder(view)
    }

    override fun getItemCount() = hobbies.size

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        val hobby = hobbies[position]

        holder.txtCategory.text = hobby.name_hobi
        holder.txtPath.text = hobby.label

        setBackgroundColor(hobby.isSelected, holder.itemView)

        holder.itemView.setOnClickListener {
            hobby.isSelected = !hobby.isSelected
            onSelectionChanged()
            notifyItemChanged(position)
        }

    }

    private fun setBackgroundColor(isSelected: Boolean = false, itemView: View) {
        if (isSelected) {
            itemView.setBackgroundColor(getColor(itemView.context, R.color.primary15))
        } else {
            itemView.setBackgroundColor(getColor(itemView.context, R.color.baseBackground))
        }
    }
}