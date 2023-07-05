package com.topmortar.topmortarsales

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.topmortar.topmortarsales.commons.CONST_BIRTHDAY
import com.topmortar.topmortarsales.commons.CONST_CONTACT_ID
import com.topmortar.topmortarsales.commons.CONST_NAME
import com.topmortar.topmortarsales.commons.CONST_PHONE
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Suppress("DEPRECATION")
class DetailContactActivity : AppCompatActivity() {

    private lateinit var tvPhoneContainer: LinearLayout
    private lateinit var tvBirthdayContainer: LinearLayout
    private lateinit var etPhoneContainer: LinearLayout
    private lateinit var etBirthdayContainer: LinearLayout
    private lateinit var icBack: ImageView
    private lateinit var icEdit: ImageView
    private lateinit var tvTitleBar: TextView
    private lateinit var tvCancelEdit: TextView
    private lateinit var tvName: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvBirthday: TextView
    private lateinit var tvEditBirthday: TextView
    private lateinit var etName: EditText
    private lateinit var etPhone: EditText
    private lateinit var btnSendMessage: Button
    private lateinit var btnSaveEdit: Button

    private var contactId: String? = null
    private var isEdit: Boolean = false
    private var selectedDate: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        setContentView(R.layout.activity_detail_contact)

        initVariable()
        initClickHandler()
        dataActivityValidation()

    }

    private fun initVariable() {

        tvPhoneContainer = findViewById(R.id.tv_phone_container)
        tvBirthdayContainer = findViewById(R.id.tv_birthday_container)
        etPhoneContainer = findViewById(R.id.et_phone_container)
        etBirthdayContainer = findViewById(R.id.et_birthday_container)
        tvName = findViewById(R.id.tv_name)
        tvPhone = findViewById(R.id.tv_phone)
        icBack = findViewById(R.id.ic_back)
        icEdit = findViewById(R.id.ic_edit)
        tvTitleBar = findViewById(R.id.tv_title_bar)
        tvCancelEdit = findViewById(R.id.tv_cancel_edit)
        tvName = findViewById(R.id.tv_name)
        tvDescription = findViewById(R.id.tv_description)
        tvPhone = findViewById(R.id.tv_phone)
        tvBirthday = findViewById(R.id.tv_birthday)
        etName = findViewById(R.id.et_name)
        etPhone = findViewById(R.id.et_phone)
        tvEditBirthday = findViewById(R.id.tv_edit_birthday)
        btnSendMessage = findViewById(R.id.btn_send_message)
        btnSaveEdit = findViewById(R.id.btn_save_edit)

    }

    private fun initClickHandler() {

        icBack.setOnClickListener { finish() }
        icEdit.setOnClickListener { toggleEdit() }
        tvCancelEdit.setOnClickListener { toggleEdit() }
        btnSendMessage.setOnClickListener { navigateAddNewRoom() }
        btnSaveEdit.setOnClickListener { saveEdit() }
        etBirthdayContainer.setOnClickListener { showDatePickerDialog() }
        tvEditBirthday.setOnClickListener { showDatePickerDialog() }

    }

    private fun dataActivityValidation() {

        val intent = intent

        val iContactId = intent.getStringExtra(CONST_CONTACT_ID)
        val iPhone = intent.getStringExtra(CONST_PHONE)
        val iName = intent.getStringExtra(CONST_NAME)
        val iBirthday = intent.getStringExtra(CONST_BIRTHDAY)

        if (iContactId!!.isNotEmpty() ) {
            contactId = iContactId
        }
        if (iPhone!!.isNotEmpty() ) {
            tvPhone.text = "+$iPhone"
            etPhone.setText(iPhone)
        }
        if (iName!!.isNotEmpty() ) {
            tvName.text = iName
            etName.setText(iName)
        }
        if (iBirthday!!.isNotEmpty() ) {
            tvBirthday.text = formatDate(iBirthday)
            tvEditBirthday.text = formatDate(iBirthday)
        }

    }

    private fun toggleEdit() {

        isEdit = !isEdit

        if (isEdit) {

            tvName.visibility = View.GONE
//            tvPhoneContainer.visibility = View.GONE
            tvBirthdayContainer.visibility = View.GONE
            tvDescription.visibility = View.GONE
            icBack.visibility = View.GONE
            icEdit.visibility = View.GONE
            btnSendMessage.visibility = View.GONE

            tvCancelEdit.visibility = View.VISIBLE
            tvTitleBar.visibility = View.VISIBLE
            etName.visibility = View.VISIBLE
//            etPhoneContainer.visibility = View.VISIBLE
            etBirthdayContainer.visibility = View.VISIBLE
            btnSaveEdit.visibility = View.VISIBLE

            etName.requestFocus()

        } else {

            tvName.visibility = View.VISIBLE
//            tvPhoneContainer.visibility = View.VISIBLE
            tvBirthdayContainer.visibility = View.VISIBLE
            tvDescription.visibility = View.VISIBLE
            icBack.visibility = View.VISIBLE
            icEdit.visibility = View.VISIBLE
            btnSendMessage.visibility = View.VISIBLE

            tvCancelEdit.visibility = View.GONE
            tvTitleBar.visibility = View.GONE
            etName.visibility = View.GONE
//            etPhoneContainer.visibility = View.GONE
            etBirthdayContainer.visibility = View.GONE
            btnSaveEdit.visibility = View.GONE

        }

    }

    private fun saveEdit() {

        tvName.text = etName.text.toString()
        tvBirthday.text = tvEditBirthday.text.toString()

        toggleEdit()

    }

    private fun showDatePickerDialog() {
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, day ->
                selectedDate.set(Calendar.YEAR, year)
                selectedDate.set(Calendar.MONTH, month)
                selectedDate.set(Calendar.DAY_OF_MONTH, day)

                // Do something with the selected date
                val formattedDate = formatDate(selectedDate)
                tvEditBirthday.text = formattedDate
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        )

        datePicker.show()
    }

    private fun formatDate(calendar: Calendar): String {
        val format = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return format.format(calendar.time)
    }

    private fun formatDate(dateString: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())

        val date = inputFormat.parse(dateString)
        return outputFormat.format(date!!)
    }

    private fun navigateAddNewRoom() {

        val intent = Intent(this@DetailContactActivity, NewRoomChatFormActivity::class.java)
        intent.putExtra(CONST_NAME, tvName.text)
        intent.putExtra(CONST_PHONE, tvPhone.text)

        startActivity(intent)

    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (requestCode == MAIN_ACTIVITY_REQUEST_CODE) {
//
//            val resultData = data?.getStringExtra("$MAIN_ACTIVITY_REQUEST_CODE")
//
//            if (resultData == SYNC_NOW) {
//
//                finish()
//
//            }
//
//        }
//    }

    override fun onBackPressed() {

        if (isEdit) toggleEdit()
        else return super.onBackPressed()
    }

}