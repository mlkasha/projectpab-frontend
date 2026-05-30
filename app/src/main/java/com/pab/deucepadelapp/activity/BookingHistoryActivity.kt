package com.pab.deucepadelapp.activity

import android.content.Context
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pab.deucepadelapp.R
import com.pab.deucepadelapp.adapter.BookingHistoryAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// PANGGILAN MODEL DARI FILE PUSAT (PaymentNetworkModel) agar tidak redeclaration/bentrok
// Catatan: Jika file PaymentNetworkModel.kt berada di folder package yang sama,
// baris di bawah ini otomatis terhubung.

class BookingHistoryActivity : AppCompatActivity() {

    private lateinit var rvBookingHistory: RecyclerView
    private lateinit var btnBackHistory: ImageView

    // Alamat URL API Backend kamu
    private val BACKEND_URL = "https://paralegal-silicon-stoplight.ngrok-free.dev/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking_history)

        // Inisialisasi komponen UI dari XML
        rvBookingHistory = findViewById(R.id.rvBookingHistory)
        btnBackHistory = findViewById(R.id.btnBackHistory)

        // Atur RecyclerView agar list-nya memanjang ke bawah
        rvBookingHistory.layoutManager = LinearLayoutManager(this)

        // Aksi ketika tombol kembali (back arrow) diklik
        btnBackHistory.setOnClickListener {
            finish()
        }

        // Jalankan fungsi untuk nembak data ke database
        ambilDataRiwayatAsli()
    }

    private fun ambilDataRiwayatAsli() {
        // Mengambil token login dari SharedPreferences
        val sharedPreferences = getSharedPreferences("DeucePref", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("token", "") ?: ""
        val tokenBearer = "Bearer $token"

        // Cek jika token kosong
        if (token.isEmpty()) {
            Toast.makeText(this, "Sesi habis, silakan login ulang!", Toast.LENGTH_SHORT).show()
            return
        }

        // Konfigurasi Client Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl(BACKEND_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        // Membaca interface dari file PaymentNetworkModel.kt
        val service = retrofit.create(PaymentApiService::class.java)

        // Eksekusi request ke server secara asynchronous
        service.ambilRiwayatPembayaran(tokenBearer).enqueue(object : Callback<PaymentHistoryResponse> {
            override fun onResponse(
                call: Call<PaymentHistoryResponse>,
                response: Response<PaymentHistoryResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.success) {
                        // Pasang data list payment asli dari database ke dalam Adapter RecyclerView
                        val adapter = BookingHistoryAdapter(body.data)
                        rvBookingHistory.adapter = adapter
                    } else {
                        Toast.makeText(this@BookingHistoryActivity, body.message, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@BookingHistoryActivity, "Gagal mengambil data transaksi!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PaymentHistoryResponse>, t: Throwable) {
                // Beri pesan jika server mati atau koneksi internet bermasalah
                Toast.makeText(this@BookingHistoryActivity, "Error Koneksi: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}