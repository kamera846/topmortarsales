package com.topmortar.topmortarsales.adapter.recyclerview

import android.app.DatePickerDialog
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
import com.topmortar.topmortarsales.commons.utils.DateFormat
import com.topmortar.topmortarsales.databinding.ItemQnaFormReportBinding
import com.topmortar.topmortarsales.model.QnAFormReportModel
import java.util.Calendar

class QnAFormReportRVA : RecyclerView.Adapter<QnAFormReportRVA.MyViewHolder>() {
    private lateinit var context: Context
    var items = arrayListOf<QnAFormReportModel>()
    var isAnswerChecklist: Boolean? = null

    inner class MyViewHolder(val binding: ItemQnaFormReportBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding =
            ItemQnaFormReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = parent.context
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val binding = holder.binding
        val data = items[position]
        val incrementNumber = "${position + 1}. "

        if (isAnswerChecklist != null && isAnswerChecklist == true) {
            binding.answerCard.visibility = View.VISIBLE
            binding.textAnswerQuestion.text = incrementNumber + data.text_question
            if (data.answers.isNotEmpty()) {
                if (data.answers[0].isEmpty()) binding.textUserAnswer.text = "Tidak dijawab"
                else binding.textUserAnswer.text = data.answers.joinToString(separator = ", ")
            } else binding.textUserAnswer.text = "Tidak dijawab"
            return
        }

        when (data.answer_type) {
            "text" -> {
                if (data.is_required == "1") binding.textRequired.visibility = View.VISIBLE
                else binding.textRequired.visibility = View.GONE
                binding.textCard.visibility = View.VISIBLE
                binding.textQuestion.text = incrementNumber + data.text_question
                binding.textAnswer.hint = data.placeholder
                binding.textAnswer.addTextChangedListener(object: TextWatcher {
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
                        data.text_answer = s.toString()
                    }

                    override fun afterTextChanged(s: Editable?) {}

                })
            }

            "date" -> {
                if (data.is_required == "1") binding.dateRequired.visibility = View.VISIBLE
                else binding.dateRequired.visibility = View.GONE
                binding.dateCard.visibility = View.VISIBLE
                binding.dateQuestion.text = incrementNumber + data.text_question

                val datePicker = setupDatePicker(binding.dateInput, data)
                binding.dateIcon.setOnClickListener { datePicker.show() }
                binding.dateInput.setOnClickListener { datePicker.show() }
                binding.dateInput.setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) {
                        datePicker.show()
                        binding.dateInput.setSelection(binding.dateInput.length())
                    } else binding.dateInput.clearFocus()
                }
            }

            "radio" -> {
                if (data.is_required == "1") binding.radioRequired.visibility = View.VISIBLE
                else binding.radioRequired.visibility = View.GONE
                binding.radioCard.visibility = View.VISIBLE
                binding.radioQuestion.text = incrementNumber + data.text_question
                binding.checkboxContainer.removeAllViews()

                val options = data.answer_option
                options?.let {
                    it.forEach { item ->
                        val radioButton = RadioButton(context).apply {
                            text = item
                            setOnCheckedChangeListener { _, checked ->
                                isChecked = checked
                                if (checked) data.text_answer = item
                            }
                        }
                        binding.radioGroupContainer.addView(radioButton)
                    }
                }
            }

            "checkbox" -> {
                if (data.is_required == "1") binding.checkboxRequired.visibility = View.VISIBLE
                else binding.checkboxRequired.visibility = View.GONE
                binding.checkboxCard.visibility = View.VISIBLE
                binding.checkboxQuestion.text = incrementNumber + data.text_question
                binding.checkboxContainer.removeAllViews()

                val options = data.answer_option
                options?.let {
                    data.selected_answer = arrayListOf()
                    it.forEach { item ->
                        val checkBox = setupCheckbox(item, data)
                        binding.checkboxContainer.addView(checkBox)
                    }
                }
            }
        }
    }

    override fun getItemCount() = items.size

    fun submitForm(): ArrayList<QnAFormReportModel> = items

    private fun setupCheckbox(item: String, data: QnAFormReportModel): CheckBox {
        return CheckBox(context).apply {
            text = item
            setOnCheckedChangeListener {_, isChecked ->
                if (isChecked) data.selected_answer!!.add(item)
                else data.selected_answer!!.remove(item)
            }
        }
    }

    private fun setupDatePicker(view: EditText, data: QnAFormReportModel): DatePickerDialog {
        val selectedDate: Calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            context,
            { _, year, month, day ->
                selectedDate.set(Calendar.YEAR, year)
                selectedDate.set(Calendar.MONTH, month)
                selectedDate.set(Calendar.DAY_OF_MONTH, day)

                // Do something with the selected date
                val formattedDate = DateFormat.format(selectedDate)
                data.text_answer = formattedDate
                view.setText(formattedDate)
                view.clearFocus()
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        )

        datePicker.setOnDismissListener { view.clearFocus() }

        return datePicker
    }
}
