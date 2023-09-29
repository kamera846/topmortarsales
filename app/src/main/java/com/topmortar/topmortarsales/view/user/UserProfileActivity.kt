package com.topmortar.topmortarsales.view.user

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.topmortar.topmortarsales.commons.CONST_FULL_NAME
import com.topmortar.topmortarsales.commons.CONST_LOCATION
import com.topmortar.topmortarsales.commons.CONST_NAME
import com.topmortar.topmortarsales.commons.CONST_PHONE
import com.topmortar.topmortarsales.commons.CONST_USER_ID
import com.topmortar.topmortarsales.commons.CONST_USER_LEVEL
import com.topmortar.topmortarsales.commons.MANAGE_USER_ACTIVITY_REQUEST_CODE
import com.topmortar.topmortarsales.commons.utils.SessionManager
import com.topmortar.topmortarsales.commons.utils.convertDpToPx
import com.topmortar.topmortarsales.databinding.ActivityUserProfileBinding
import com.topmortar.topmortarsales.model.UserModel

class UserProfileActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var binding: ActivityUserProfileBinding

    private lateinit var iUserName: String; private lateinit var iFullName: String; private lateinit var iUserLevel: String
    private lateinit var iUserID: String; private lateinit var iPhone: String; private lateinit var iLocation: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        sessionManager = SessionManager(this)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initClickHandler()
        dataActivityValidation()

    }

    private fun navigateEditUser() {

        intent.putExtra(CONST_USER_ID, iUserID)
        intent.putExtra(CONST_PHONE, iPhone)
        intent.putExtra(CONST_NAME, iUserName)
        intent.putExtra(CONST_USER_LEVEL, iUserLevel)
        intent.putExtra(CONST_LOCATION, iLocation)
        intent.putExtra(CONST_FULL_NAME, iFullName)
        startActivityForResult(intent, MANAGE_USER_ACTIVITY_REQUEST_CODE)

    }

    private fun initClickHandler() {

        binding.titleBarLight.icEdit.visibility = View.VISIBLE
        binding.titleBarLight.icEdit.setOnClickListener {
            navigateEditUser()
        }

    }

    private fun dataActivityValidation() {

        iUserID = intent.getStringExtra(CONST_USER_ID).toString()
        iPhone = intent.getStringExtra(CONST_PHONE).toString()
        iUserName = intent.getStringExtra(CONST_NAME).toString()
        iFullName = intent.getStringExtra(CONST_FULL_NAME).toString()
        iUserLevel = intent.getStringExtra(CONST_USER_LEVEL).toString()
        iLocation = intent.getStringExtra(CONST_LOCATION).toString()

        if (iUserName.isNotEmpty()) {
            binding.titleBarLight.tvTitleBar.text = iUserName
            binding.titleBarLight.tvTitleBar.setPadding(0, 0, convertDpToPx(16, this), 0)
        }
        if (iFullName.isNotEmpty()) binding.tvFullName.text = iFullName
        if (iUserLevel.isNotEmpty()) binding.tvLevel.text = iUserLevel

    }

}