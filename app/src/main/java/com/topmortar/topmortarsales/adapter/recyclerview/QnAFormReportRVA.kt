package com.topmortar.topmortarsales.adapter.recyclerview

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
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
        val incrementNumber = "${position + 1}. "

        when (data.answer_type) {
            "text" -> {
                if (data.is_required == "1") binding.textRequired.visibility = View.VISIBLE
                else binding.textRequired.visibility = View.GONE
                binding.textCard.visibility = View.VISIBLE
                binding.textQuestion.text = incrementNumber + data.question
            }

            "date" -> {
                if (data.is_required == "1") binding.dateRequired.visibility = View.VISIBLE
                else binding.dateRequired.visibility = View.GONE
                binding.dateCard.visibility = View.VISIBLE
                binding.dateQuestion.text = incrementNumber + data.question
            }

            "radio" -> {
                if (data.is_required == "1") binding.radioRequired.visibility = View.VISIBLE
                else binding.radioRequired.visibility = View.GONE
                binding.radioCard.visibility = View.VISIBLE
                binding.radioQuestion.text = incrementNumber + data.question
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
                binding.checkboxQuestion.text = incrementNumber + data.question
                binding.checkboxContainer.removeAllViews()

                val options = data.answer_option
                options?.let {
                    data.selected_answer = MutableList(it.size) { false }
                    it.forEachIndexed { index, item ->
                        val checkBox = setupCheckbox(index, item, data)
                        binding.checkboxContainer.addView(checkBox)
                    }
                    val editText = setupEditText(data)
                    binding.checkboxContainer.addView(editText)
                }
            }
        }
    }

    override fun getItemCount() = items.size

    fun submitForm(): ArrayList<QnAFormReportModel> = items

    private fun setupCheckbox(index: Int, item: String, data: QnAFormReportModel): CheckBox {
        return CheckBox(context).apply {
            text = item
            data.selected_answer?.let { selected ->
                isChecked = selected[index]
                setOnCheckedChangeListener {_, isChecked ->
                    data.selected_answer!![index] = isChecked
                }
            }
        }
    }

    private fun setupEditText(data: QnAFormReportModel): EditText {
        return EditText(context).apply {
            textSize = 14f
            inputType = android.text.InputType.TYPE_TEXT_VARIATION_NORMAL
            setText(data.keterangan)
            hint = "Jawaban Anda"
            addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {}

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    data.keterangan = s.toString()
                }

                override fun afterTextChanged(s: Editable?) {}

            })
        }
    }
}
