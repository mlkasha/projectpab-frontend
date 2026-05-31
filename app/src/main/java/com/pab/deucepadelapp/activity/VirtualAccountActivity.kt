package com.pab.deucepadelapp.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pab.deucepadelapp.R

class VirtualAccountActivity : AppCompatActivity() {

    private lateinit var tvVaBankTitle: TextView
    private lateinit var tvVaNumber: TextView
    private lateinit var tvVaAmount: TextView
    private lateinit var btnCopyVa: Button
    private lateinit var btnUploadProof: Button

    private var currentBookingId: Long = 10L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_virtual_account)

        tvVaBankTitle = findViewById(R.id.tvVaBankTitle)
        tvVaNumber = findViewById(R.id.tvVaNumber)
        tvVaAmount = findViewById(R.id.tvVaAmount)
        btnCopyVa = findViewById(R.id.btnCopyVa)
        btnUploadProof = findViewById(R.id.btnUploadProof)

        val bankName = intent.getStringExtra("BANK_NAME") ?: "BCA"
        currentBookingId = intent.getLongExtra("BOOKING_ID", 10L)
        val grandTotal = intent.getDoubleExtra("GRAND_TOTAL", 158500.0)

        tvVaBankTitle.text = "$bankName Virtual Account"
        tvVaAmount.text = String.format("Rp %,.0f", grandTotal).replace(",", ".")

        val prefixVA = when (bankName.lowercase()) {
            "bca" -> "88308"
            "bni" -> "82770"
            "mandiri" -> "89508"
            else -> "85211"
        }
        tvVaNumber.text = "$prefixVA${(12345678..99999999).random()}"

        btnCopyVa.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Nomor VA", tvVaNumber.text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Nomor VA berhasil disalin!", Toast.LENGTH_SHORT).show()
        }

        btnUploadProof.setOnClickListener {
            val intentKeStruk = Intent(this, ReceiptSimulationActivity::class.java).apply {
                putExtra("BOOKING_ID", currentBookingId)
                putExtra("COURT_NAME", "Lapangan Padel")
                putExtra("PAYMENT_NAME", "VA $bankName")
                putExtra("GRAND_TOTAL", grandTotal)
            }
            startActivity(intentKeStruk)
            finish()
        }
    }
}