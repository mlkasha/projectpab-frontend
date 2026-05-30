package com.pab.deucepadelapp.activity

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.pab.deucepadelapp.R
import com.pab.deucepadelapp.api.ApiClient
import com.pab.deucepadelapp.model.* // PERBAIKAN 1: Pakai tanda bintang (*) agar RegisterRequest dan AuthResponse terbaca semua
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_register)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val tvToLogin = findViewById<TextView>(R.id.tvToLogin)
        val btnSubmitRegister = findViewById<MaterialButton>(R.id.btnSubmitRegister)

        // AMBIL INPUTAN DARI XML
        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etPhone = findViewById<EditText>(R.id.etPhone)

        btnBack.setOnClickListener { finish() }
        tvToLogin.setOnClickListener { finish() }

        btnSubmitRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val phone = etPhone.text.toString().trim()

            // Validasi input kosong
            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Nama, Email, dan Password wajib diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // MANTRA UNTUK MENGIRIM DATA KE SPRING BOOT LEWAT INTERNET NGROK
            val request = RegisterRequest(name, email, password, phone)

            // PERBAIKAN 2: Mengubah .registerUser(request) menjadi .register(request) sesuai nama fungsi di ApiService
            ApiClient.instance.register(request).enqueue(object : Callback<AuthResponse> {
                override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        Toast.makeText(this@RegisterActivity, "Register Sukses! Silakan Login.", Toast.LENGTH_SHORT).show()

                        // 🌟 DI SINI PERUBAHANNYA: Sekarang dioper ke LoginActivity, bukan HomeActivity lagi
                        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@RegisterActivity, "Register Gagal: Email sudah terdaftar atau format salah", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    // Jika gagal koneksi (Spring boot mati / IP / Ngrok salah)
                    Toast.makeText(this@RegisterActivity, "Gagal konek ke server laptop: ${t.message}", Toast.LENGTH_LONG).show()
                }
            }) // Tutup enqueue Retrofit yang benar
        } // Tutup btnSubmitRegister
    } // Tutup onCreate
} // Tutup RegisterActivity