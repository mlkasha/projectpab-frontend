package com.pab.deucepadelapp.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.pab.deucepadelapp.R

// KITA PAKAI IMPORT KLASIK YANG DIJAMIN DIKENAL OLEH SEMUA VERSI OKHTTP/RETROFIT
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import java.io.File
import java.io.FileOutputStream

interface UploadApiService {
    @Multipart
    @POST("api/payments/{bookingId}/proof")
    fun uploadBukti(
        @Path("bookingId") bookingId: Long,
        @Header("Authorization") bearerToken: String,
        @Part file: MultipartBody.Part
    ): Call<ResponseBody>
}

class UploadProofActivity : AppCompatActivity() {

    private lateinit var ivSelectedProof: ImageView
    private lateinit var btnSelectImage: Button
    private lateinit var btnSubmitProof: Button

    private var selectedImageUri: Uri? = null
    private var bookingId: Long = 10L
    private val BASE_URL = "http://10.0.2.2:8080/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_proof)

        ivSelectedProof = findViewById(R.id.ivSelectedProof)
        btnSelectImage = findViewById(R.id.btnSelectImage)
        btnSubmitProof = findViewById(R.id.btnSubmitProof)

        bookingId = intent.getLongExtra("BOOKING_ID", 10L)

        val launcherGaleri = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                selectedImageUri = result.data?.data
                ivSelectedProof.setImageURI(selectedImageUri)
            }
        }

        btnSelectImage.setOnClickListener {
            val intentGaleri = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            launcherGaleri.launch(intentGaleri)
        }

        btnSubmitProof.setOnClickListener {
            if (selectedImageUri == null) {
                Toast.makeText(this, "Pilih foto bukti transfer dulu jirr!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            prosesUploadFileKeBackend()
        }
    }

    private fun prosesUploadFileKeBackend() {
        val fileGambar = uriToFile(selectedImageUri!!, this)

        // SOLUSI TOTAL: Mengganti total extension .asRequestBody() yang bikin error 4 biji itu
        // Cara di bawah ini murni dibaca oleh OkHttp versi 3 maupun versi 4 tanpa drama
        val mediaTypeObj = MediaType.parse("image/*")
        val requestFile = RequestBody.create(mediaTypeObj, fileGambar)
        val bodyMultipart = MultipartBody.Part.createFormData("file", fileGambar.name, requestFile)

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(UploadApiService::class.java)

        val sharedPref = getSharedPreferences("DeuceAppPref", Context.MODE_PRIVATE)
        val tokenLokal = sharedPref.getString("JWT_TOKEN", "") ?: ""
        val tokenBearer = "Bearer $tokenLokal"

        apiService.uploadBukti(bookingId, tokenBearer, bodyMultipart).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@UploadProofActivity, "Bukti Berhasil Diupload!", Toast.LENGTH_LONG).show()

                    val intentHome = Intent(this@UploadProofActivity, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    startActivity(intentHome)
                    finish()
                } else {
                    Toast.makeText(this@UploadProofActivity, "Gagal upload, error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@UploadProofActivity, "Koneksi terputus: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun uriToFile(uri: Uri, context: Context): File {
        val contentResolver = context.contentResolver
        val fileTemp = File(context.cacheDir, "bukti_pembayaran.jpg")
        val inputStream = contentResolver.openInputStream(uri)
        val outputStream = FileOutputStream(fileTemp)
        inputStream?.use { input -> outputStream.use { output -> input.copyTo(output) } }
        return fileTemp
    }
}