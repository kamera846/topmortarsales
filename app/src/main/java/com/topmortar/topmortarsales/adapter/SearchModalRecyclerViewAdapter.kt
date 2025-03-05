package com.topmortar.topmortarsales.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.model.ModalSearchModel

class SearchModalRecyclerViewAdapter(private val items: ArrayList<ModalSearchModel>, private val itemClickListener: ItemClickListener): RecyclerView.Adapter<SearchModalRecyclerViewAdapter.ViewHolder>() {
    private var filteredItemList: ArrayList<ModalSearchModel> = ArrayList(items)
    private var context: Context? = null
    var withEtc: Boolean = false

    interface ItemClickListener {
        fun onItemClick(data: ModalSearchModel? = null)
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        private val title = itemView.findViewById<TextView>(R.id.title)
        private val etc = itemView.findViewById<TextView>(R.id.etcText)
        private val etcOverlay = itemView.findViewById<TextView>(R.id.etcTextOverlay)
        fun bind(item: ModalSearchModel) {
            title.text = item.title
            etc.text = item.etc
            if (withEtc) {
                etcOverlay.visibility = View.INVISIBLE
                etc.visibility = View.VISIBLE
            } else {
                etc.visibility = View.GONE
            }
        }

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SearchModalRecyclerViewAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_search_modal, parent, false)
        context = parent.context
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: SearchModalRecyclerViewAdapter.ViewHolder,
        position: Int
    ) {
        val item = filteredItemList[position]
        holder.bind(item)
        holder.itemView.startAnimation(AnimationUtils.loadAnimation(holder.itemView.context, R.anim.rv_item_fade_slide_up))
        holder.itemView.setOnClickListener {
            val animateDuration = 100L

            val fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in)
            fadeIn.duration = animateDuration

            val overlayView = holder.itemView.findViewById<LinearLayout>(R.id.overlay_view)

            overlayView.alpha = 0.3f
            overlayView.visibility = View.VISIBLE
            overlayView.startAnimation(fadeIn)

            Handler(Looper.getMainLooper()).postDelayed({
                overlayView.alpha = 0f
                overlayView.visibility = View.GONE
                val data = filteredItemList[position]
                itemClickListener.onItemClick(data)
            }, animateDuration)
        }
    }

    override fun getItemCount(): Int = filteredItemList.size

    @SuppressLint("NotifyDataSetChanged")
    fun filter(filterText: String) {
        filteredItemList.clear()

        if (filterText.isBlank()) {
            filteredItemList.addAll(items) // Display the original list when the filter text is empty or blank
        } else {
            for (item in items) {
                if (item.title!!.contains(filterText, ignoreCase = true)) {
                    filteredItemList.add(item)
                }
            }
        }

        notifyDataSetChanged()
    }
}