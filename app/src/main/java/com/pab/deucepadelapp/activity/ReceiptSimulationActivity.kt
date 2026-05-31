package com.pab.deucepadelapp.activity

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
import androidx.appcompat.app.AppCompatActivity
import com.pab.deucepadelapp.R
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReceiptSimulationActivity : AppCompatActivity() {

    private lateinit var ivReceiptSelectedProof: ImageView
    private lateinit var btnReceiptSelectImage: Button
    private lateinit var btnReceiptSubmitProof: Button

    private var bookingId: Long = 10L

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

        ivReceiptSelectedProof.visibility = View.GONE
        btnReceiptSelectImage.visibility = View.GONE

        btnReceiptSubmitProof.text = "SELESAI & LIHAT BOOKING"

        bookingId = intent.getLongExtra("BOOKING_ID", 10L)
        val courtName = intent.getStringExtra("COURT_NAME") ?: "Padel Court"
        val paymentName = intent.getStringExtra("PAYMENT_NAME") ?: "BCA Virtual Account"
        val grandTotal = intent.getDoubleExtra("GRAND_TOTAL", 152500.0)
        val courtPhoto = intent.getStringExtra("COURT_PHOTO") ?: "lap1"

        val randomCode = (100000000000..999999999999).random().toString()

        // STRUK TETAP AMAN TIDAK BERUBAH
        tvReceiptBookingId.text = "#$bookingId"
        tvReceiptCourtName.text = courtName
        tvReceiptMethod.text = paymentName
        tvReceiptCode.text = randomCode
        tvReceiptTotal.text = "Rp ${String.format("%,.0f", grandTotal)}"

        btnDownloadReceipt.setOnClickListener {
            val bitmapStruk = ambilBitmapDariView(layoutCardStruk)
            if (bitmapStruk != null) {
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                simpanBitmapKeGaleri(bitmapStruk, "Struk_Deuce_ID_${bookingId}_$timestamp")
            } else {
                Toast.makeText(this, "Gagal memproses gambar struk !", Toast.LENGTH_SHORT).show()
            }
        }

        // Tombol dialihkan langsung ke halaman utama booking tanpa crash Retrofit
        btnReceiptSubmitProof.setOnClickListener {
            Toast.makeText(this, "Booking sukses tersimpan!", Toast.LENGTH_SHORT).show()

            // Alihkan instan tanpa animasi jeda patah
            val intentBookings = Intent(this, MyBookingsActivity::class.java)
            intentBookings.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intentBookings)
            overridePendingTransition(0, 0)
            finish()
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
                Toast.makeText(this, "Struk disimpan ke galeri perangkat!", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal simpan gambar: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}