package com.topmortar.topmortarsales.adapter.recyclerview

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.USER_KIND_COURIER
import com.topmortar.topmortarsales.commons.utils.DateFormat
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.convertDpToPx
import com.topmortar.topmortarsales.databinding.ItemReportsBinding
import com.topmortar.topmortarsales.model.ReportVisitModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ReportsRecyclerViewAdapter : RecyclerView.Adapter<ReportsRecyclerViewAdapter.ViewHolder>() {
    private var listItem: ArrayList<ReportVisitModel> = ArrayList()
    private var context: Context? = null
    private var withName: Boolean? = null
    private var isCourier = false

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
    fun setIsCourier(data: Boolean) {
        isCourier = data
    }
    fun setWithName(withName: Boolean?) {
        this.withName = withName
    }

    inner class ViewHolder(private val binding: ItemReportsBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ReportVisitModel, position: Int) {
            binding.description.text = item.laporan_visit
            if (item.is_approved == "1") binding.icStatus.setImageDrawable(context!!.getDrawable(R.drawable.checkbox_circle_green))

            val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(item.date_visit)
            val dateFormat = if (date != null) {
                val calendar = Calendar.getInstance()
                val currentYear = calendar.get(Calendar.YEAR)
                val dateYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(date)

                if (currentYear == dateYear.toInt()) DateFormat.format(item.date_visit, "yyyy-MM-dd HH:mm:ss", "dd MMM, HH:mm")
                else DateFormat.format(item.date_visit, "yyyy-MM-dd HH:mm:ss", "dd MMM yyyy, HH:mm")
            } else DateFormat.format(item.date_visit, "yyyy-MM-dd HH:mm:ss", "dd MMM, HH:mm")

            val distanceFormat = item.distance_visit
            var stringDistance = "%.2f".format(distanceFormat.toDouble())
            if (stringDistance.contains(",")) stringDistance = stringDistance.replace(",", ".")

            binding.title.text = if (withName == true){
                if (isCourier) item.nama_gudang else item.nama
            } else "$stringDistance km dari titik"
            binding.date.text = if (withName == true) "$stringDistance km\n$dateFormat" else dateFormat

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