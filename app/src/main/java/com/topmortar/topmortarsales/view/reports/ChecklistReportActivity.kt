package com.topmortar.topmortarsales.view.reports

import android.os.Bundle
import android.widget.CheckBox
import android.widget.RadioButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.topmortar.topmortarsales.adapter.recyclerview.QnAFormReportRVA
import com.topmortar.topmortarsales.databinding.ActivityChecklistReportBinding

class ChecklistReportActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChecklistReportBinding

    private val genders = listOf("Laki-laki", "Perempuan")
    private val hobbies = listOf("Olahraga", "Memasak", "Membaca", "Menggambar", "Lainnya...")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        supportActionBar?.hide()
        binding = ActivityChecklistReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.titleBar.icBack.setOnClickListener { finish() }
        binding.titleBar.tvTitleBar.text = "Form Laporan"

        setupRecyclerView()

//        setupRadioButton()
//        setupCheckbox()
//
//        binding.submitReport.setOnClickListener { submitReport() }
    }

    private fun setupRecyclerView() {
        val rvAdapter = QnAFormReportRVA(arrayListOf("Siapa nama kamu?", "Kapan tanggal lahir kamu?", "Apa jenis kelamin kamu?", "Apa saja hobi kamu?"))
        binding.recyclerView.apply {
            adapter = rvAdapter
            layoutManager = LinearLayoutManager(this@ChecklistReportActivity)
        }
    }

    private fun setupRadioButton() {
        genders.forEach { gender ->
            val radioButton = RadioButton(this).apply {
                text = gender
            }
            binding.radioGroupContainer.addView(radioButton)
        }
    }

    private fun setupCheckbox() {
        hobbies.forEach { hobby ->
            val checkBox = CheckBox(this).apply {
                text = hobby
            }
            binding.checkboxContainer.addView(checkBox)
        }
    }

    private fun submitReport() {

        val name = binding.editTextAnswer.text.toString()

        val datePicker = binding.datePickerAnswer
        val day = datePicker.dayOfMonth
        val month = datePicker.month + 1
        val year = datePicker.year

        val selectedDate = "$day/$month/$year"

        val selectedGender = binding.radioGroupContainer.checkedRadioButtonId
        val selectedGenderText = if (selectedGender != -1) {
            findViewById<RadioButton>(selectedGender).text
        } else {
            "Belum dipilih"
        }

        val selectedHobbies = mutableListOf<String>()
        for (i in 0 until binding.checkboxContainer.childCount) {
            val checkBox = binding.checkboxContainer.getChildAt(i) as CheckBox
            if (checkBox.isChecked) {
                selectedHobbies.add(checkBox.text.toString())
            }
        }

        println("Name: $name")
        println("Birthday: $selectedDate")
        println("Gender: $selectedGenderText")
        println("Hobbies: $selectedHobbies")
    }

}