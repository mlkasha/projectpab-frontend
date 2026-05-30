package com.pab.deucepadelapp.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pab.deucepadelapp.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header

// STRUCT DATA SESUAI FORMAT RESPONSE WRAPPER BACKEND KAMU
data class UserProfileResponse(
    val success: Boolean,
    val message: String,
    val data: RealUserData
)

data class RealUserData(
    val id: Int,
    val name: String,
    val email: String,
    val phone: String?,
    val role: String
)

// INTERFACE RETROFIT UNTUK GET /api/auth/me
interface ProfileApiService {
    @GET("api/auth/me")
    fun dapatkanDataUserAktif(
        @Header("Authorization") tokenAuth: String
    ): Call<UserProfileResponse>
}

class ProfileActivity : AppCompatActivity() {

    private lateinit var tvProfileName: TextView
    private lateinit var tvProfileRole: TextView
    private lateinit var tvProfileEmail: TextView
    private lateinit var tvProfilePhone: TextView
    private lateinit var btnBookNow: Button

    // Mengarah ke localhost laptop via Emulator Android Studio
    private val BACKEND_URL = "http://10.0.2.2:8080/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Hubungkan Variabel dengan ID Komponen XML
        tvProfileName = findViewById(R.id.tvProfileName)
        tvProfileRole = findViewById(R.id.tvProfileRole)
        tvProfileEmail = findViewById(R.id.tvProfileEmail)
        tvProfilePhone = findViewById(R.id.tvProfilePhone)
        btnBookNow = findViewById(R.id.btnBookNow)

        // Event Klik Tombol Booking Lapangan
        btnBookNow.setOnClickListener {
            Toast.makeText(this, "Membuka daftar Lapangan Padel...", Toast.LENGTH_SHORT).show()
        }

        // Jalankan fungsi ambil data riil database
        muatDataProfileAsli()
    }

    private fun muatDataProfileAsli() {
        // Menggunakan Retrofit cara klasik murni biar gak bentrok library okhttp extension
        val retrofit = Retrofit.Builder()
            .baseUrl(BACKEND_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val serviceApi = retrofit.create(ProfileApiService::class.java)

        // Menarik JWT token asli dari SharedPreferences login-an kamu jirr
        val sharedPreferences = getSharedPreferences("DeuceAppPref", Context.MODE_PRIVATE)
        val stringToken = sharedPreferences.getString("JWT_TOKEN", "") ?: ""
        val headerBearerToken = "Bearer $stringToken"

        if (stringToken.isEmpty()) {
            Toast.makeText(this, "Sesi login habis, silakan login ulang jirr!", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Tembak API database Spring Boot
        serviceApi.dapatkanDataUserAktif(headerBearerToken).enqueue(object : Callback<UserProfileResponse> {
            override fun onResponse(call: Call<UserProfileResponse>, response: Response<UserProfileResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val bodyRespons = response.body()!!

                    if (bodyRespons.success) {
                        val dataUserDariDatabase = bodyRespons.data

                        // Menuangkan isi DB langsung ke komponen UI
                        tvProfileName.text = dataUserDariDatabase.name
                        tvProfileRole.text = "Role: ${dataUserDariDatabase.role}"
                        tvProfileEmail.text = dataUserDariDatabase.email
                        tvProfilePhone.text = dataUserDariDatabase.phone ?: "Belum Mengisi No HP"
                    } else {
                        Toast.makeText(this@ProfileActivity, "Pesan Backend: ${bodyRespons.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@ProfileActivity, "Gagal terhubung, Error code: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                // Muncul kalau Spring Boot kamu di laptop mati jirr
                Toast.makeText(this@ProfileActivity, "Koneksi ke Database Terputus: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}