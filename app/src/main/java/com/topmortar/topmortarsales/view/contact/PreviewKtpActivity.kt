package com.topmortar.topmortarsales.view.contact

import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.chrisbanes.photoview.PhotoView
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.topmortar.topmortarsales.R
import com.topmortar.topmortarsales.commons.CONST_KTP
import com.topmortar.topmortarsales.commons.utils.convertDpToPx

class PreviewKtpActivity : AppCompatActivity() {

    private lateinit var tvTitleBar: TextView
    private lateinit var tvEmpty: TextView
    private lateinit var icBack: ImageView
    private lateinit var lnrFooter: LinearLayout
    private lateinit var lnrTitleBar: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        setContentView(R.layout.activity_preview_ktp)

        tvTitleBar = findViewById(R.id.tv_title_bar)
        tvEmpty = findViewById(R.id.tv_empty)
        icBack = findViewById(R.id.ic_back)
        lnrFooter = findViewById(R.id.footer)
        lnrTitleBar = findViewById(R.id.title_bar_light)

        tvTitleBar.text = "Preview File"
        tvTitleBar.setPadding(0, 0, convertDpToPx(16, this), 0)

        val imageUrl = intent.getStringExtra(CONST_KTP) // Dapatkan URL gambar dari intent

        val photoView = findViewById<PhotoView>(R.id.photoView)

        // Menggunakan Picasso untuk memuat dan menampilkan gambar dari URL
        Picasso.get()
            .load(imageUrl)
            .into(photoView, object : Callback {
                override fun onSuccess() {
                    Handler().postDelayed({
                        lnrFooter.visibility = View.GONE
                        lnrTitleBar.visibility = View.GONE
                    }, 5000)
                }

                override fun onError(e: Exception?) {
                    photoView.visibility = View.GONE
                    tvEmpty.visibility = View.VISIBLE
                    e?.printStackTrace()
                }
            })

        // Mengaktifkan zoom
        photoView.maximumScale = 5f // Atur tingkat zoom maksimum sesuai kebutuhan

        // Mengaktifkan rotasi
        photoView.rotation = 0f // Atur rotasi awal gambar (dalam derajat)
        photoView.setOnClickListener { photoView.rotation = photoView.rotation + 90F }
        icBack.setOnClickListener { finish() }
        photoView.setOnPhotoTapListener { view, x, y ->
            lnrFooter.visibility = View.VISIBLE
            lnrTitleBar.visibility = View.VISIBLE
            Handler().postDelayed({
                lnrFooter.visibility = View.GONE
                lnrTitleBar.visibility = View.GONE
            }, 5000)
        }
    }
}
