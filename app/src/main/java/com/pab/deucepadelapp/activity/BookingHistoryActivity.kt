package com.pab.deucepadelapp.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pab.deucepadelapp.R
import com.pab.deucepadelapp.adapter.BookingHistoryAdapter
import com.pab.deucepadelapp.network.ApiResponse
import com.pab.deucepadelapp.network.PaymentApiService
import com.pab.deucepadelapp.network.PaymentData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class BookingHistoryActivity : AppCompatActivity() {

    private lateinit var rvBookingHistory: RecyclerView
    private lateinit var btnBackHistory: ImageView
    private val BACKEND_URL = "https://paralegal-silicon-stoplight.ngrok-free.dev/"

    private lateinit var menuExplore: LinearLayout
    private lateinit var menuBookings: LinearLayout
    private lateinit var menuHistory: LinearLayout
    private lateinit var menuProfile: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking_history)

        rvBookingHistory = findViewById(R.id.rvBookingHistory)
        btnBackHistory = findViewById(R.id.btnBackHistory)

        rvBookingHistory.layoutManager = LinearLayoutManager(this)
        rvBookingHistory.adapter = BookingHistoryAdapter(arrayListOf())

        // Back button dibuat instan tanpa animasi penutupan
        btnBackHistory.setOnClickListener {
            finish()
            overridePendingTransition(0, 0)
        }

        setupBottomNavigationLogic()
        ambilDataRiwayatAsli()
    }

    private fun setupBottomNavigationLogic() {
        menuExplore = findViewById(R.id.menuExplore)
        menuBookings = findViewById(R.id.menuBookings)
        menuHistory = findViewById(R.id.menuHistory)
        menuProfile = findViewById(R.id.menuProfile)

        menuExplore.setOnClickListener {
            val intentHome = Intent(this, HomeActivity::class.java)
            intentHome.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intentHome)
            overridePendingTransition(0, 0)
        }

        menuBookings.setOnClickListener {
            val intentBookings = Intent(this, MyBookingsActivity::class.java)
            intentBookings.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intentBookings)
            overridePendingTransition(0, 0)
        }

        menuHistory.setOnClickListener {
            // Halaman saat ini, dibiarkan kosong untuk linking murni
        }

        menuProfile.setOnClickListener {
            val intentProfile = Intent(this, ProfileActivity::class.java)
            intentProfile.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intentProfile)
            overridePendingTransition(0, 0)
        }
    }

    private fun ambilDataRiwayatAsli() {
        val sharedPreferences = getSharedPreferences("DeucePref", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("token", "") ?: ""
        val tokenBearer = "Bearer $token"

        if (token.isEmpty()) {
            Toast.makeText(this, "Sesi habis, silakan login ulang!", Toast.LENGTH_SHORT).show()
            return
        }

        val retrofit = Retrofit.Builder()
            .baseUrl(BACKEND_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(PaymentApiService::class.java)

        service.getHistoryPembayaran(tokenBearer).enqueue(object : Callback<ApiResponse<List<PaymentData>>> {
            override fun onResponse(
                call: Call<ApiResponse<List<PaymentData>>>,
                response: Response<ApiResponse<List<PaymentData>>>
            ) {
                try {
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        if (body.success && body.data != null) {

                            // Filter data riwayat pembayaran agar sinkron dengan MyBookingsActivity
                            // Menampilkan semua item transaksi yang berstatus aktif/berhasil (UNPAID, UPLOADED, VERIFIED, CONFIRMED)
                            val listRiwayatFinal = ArrayList<PaymentData>()

                            for (payment in body.data) {
                                val statusUpper = payment.status?.uppercase() ?: ""
                                if (statusUpper == "UNPAID" || statusUpper == "UPLOADED" ||
                                    statusUpper == "VERIFIED" || statusUpper == "CONFIRMED") {
                                    listRiwayatFinal.add(payment)
                                }
                            }

                            // Set list data riwayat ke adapter RecyclerView
                            val adapter = BookingHistoryAdapter(listRiwayatFinal)
                            rvBookingHistory.adapter = adapter

                        } else {
                            Toast.makeText(this@BookingHistoryActivity, body.message ?: "Gagal memproses data", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@BookingHistoryActivity, "Gagal mengambil data transaksi dari server!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    // PENGAMAN UTAMA: Menangkap segala bentuk eror konversi data agar aplikasi TIDAK AKAN MENTAL
                    Toast.makeText(this@BookingHistoryActivity, "Format data tidak sesuai: ${e.message}", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<ApiResponse<List<PaymentData>>>, t: Throwable) {
                Toast.makeText(this@BookingHistoryActivity, "Error Koneksi Server: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}