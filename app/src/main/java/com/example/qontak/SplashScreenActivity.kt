package com.example.qontak

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler

class SplashScreenActivity : AppCompatActivity() {

    private val splashScreenDuration = 3000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        supportActionBar?.hide()

        Handler().postDelayed({
            // Start your main activity here
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Finish the splash activity to prevent going back to it
        }, splashScreenDuration)

    }
}