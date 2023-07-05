package com.topmortar.topmortarsales

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.topmortar.topmortarsales.commons.ET_MESSAGE
import com.topmortar.topmortarsales.commons.ET_NAME
import com.topmortar.topmortarsales.commons.ET_PHONE

class DetailContactActivity : AppCompatActivity() {

//    private lateinit var ivProfile: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvPhone: TextView

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        setContentView(R.layout.activity_detail_contact)

//        ivProfile = findViewById(R.id.iv_profile)
        tvName = findViewById(R.id.tv_name)
        tvPhone = findViewById(R.id.tv_phone)

//        val ivProfileUrl = R.mipmap.logo_dark_square
//
//        Glide.with(this)
//            .load(ivProfileUrl)
//            .apply(RequestOptions().transform(CircleCrop()))
//            .into(ivProfile)

        dataActivityValidation()

    }

    private fun dataActivityValidation() {

        val intent = intent

        val iPhone = intent.getStringExtra(ET_PHONE)
        val iName = intent.getStringExtra(ET_NAME)

        if (iPhone != "") tvPhone.text = "+$iPhone"
        if (iName != "") tvName.text = iName

    }

}