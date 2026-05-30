package com.pab.deucepadelapp.activity

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.pab.deucepadelapp.R
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

interface ReceiptUploadApiService {
    @Multipart
    @POST("api/payments/{bookingId}/proof")
    @Headers("ngrok-skip-browser-warning: bypass")
    fun uploadBukti(
        @Path("bookingId") bookingId: Long,
        @Header("Authorization") bearerToken: String,
        @Part file: MultipartBody.Part
    ): Call<ResponseBody>
}

class ReceiptSimulationActivity : AppCompatActivity() {

    private lateinit var ivReceiptSelectedProof: ImageView
    private lateinit var btnReceiptSelectImage: Button
    private lateinit var btnReceiptSubmitProof: Button

    private var selectedImageUri: Uri? = null
    private var bookingId: Long = 10L
    private val BASE_URL = "https://paralegal-silicon-stoplight.ngrok-free.dev/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_receipt_simulation)

        val layoutCardStruk: LinearLayout = findViewById(R.id.layoutCardStruk)
        val tvReceiptBookingId: TextView = findViewById(R.id.tvReceiptBookingId)
        val tvReceiptCourtName: TextView = findViewById(R.id.tvReceiptCourtName)
        val tvReceiptMethod: TextView = findViewById(R.id.tvReceiptMethod)
        val tvReceiptCode: TextView = findViewById(R.id.tvReceiptCode)
        val tvReceiptTotal: TextView = findViewById(R.id.tvReceiptTotal)
        val btnDownloadReceipt: Button = findViewById(R.id.btnDownloadReceipt)

        ivReceiptSelectedProof = findViewById(R.id.ivReceiptSelectedProof)
        btnReceiptSelectImage = findViewById(R.id.btnReceiptSelectImage)
        btnReceiptSubmitProof = findViewById(R.id.btnReceiptSubmitProof)

        bookingId = intent.getLongExtra("BOOKING_ID", 10L)
        val courtName = intent.getStringExtra("COURT_NAME") ?: "Padel Court"
        val paymentName = intent.getStringExtra("PAYMENT_NAME") ?: "BCA Virtual Account"
        val grandTotal = intent.getDoubleExtra("GRAND_TOTAL", 152500.0)

        val randomCode = (100000000000..999999999999).random().toString()

        tvReceiptBookingId.text = "#$bookingId"
        tvReceiptCourtName.text = courtName
        tvReceiptMethod.text = paymentName
        tvReceiptCode.text = randomCode
        tvReceiptTotal.text = "Rp ${String.format("%,.0f", grandTotal)}"

        val launcherGaleri = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                selectedImageUri = result.data?.data
                ivReceiptSelectedProof.setImageURI(selectedImageUri)
            }
        }

        btnDownloadReceipt.setOnClickListener {
            val bitmapStruk = ambilBitmapDariView(layoutCardStruk)
            if (bitmapStruk != null) {
                simpanBitmapKeGaleri(bitmapStruk, "Struk_Deuce_ID_$bookingId")
            } else {
                Toast.makeText(this, "Gagal memproses gambar struk !", Toast.LENGTH_SHORT).show()
            }
        }

        btnReceiptSelectImage.setOnClickListener {
            val intentGaleri = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            launcherGaleri.launch(intentGaleri)
        }

        btnReceiptSubmitProof.setOnClickListener {
            if (selectedImageUri == null) {
                Toast.makeText(this, "Pilih foto bukti transfer dulu !", Toast.LENGTH_SHORT).show()
            } else {
                prosesUploadLangsungDariStruk()
            }
        }
    }

    private fun ambilBitmapDariView(view: View): Bitmap? {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun simpanBitmapKeGaleri(bitmap: Bitmap, namaFile: String) {
        val namaLengkap = "$namaFile.jpg"
        var outputStream: OutputStream? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, namaLengkap)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/DeuceReceipts")
                }
                val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (imageUri != null) {
                    outputStream = resolver.openOutputStream(imageUri)
                }
            } else {
                val direktoriGambar = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_PICTURES)
                val folderApp = File(direktoriGambar, "DeuceReceipts")
                if (!folderApp.exists()) folderApp.mkdirs()
                val fileGambar = File(folderApp, namaLengkap)
                outputStream = FileOutputStream(fileGambar)
            }

            if (outputStream != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.flush()
                outputStream.close()
                Toast.makeText(this, "Struk beneran kesimpen di Galeri Perangkat!", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal simpan gambar: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun prosesUploadLangsungDariStruk() {
        val uriAman = selectedImageUri ?: return
        val fileGambar = uriToFile(uriAman, this)

        val requestFile = fileGambar.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val bodyMultipart = MultipartBody.Part.createFormData("file", fileGambar.name, requestFile)

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ReceiptUploadApiService::class.java)

        val sharedPref = getSharedPreferences("DeucePref", Context.MODE_PRIVATE)
        val tokenLokal = sharedPref.getString("token", "") ?: ""
        val tokenBearer = "Bearer $tokenLokal"

        apiService.uploadBukti(bookingId, tokenBearer, bodyMultipart).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ReceiptSimulationActivity, "Bukti Berhasil Diupload!", Toast.LENGTH_LONG).show()

                    val intentHome = Intent(this@ReceiptSimulationActivity, HomeActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    startActivity(intentHome)
                    finish()
                } else {
                    Toast.makeText(this@ReceiptSimulationActivity, "Gagal upload, error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@ReceiptSimulationActivity, "Koneksi terputus: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun uriToFile(uri: Uri, context: Context): File {
        val contentResolver = context.contentResolver
        val fileTemp = File(context.cacheDir, "bukti_pembayaran.jpg")
        val inputStream = contentResolver.openInputStream(uri)
        val fileOutputStream = FileOutputStream(fileTemp)
        inputStream?.use { input -> fileOutputStream.use { output -> input.copyTo(output) } }
        return fileTemp
    }
}