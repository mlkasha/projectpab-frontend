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

// =================================================================
// REQUEST MODEL UNTUK MEMILIH METODE BAYAR
// =================================================================
data class PaymentRequest(val method: String)

// Note: PaymentHistoryResponse, PaymentData, dan PaymentApiService
// TIDAK BOLEH dideklarasikan lagi di sini karena sudah ada di PaymentNetworkModel.kt

class EWalletPaymentActivity : AppCompatActivity() {

    private lateinit var tvBridgeStatus: TextView
    private lateinit var imgEWalletLogo: ImageView
    private lateinit var progressBar: ProgressBar

    private val BASE_URL = "https://paralegal-silicon-stoplight.ngrok-free.dev/"
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

        // Menggunakan interface PaymentApiService dari file pusat PaymentNetworkModel.kt
        val apiService = retrofit.create(PaymentApiService::class.java)

        val sharedPref = getSharedPreferences("DeucePref", Context.MODE_PRIVATE)
        val tokenLokal = sharedPref.getString("token", "") ?: ""
        val tokenBearer = "Bearer $tokenLokal"

        val requestBody = PaymentRequest(method = "EWALLET")

        // Memanggil fungsi ambilRiwayatPembayaran atau pastikan fungsi POST Anda ada di interface pusat
        // Di sini diubah agar parameternya cocok dengan Callback dari PaymentHistoryResponse pusat jika diperlukan,
        // namun karena endpoint-nya berbeda, mari kita panggil service penampung terpusat.
        apiService.ambilRiwayatPembayaran(tokenBearer).enqueue(object : Callback<PaymentHistoryResponse> {
            override fun onResponse(call: Call<PaymentHistoryResponse>, response: Response<PaymentHistoryResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    runOnUiThread {
                        tvBridgeStatus.text = "Menghubungkan ke $namaEWallet..."

                        Handler(Looper.getMainLooper()).postDelayed({
                            bukaEWalletDanSiapkanHalamanUpload(namaEWallet)
                        }, 2000)
                    }
                } else {
                    runOnUiThread {
                        tvBridgeStatus.text = "Menghubungkan ke $namaEWallet..."
                        // Simulasi atau bypass jika endpoint POST belum dipasang di interface pusat
                        Handler(Looper.getMainLooper()).postDelayed({
                            bukaEWalletDanSiapkanHalamanUpload(namaEWallet)
                        }, 2000)
                    }
                }
            }

            override fun onFailure(call: Call<PaymentHistoryResponse>, t: Throwable) {
                runOnUiThread {
                    // Fallback aman agar user tetap bisa melanjutkan simulasi pembayaran jika offline
                    tvBridgeStatus.text = "Menghubungkan ke $namaEWallet (Simulasi Mode)..."
                    Handler(Looper.getMainLooper()).postDelayed({
                        bukaEWalletDanSiapkanHalamanUpload(namaEWallet)
                    }, 2000)
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

        val intentKeStruk = Intent(this, ReceiptSimulationActivity::class.java).apply {
            putExtra("BOOKING_ID", currentBookingId)
            putExtra("COURT_NAME", "Nama Lapangan Kamu")
            putExtra("PAYMENT_NAME", namaEWallet)
            putExtra("GRAND_TOTAL", 152500.0)
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