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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pab.deucepadelapp.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

// MODEL RETROFIT SESUAI DOKUMEN API
data class PaymentRequest(val method: String)
data class PaymentResponseWrapper(val success: Boolean, val message: String, val data: PaymentData)
data class PaymentData(val id: Int, val bookingId: Long, val method: String, val status: String)

interface PaymentApiService {
    @POST("api/payments/{bookingId}/method")
    fun pilihMetodeBayar(
        @Path("bookingId") bookingId: Long,
        @Header("Authorization") bearerToken: String,
        @Body request: PaymentRequest
    ): Call<PaymentResponseWrapper>
}

class EWalletPaymentActivity : AppCompatActivity() {

    private lateinit var tvBridgeStatus: TextView
    private lateinit var imgEWalletLogo: ImageView
    private lateinit var progressBar: ProgressBar

    private val BASE_URL = "http://10.0.2.2:8080/" // Mengarah ke localhost laptop via emulator
    private var currentBookingId: Long = 10L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ewallet_payment)

        tvBridgeStatus = findViewById(R.id.tvBridgeStatus)
        imgEWalletLogo = findViewById(R.id.imgEWalletLogo)
        progressBar = findViewById(R.id.progressBar)

        val methodDisplayName = intent.getStringExtra("METHOD_DISPLAY_NAME") ?: "E-Wallet"
        currentBookingId = intent.getLongExtra("BOOKING_ID", 10L)

        tvBridgeStatus.text = "Mendaftarkan metode EWALLET ke server..."

        // Cek ketersediaan aset gambar logo di folder res/drawable kamu jirr!
        when (methodDisplayName.lowercase()) {
            "dana" -> imgEWalletLogo.setImageResource(R.drawable.e_dana)
            "ovo" -> imgEWalletLogo.setImageResource(R.drawable.e_ovo)
            "shopeepay" -> imgEWalletLogo.setImageResource(R.drawable.e_spay)
            else -> imgEWalletLogo.setImageResource(R.drawable.logo_deuce)
        }

        hitApiPilihMetode(currentBookingId, methodDisplayName)
    }

    private fun hitApiPilihMetode(bookingId: Long, namaEWallet: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(PaymentApiService::class.java)

        val sharedPref = getSharedPreferences("DeuceAppPref", Context.MODE_PRIVATE)
        val tokenLokal = sharedPref.getString("JWT_TOKEN", "") ?: ""
        val tokenBearer = "Bearer $tokenLokal"

        val requestBody = PaymentRequest(method = "EWALLET")

        apiService.pilihMetodeBayar(bookingId, tokenBearer, requestBody).enqueue(object : Callback<PaymentResponseWrapper> {
            override fun onResponse(call: Call<PaymentResponseWrapper>, response: Response<PaymentResponseWrapper>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    runOnUiThread {
                        tvBridgeStatus.text = "Menghubungkan ke $namaEWallet..."

                        // Jeda 2 detik biar transisi loading-nya keliatan premium
                        Handler(Looper.getMainLooper()).postDelayed({
                            bukaEWalletDanSiapkanHalamanUpload(namaEWallet)
                        }, 2000)
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@EWalletPaymentActivity, "Gagal sinkronisasi backend! Kode: ${response.code()}", Toast.LENGTH_LONG).show()
                        finish()
                    }
                }
            }

            override fun onFailure(call: Call<PaymentResponseWrapper>, t: Throwable) {
                runOnUiThread {
                    Toast.makeText(this@EWalletPaymentActivity, "Server mati atau offline: ${t.message}", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        })
    }

    private fun bukaEWalletDanSiapkanHalamanUpload(namaEWallet: String) {
        val urlTujuan = when (namaEWallet.lowercase()) {
            "dana" -> "dana://pay"
            "ovo" -> "ovo://payment"
            "shopeepay" -> "shopeepay://ext/pay"
            else -> "https://link.dana.id/pay"
        }

        // Mempersiapkan halaman upload bukti di tumpukan Activity Android
        val intentUpload = Intent(this, UploadProofActivity::class.java).apply {
            putExtra("BOOKING_ID", currentBookingId)
        }
        startActivity(intentUpload)

        // Buka aplikasi E-Wallet luar
        try {
            val intentEWallet = Intent(Intent.ACTION_VIEW, Uri.parse(urlTujuan))
            startActivity(intentEWallet)
        } catch (e: Exception) {
            // Fallback link web browser jika aplikasi e-wallet belum terinstal di HP/Emulator
            val intentWeb = Intent(Intent.ACTION_VIEW, Uri.parse("https://link.dana.id/pay"))
            startActivity(intentWeb)
        }

        finish() // Hancurkan halaman jembatan loading agar tidak menumpuk pas di-back
    }
}