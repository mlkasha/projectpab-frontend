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
import com.pab.deucepadelapp.model.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_login)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val tvToRegister = findViewById<TextView>(R.id.tvToRegister)
        val btnSubmitLogin = findViewById<MaterialButton>(R.id.btnSubmitLogin)

        // Pastikan ID EditText ini sesuai dengan yang ada di activity_login.xml kamu
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)

        btnBack.setOnClickListener {
            finish()
        }

        tvToRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            // HAPUS finish() di sini agar jika user menekan Back di halaman Register,
            // dia bisa kembali lagi ke halaman Login dengan normal.
        }

        btnSubmitLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan password wajib diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Mencoba masuk ke akun...", Toast.LENGTH_SHORT).show()

            val request = LoginRequest(email, password)

            // Menembak endpoint POST /api/auth/login lewat jembatan Retrofit
            ApiClient.instance.login(request).enqueue(object : Callback<AuthResponse> {
                override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val authData = response.body()!!
                        val message = authData.message

                        // Ambil nama user dari data objek internal database (Amankan dengan tanda tanya (?) jika null)
                        val userName = authData.data?.user?.name ?: "User"

                        // Memunculkan pesan sukses beserta nama asli dari database users
                        Toast.makeText(this@LoginActivity, "$message! Selamat datang $userName", Toast.LENGTH_LONG).show()

                        // Pindah ke HomeActivity karena hak akses terverifikasi
                        val intent = Intent(this@LoginActivity, HomeActivity::class.java)

                        // Perubahan Vital: Kirim nama user ke HomeActivity agar tampil di teks "Hello, [Nama]!"
                        intent.putExtra("USER_NAME", userName)

                        startActivity(intent)
                        finish() // Menutup LoginActivity agar user tidak bisa back ke halaman login setelah masuk
                    } else {
                        // Respon dari Back-End jika email/password salah (Error 401)
                        Toast.makeText(this@LoginActivity, "Login Gagal: Email atau password salah!", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    // Terjadi masalah koneksi jaringan (misal server mati atau IP salah)
                    Toast.makeText(this@LoginActivity, "Terjadi kesalahan jaringan: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }
    }
}