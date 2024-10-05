package com.topmortar.topmortarsales.adapter.recyclerview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.RadioButton
import androidx.recyclerview.widget.RecyclerView
import com.topmortar.topmortarsales.commons.utils.convertDpToPx
import com.topmortar.topmortarsales.databinding.ItemQnaFormReportBinding
import com.topmortar.topmortarsales.model.QnAFormReportModel

class QnAFormReportRVA : RecyclerView.Adapter<QnAFormReportRVA.MyViewHolder>() {
    private lateinit var context: Context
    var items = arrayListOf<QnAFormReportModel>()

    inner class MyViewHolder(val binding: ItemQnaFormReportBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding =
            ItemQnaFormReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = parent.context
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        if (position == 0) {
            val layoutParams = holder.itemView.layoutParams as RecyclerView.LayoutParams
            layoutParams.topMargin = convertDpToPx(8, context)
            holder.itemView.layoutParams = layoutParams
        }

        val binding = holder.binding
        val data = items[position]
        when (data.answer_type) {
            "text" -> {
                if (data.is_required == "1") binding.textRequired.visibility = View.VISIBLE
                else binding.textRequired.visibility = View.GONE
                binding.textCard.visibility = View.VISIBLE
                binding.textQuestion.text = data.question
            }

            "date" -> {
                if (data.is_required == "1") binding.dateRequired.visibility = View.VISIBLE
                else binding.dateRequired.visibility = View.GONE
                binding.dateCard.visibility = View.VISIBLE
                binding.dateQuestion.text = data.question
            }

            "radio" -> {
                if (data.is_required == "1") binding.radioRequired.visibility = View.VISIBLE
                else binding.radioRequired.visibility = View.GONE
                binding.radioCard.visibility = View.VISIBLE
                binding.radioQuestion.text = data.question
                binding.checkboxContainer.removeAllViews()

                val options = data.answer_option
                options?.let {
                    it.forEach { item ->
                        val radioButton = RadioButton(context).apply {
                            text = item
                        }
                        binding.radioGroupContainer.addView(radioButton)
                    }
                }
            }

            "checkbox" -> {
                if (data.is_required == "1") binding.checkboxRequired.visibility = View.VISIBLE
                else binding.checkboxRequired.visibility = View.GONE
                binding.checkboxCard.visibility = View.VISIBLE
                binding.checkboxQuestion.text = data.question
                binding.checkboxContainer.removeAllViews()

                val options = data.answer_option
                options?.let {
                    it.forEach { item ->
                        val checkBox = CheckBox(context).apply {
                            text = item
                        }
                        binding.checkboxContainer.addView(checkBox)
                    }
                }
            }
        }
    }

    override fun getItemCount() = items.size
}
