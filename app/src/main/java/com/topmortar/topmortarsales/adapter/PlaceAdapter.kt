package com.topmortar.topmortarsales.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.topmortar.topmortarsales.R

class PlaceAdapter(private val places: List<String>, private val address: List<String>, private val onItemClick: (Int) -> Unit) : RecyclerView.Adapter<PlaceAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val placeNameTextView: TextView = itemView.findViewById(R.id.placeNameTextView)
        val placeAddressTextView: TextView = itemView.findViewById(R.id.placeAddressTextView)
//        val placeDistanceTextView: TextView = itemView.findViewById(R.id.placeDistanceTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_place, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.placeNameTextView.text = places[position]
        holder.placeAddressTextView.text = address[position]
//        holder.placeDistanceTextView.text = distance[position]
        holder.itemView.setOnClickListener {
            onItemClick(position)
        }
    }

    override fun getItemCount(): Int {
        return places.size
    }
}
