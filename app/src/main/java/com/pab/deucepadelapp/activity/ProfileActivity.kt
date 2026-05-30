package com.pab.deucepadelapp.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.TextView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.pab.deucepadelapp.R
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

data class UserProfileResponse(
    val success: Boolean,
    val message: String,
    val data: RealUserData
)

data class RealUserData(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,
    val avatarBase64: String?
)

data class UpdateProfileFormRequest(
    val name: String,
    val email: String,
    val password: String?
)

data class UpdateAvatarRequest(
    val avatarBase64: String
)

interface ProfileApiService {
    @GET("api/auth/me")
    fun dapatkanDataUserAktif(
        @Header("Authorization") tokenAuth: String
    ): Call<UserProfileResponse>

    @PUT("api/auth/update-profile")
    fun perbaruiProfileForm(
        @Header("Authorization") tokenAuth: String,
        @Body request: UpdateProfileFormRequest
    ): Call<UserProfileResponse>

    @PUT("api/auth/update-avatar")
    fun perbaruiAvatar(
        @Header("Authorization") tokenAuth: String,
        @Body request: UpdateAvatarRequest
    ): Call<ResponseBody>

    @DELETE("api/auth/delete-account")
    fun hapusAkunUser(
        @Header("Authorization") tokenAuth: String
    ): Call<ResponseBody>
}

class ProfileActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var ivAvatar: ImageView
    private lateinit var etName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnSaveChanges: MaterialButton
    private lateinit var btnDeleteAccount: TextView

    // Properti Menu Bottom Navigation
    private lateinit var menuExplore: LinearLayout
    private lateinit var menuBookings: LinearLayout
    private lateinit var menuHistory: LinearLayout
    private lateinit var menuProfile: LinearLayout

    private val BACKEND_URL = "https://paralegal-silicon-stoplight.ngrok-free.dev/"
    private var tokenBearerStr = ""

    private val bukaGaleriLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val fotoUri: Uri? = result.data?.data
            if (fotoUri != null) {
                prosesUploadAvatar(fotoUri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // 1. Inisialisasi ID Utama Form
        btnBack = findViewById(R.id.btnBack)
        ivAvatar = findViewById(R.id.ivAvatar)
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnSaveChanges = findViewById(R.id.btnSaveChanges)
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount)

        // 2. Inisialisasi ID Bottom Navigation
        menuExplore = findViewById(R.id.menuExplore)
        menuBookings = findViewById(R.id.menuBookings)
        menuHistory = findViewById(R.id.menuHistory)
        menuProfile = findViewById(R.id.menuProfile)

        // 3. Panggil fungsi setup navigasi setelah view ter-inisialisasi
        setupBottomNavigation()

        val sharedPreferences = getSharedPreferences("DeucePref", Context.MODE_PRIVATE)
        val stringToken = sharedPreferences.getString("token", "") ?: ""
        tokenBearerStr = "Bearer $stringToken"

        if (stringToken.isEmpty()) {
            Toast.makeText(this, "Sesi login habis, silakan login ulang!", Toast.LENGTH_LONG).show()
            keluarKeHalamanLogin()
            return
        }

        // Tombol Kembali
        btnBack.setOnClickListener {
            finish()
        }

        // Klik Foto Profil untuk Ganti Gambar
        ivAvatar.setOnClickListener {
            val intentGaleri = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            bukaGaleriLauncher.launch(intentGaleri)
        }

        // Tombol Save Changes
        btnSaveChanges.setOnClickListener {
            prosesSimpanPerubahanProfil()
        }

        // Tombol Hapus Akun
        btnDeleteAccount.setOnClickListener {
            tampilkanKonfirmasiHapusAkun()
        }

        muatDataProfileAsli()
    }

    // Fungsi setup navigasi ditaruh secara mandiri di luar onCreate
    private fun setupBottomNavigation() {
        menuExplore.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        menuBookings.setOnClickListener {
            Toast.makeText(
                this,
                "Halaman Booking masih dalam pengembangan",
                Toast.LENGTH_SHORT
            ).show()
        }

        menuHistory.setOnClickListener {
            startActivity(Intent(this, BookingHistoryActivity::class.java))
            finish()
        }

        menuProfile.setOnClickListener {
            // Malika sedang berada di halaman profile, abaikan atau beri feedback ringan
        }
    }

    private fun muatDataProfileAsli() {
        val retrofit = Retrofit.Builder()
            .baseUrl(BACKEND_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val serviceApi = retrofit.create(ProfileApiService::class.java)

        serviceApi.dapatkanDataUserAktif(tokenBearerStr).enqueue(object : Callback<UserProfileResponse> {
            override fun onResponse(call: Call<UserProfileResponse>, response: Response<UserProfileResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val bodyRespons = response.body()!!
                    if (bodyRespons.success) {
                        val dataUserDariDatabase = bodyRespons.data

                        etName.setText(dataUserDariDatabase.name)
                        etEmail.setText(dataUserDariDatabase.email)
                        etPassword.setText("")

                        if (!dataUserDariDatabase.avatarBase64.isNullOrEmpty()) {
                            val byteGambar = android.util.Base64.decode(dataUserDariDatabase.avatarBase64, android.util.Base64.DEFAULT)
                            Glide.with(this@ProfileActivity)
                                .load(byteGambar)
                                .placeholder(android.R.drawable.ic_menu_gallery)
                                .error(android.R.drawable.ic_menu_gallery)
                                .into(ivAvatar)
                        }
                    }
                }
            }
            override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                Toast.makeText(this@ProfileActivity, "Koneksi Bermasalah: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun prosesSimpanPerubahanProfil() {
        val namaInput = etName.text.toString().trim()
        val emailInput = etEmail.text.toString().trim()
        val passwordInput = etPassword.text.toString().trim()

        if (namaInput.isEmpty() || emailInput.isEmpty()) {
            Toast.makeText(this, "Nama dan Email tidak boleh kosong!", Toast.LENGTH_SHORT).show()
            return
        }

        val passwordKirim = if (passwordInput.isEmpty()) null else passwordInput

        val retrofit = Retrofit.Builder()
            .baseUrl(BACKEND_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val serviceApi = retrofit.create(ProfileApiService::class.java)
        val request = UpdateProfileFormRequest(namaInput, emailInput, passwordKirim)

        serviceApi.perbaruiProfileForm(tokenBearerStr, request).enqueue(object : Callback<UserProfileResponse> {
            override fun onResponse(call: Call<UserProfileResponse>, response: Response<UserProfileResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ProfileActivity, "Profil berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                    muatDataProfileAsli()
                } else {
                    Toast.makeText(this@ProfileActivity, "Gagal memperbarui data profil!", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                Toast.makeText(this@ProfileActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun prosesUploadAvatar(uriGambar: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uriGambar)
            val bytesData = inputStream?.readBytes()
            inputStream?.close()

            if (bytesData == null) {
                Toast.makeText(this, "Gagal memproses gambar profil!", Toast.LENGTH_SHORT).show()
                return
            }

            val teksBase64Avatar = android.util.Base64.encodeToString(bytesData, android.util.Base64.DEFAULT)

            val retrofit = Retrofit.Builder()
                .baseUrl(BACKEND_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val serviceApi = retrofit.create(ProfileApiService::class.java)
            val requestBody = UpdateAvatarRequest(avatarBase64 = teksBase64Avatar)

            serviceApi.perbaruiAvatar(tokenBearerStr, requestBody).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@ProfileActivity, "Foto profil berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                        muatDataProfileAsli()
                    } else {
                        Toast.makeText(this@ProfileActivity, "Gagal mengunggah foto profil baru!", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(this@ProfileActivity, "Koneksi terputus: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun tampilkanKonfirmasiHapusAkun() {
        AlertDialog.Builder(this)
            .setTitle("Hapus Akun Permanen?")
            .setMessage("Apakah Anda yakin ingin menghapus akun ini? Semua data booking dan transaksi akan hilang selamanya.")
            .setPositiveButton("YA, HAPUS") { dialog, _ ->
                val retrofit = Retrofit.Builder()
                    .baseUrl(BACKEND_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val serviceApi = retrofit.create(ProfileApiService::class.java)

                serviceApi.hapusAkunUser(tokenBearerStr).enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@ProfileActivity, "Akun sukses dihapus!", Toast.LENGTH_LONG).show()

                            val sharedPref = getSharedPreferences("DeucePref", Context.MODE_PRIVATE)
                            sharedPref.edit().clear().apply()

                            keluarKeHalamanLogin()
                        } else {
                            Toast.makeText(this@ProfileActivity, "Gagal menghapus akun!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Toast.makeText(this@ProfileActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
                dialog.dismiss()
            }
            .setNegativeButton("BATAL") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun keluarKeHalamanLogin() {
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}