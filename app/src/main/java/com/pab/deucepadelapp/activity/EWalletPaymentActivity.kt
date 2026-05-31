package com.pab.deucepadelapp.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.pab.deucepadelapp.R

class EWalletPaymentActivity : AppCompatActivity() {

    private lateinit var tvBridgeStatus: TextView
    private lateinit var imgEWalletLogo: ImageView
    private lateinit var progressBar: ProgressBar

    private var currentBookingId: Long = 10L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ewallet_payment)

        tvBridgeStatus = findViewById(R.id.tvBridgeStatus)
        imgEWalletLogo = findViewById(R.id.imgEWalletLogo)
        progressBar = findViewById(R.id.progressBar)

        val methodDisplayName = intent.getStringExtra("METHOD_DISPLAY_NAME") ?: "E-Wallet"
        currentBookingId = intent.getLongExtra("BOOKING_ID", 10L)

        tvBridgeStatus.text = "Menghubungkan ke $methodDisplayName..."

        when (methodDisplayName.lowercase()) {
            "dana" -> imgEWalletLogo.setImageResource(R.drawable.e_dana)
            "ovo" -> imgEWalletLogo.setImageResource(R.drawable.e_ovo)
            "shopeepay" -> imgEWalletLogo.setImageResource(R.drawable.e_spay)
            else -> imgEWalletLogo.setImageResource(R.drawable.logo_deuce)
        }

        // Langsung arahkan ke e-wallet & simulasi struk karena backend tidak butuh hit api simpan metode
        Handler(Looper.getMainLooper()).postDelayed({
            bukaEWalletDanSiapkanHalamanUpload(methodDisplayName)
        }, 2000)
    }

    private fun bukaEWalletDanSiapkanHalamanUpload(namaEWallet: String) {
        val urlTujuan = when (namaEWallet.lowercase()) {
            "dana" -> "dana://pay"
            "ovo" -> "ovo://payment"
            "shopeepay" -> "shopeepay://ext/pay"
            else -> "https://link.dana.id/pay"
        }

        val intentKeStruk = Intent(this, ReceiptSimulationActivity::class.java).apply {
            putExtra("BOOKING_ID", currentBookingId)
            putExtra("COURT_NAME", "Lapangan Padel")
            putExtra("PAYMENT_NAME", namaEWallet)
            putExtra("GRAND_TOTAL", 158500.0)
        }
        startActivity(intentKeStruk)

        try {
            val intentEWallet = Intent(Intent.ACTION_VIEW, Uri.parse(urlTujuan))
            startActivity(intentEWallet)
        } catch (e: Exception) {
            val intentWeb = Intent(Intent.ACTION_VIEW, Uri.parse("https://link.dana.id/pay"))
            startActivity(intentWeb)
        }

        finish()
    }
}