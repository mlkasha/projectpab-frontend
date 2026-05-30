package com.pab.deucepadelapp.activity

import android.content.Context
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
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)

        btnBack.setOnClickListener {
            finish()
        }

        tvToRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
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

            ApiClient.instance.login(request).enqueue(object : Callback<AuthResponse> {
                override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val authData = response.body()!!
                        val message = authData.message
                        val userName = authData.data?.user?.name ?: "User"
                        val userToken = authData.data?.token ?: ""

                        val sharedPref = getSharedPreferences("DeucePref", Context.MODE_PRIVATE)
                        sharedPref.edit().apply {
                            putString("token", userToken)
                            putString("USER_NAME", userName)
                            apply()
                        }

                        Toast.makeText(this@LoginActivity, "$message! Selamat datang $userName", Toast.LENGTH_LONG).show()

                        val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                        intent.putExtra("USER_NAME", userName)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, "Login Gagal: Email atau password salah!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, "Terjadi kesalahan jaringan: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}