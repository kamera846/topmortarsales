package com.topmortar.topmortarsales.adapter.recyclerview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.RadioButton
import androidx.recyclerview.widget.RecyclerView
import com.topmortar.topmortarsales.databinding.ItemQnaFormReportBinding

class QnAFormReportRVA(private val items: List<String>) : RecyclerView.Adapter<QnAFormReportRVA.MyViewHolder>() {
    private lateinit var context: Context

    inner class MyViewHolder(val binding: ItemQnaFormReportBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemQnaFormReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = parent.context
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        when (position) {
            0 -> {
                holder.binding.textCard.visibility = View.VISIBLE
                holder.binding.textQuestion.text = items[position]
            }
            1 -> {
                holder.binding.dateCard.visibility = View.VISIBLE
                holder.binding.dateQuestion.text = items[position]
            }
            2 -> {
                holder.binding.radioCard.visibility = View.VISIBLE
                holder.binding.radioQuestion.text = items[position]

                val genders = listOf("Laki-laki", "Perempuan")
                genders.forEach { gender ->
                    val radioButton = RadioButton(context).apply {
                        text = gender
                    }
                    holder.binding.radioGroupContainer.addView(radioButton)
                }
            }
            3 -> {
                holder.binding.checkboxCard.visibility = View.VISIBLE
                holder.binding.submitReport.visibility = View.VISIBLE
                holder.binding.checkboxQuestion.text = items[position]

                val hobbies = listOf("Olahraga", "Memasak", "Membaca", "Menggambar", "Lainnya...")
                hobbies.forEach { hobby ->
                    val checkBox = CheckBox(context).apply {
                        text = hobby
                    }
                    holder.binding.checkboxContainer.addView(checkBox)
                }
            }
        }
    }

    override fun getItemCount() = items.size
}
