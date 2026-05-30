package com.pab.deucepadelapp.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.pab.deucepadelapp.R

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_splash)

        val ivLogoDeuce = findViewById<ImageView>(R.id.ivLogoDeuce)
        val splashAnim = AnimationUtils.loadAnimation(this, R.anim.splash_animation)
        ivLogoDeuce.startAnimation(splashAnim)

        // TIMER DIUBAH JADI 5 DETIK (5000 milidetik)
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

            // KODE KUNCI: Efek transisi timbul/slide up saat berpindah halaman
            overridePendingTransition(R.anim.slide_in_up, R.anim.fade_out)

            finish()
        }, 5000)
    }
}