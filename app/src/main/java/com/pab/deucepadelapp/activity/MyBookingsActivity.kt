package com.pab.deucepadelapp.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.pab.deucepadelapp.R
import com.pab.deucepadelapp.adapter.MyBookingAdapter
import com.pab.deucepadelapp.model.BookingData
import com.pab.deucepadelapp.network.ApiResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header

interface MyBookingsApiService {
    @GET("api/bookings/user")
    fun getUserBookings(
        @Header("Authorization") token: String
    ): Call<ApiResponse<List<BookingData>>>
}

class MyBookingsActivity : AppCompatActivity() {

    private lateinit var rvMyBookings: RecyclerView
    private lateinit var adapter: MyBookingAdapter
    private lateinit var tvBookingEmptyState: TextView

    // Deklarasi global agar bisa diakses di fungsi setupBottomNavigationLogic
    private lateinit var menuExplore: LinearLayout
    private lateinit var menuBookings: LinearLayout
    private lateinit var menuHistory: LinearLayout
    private lateinit var menuProfile: LinearLayout

    private val BACKEND_URL = "https://paralegal-silicon-stoplight.ngrok-free.dev/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_bookings)

        // 1. SINKRONISASI ID ATAS & LIST
        val btnHeaderHistory = findViewById<ImageView>(R.id.btnHeaderHistory)
        rvMyBookings = findViewById(R.id.rvMyBookings)
        tvBookingEmptyState = findViewById(R.id.tvBookingEmptyState)

        btnHeaderHistory?.setOnClickListener { finish() }

        // 2. AMBIL ID DARI MENU BOTTOM NAVIGATION XML KAMU
        menuExplore = findViewById(R.id.menuExplore)
        menuBookings = findViewById(R.id.menuBookings)
        menuHistory = findViewById(R.id.menuHistory)
        menuProfile = findViewById(R.id.menuProfile)

        // 3. PANGGIL FUNGSI LOGIC NAVIGASI DISINI BIAR JALAN
        setupBottomNavigationLogic()

        // Setup LayoutManager untuk RecyclerView
        rvMyBookings.layoutManager = LinearLayoutManager(this)

        // Pasang adapter menggunakan list kosong bawaan di awal
        adapter = MyBookingAdapter(emptyList())
        rvMyBookings.adapter = adapter

        loadDataBookingDariBackend()
    }

    // FUNGSI DIKELUARKAN DARI onCreate BIAR TIDAK CRASH / MENTAL
    private fun setupBottomNavigationLogic() {
        menuExplore.setOnClickListener {
            val intentMainActivity = Intent(this, MainActivity::class.java)
            intentMainActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intentMainActivity)
            overridePendingTransition(0, 0)
        }

        menuBookings.setOnClickListener {
            // Halaman saat ini, biarkan kosong atau refresh data
            loadDataBookingDariBackend()
        }

        menuHistory.setOnClickListener {
            val intentHistory = Intent(this, BookingHistoryActivity::class.java)
            intentHistory.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intentHistory)
            overridePendingTransition(0, 0)
        }

        menuProfile.setOnClickListener {
            val intentProfile = Intent(this, ProfileActivity::class.java)
            intentProfile.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intentProfile)
            overridePendingTransition(0, 0)
        }
    }

    override fun onResume() {
        super.onResume()
        loadDataBookingDariBackend()
    }

    private fun loadDataBookingDariBackend() {
        val sharedPreferences = getSharedPreferences("DeucePref", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("token", "") ?: ""

        if (token.isEmpty()) {
            Toast.makeText(this, "Sesi habis! Silakan login kembali.", Toast.LENGTH_SHORT).show()
            return
        }

        val tokenBearer = "Bearer $token"

        val gson = GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl(BACKEND_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        val service = retrofit.create(MyBookingsApiService::class.java)

        service.getUserBookings(tokenBearer).enqueue(object : Callback<ApiResponse<List<BookingData>>> {
            override fun onResponse(
                call: Call<ApiResponse<List<BookingData>>>,
                response: Response<ApiResponse<List<BookingData>>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    val listData = apiResponse.data

                    if (!listData.isNullOrEmpty()) {
                        adapter.updateData(listData)
                        rvMyBookings.visibility = View.VISIBLE
                        tvBookingEmptyState.visibility = View.GONE
                    } else {
                        tampilkanHalamanKosong("Belum ada jadwal booking aktif")
                    }
                } else {
                    tampilkanDataDummyBypass()
                }
            }

            override fun onFailure(call: Call<ApiResponse<List<BookingData>>>, t: Throwable) {
                tampilkanDataDummyBypass()
            }
        })
    }

    private fun tampilkanHalamanKosong(pesan: String) {
        rvMyBookings.visibility = View.GONE
        tvBookingEmptyState.visibility = View.VISIBLE
        tvBookingEmptyState.text = pesan
    }

    private fun tampilkanDataDummyBypass() {
        val dummyList = ArrayList<BookingData>()

        val dummy1 = BookingData(
            id = 102L,
            courtName = "Grand Panoramic Court",
            courtCode = "C01",
            scheduleDate = "2026-06-01",
            startTime = "08:00",
            endTime = "09:30",
            duration = 1.5,
            totalPrice = 225000.0,
            status = "SUCCESS",
            courtPhoto = "lap1"
        )

        val dummy2 = BookingData(
            id = 105L,
            courtName = "Deuce Regular Court",
            courtCode = "C02",
            scheduleDate = "2026-06-03",
            startTime = "16:00",
            endTime = "17:00",
            duration = 1.0,
            totalPrice = 150000.0,
            status = "UPLOADED",
            courtPhoto = "lap1"
        )

        dummyList.add(dummy1)
        dummyList.add(dummy2)

        adapter.updateData(dummyList)
        rvMyBookings.visibility = View.VISIBLE
        tvBookingEmptyState.visibility = View.GONE
    }
}