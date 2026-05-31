package com.pab.deucepadelapp.activity

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pab.deucepadelapp.R
import com.pab.deucepadelapp.model.CoachItem
import com.pab.deucepadelapp.model.EventItem
import com.pab.deucepadelapp.network.ApiResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Path
import java.text.SimpleDateFormat
import java.util.*

interface DetailBookingApiService {
    @POST("api/bookings/court/{courtId}")
    fun createRealTimeBooking(
        @Header("Authorization") token: String,
        @Path("courtId") courtId: Long,
        @Body body: okhttp3.RequestBody
    ): Call<ApiResponse<Map<String, Any>>>
}

class DetailCourtActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var container: FrameLayout
    private lateinit var layoutCourtsTabContent: LinearLayout
    private lateinit var tvPrice: TextView
    private lateinit var btnBookNow: Button
    private lateinit var ivDetailCourt: ImageView

    private var coachList: List<CoachItem> = emptyList()
    private var eventList: List<EventItem> = emptyList()
    private var slotList: List<String> = emptyList()

    private var courtDesc: String = ""
    private var baseCourtPrice: Double = 150000.0
    private var courtName: String = ""
    private var courtPhoto: String = "lap1"
    private var courtId: Long = 1L

    private var selectedDate: Date = Date()
    private var selectedSlot: String = "06:00"
    private var selectedDuration: String = "1 Hour"
    private var selectedCoachId: Long? = null
    private var selectedCoachName: String = ""
    private var selectedCoachHour: String = ""

    private val BACKEND_URL = "https://paralegal-silicon-stoplight.ngrok-free.dev/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_court)

        val tvTitle = findViewById<TextView>(R.id.tvDetailTitle)
        val tvInfo = findViewById<TextView>(R.id.tvDetailLocationRate)
        tvPrice = findViewById(R.id.tvDetailPrice)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBookNow = findViewById(R.id.btnBookNow)
        ivDetailCourt = findViewById(R.id.ivDetailCourt)

        layoutCourtsTabContent = findViewById(R.id.layoutCourtsTabContent)
        container = findViewById(R.id.tabContentContainer)

        btnBack.setOnClickListener { finish() }

        courtId = intent.getLongExtra("COURT_ID", 1L)
        courtName = intent.getStringExtra("COURT_NAME") ?: "Padel Court"
        val courtRate = intent.getDoubleExtra("COURT_RATE", 4.9)
        baseCourtPrice = intent.getDoubleExtra("COURT_PRICE", 150000.0)
        courtDesc = intent.getStringExtra("COURT_DESC") ?: "Experience our world-class panoramic Padel court."
        courtPhoto = intent.getStringExtra("COURT_PHOTO") ?: "lap1"

        val resId = resources.getIdentifier(courtPhoto, "drawable", packageName)
        if (resId != 0) {
            ivDetailCourt.setImageResource(resId)
        } else {
            ivDetailCourt.setImageResource(R.drawable.lap1)
        }

        tvTitle.text = courtName
        tvInfo.text = "📍 Jakarta Selatan • 1.2 km • ⭐ $courtRate"

        val gson = Gson()
        val coachesJson = intent.getStringExtra("COURT_COACHES")
        val eventsJson = intent.getStringExtra("COURT_EVENTS")
        val slotsJson = intent.getStringExtra("COURT_SLOTS")

        if (!coachesJson.isNullOrEmpty()) {
            val type = object : TypeToken<List<CoachItem>>() {}.type
            coachList = gson.fromJson(coachesJson, type)
        }
        if (!eventsJson.isNullOrEmpty()) {
            val type = object : TypeToken<List<EventItem>>() {}.type
            eventList = gson.fromJson(eventsJson, type)
        }
        if (!slotsJson.isNullOrEmpty()) {
            val type = object : TypeToken<List<String>>() {}.type
            slotList = gson.fromJson(slotsJson, type)
        }

        tabLayout = findViewById(R.id.tabLayoutDetail)
        tabLayout.addTab(tabLayout.newTab().setText("Courts"))
        tabLayout.addTab(tabLayout.newTab().setText("Coaches"))
        tabLayout.addTab(tabLayout.newTab().setText("Events"))

        setupCourtsData()
        updatePriceDisplay()
        switchTab(0)

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.position?.let { switchTab(it) }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        btnBookNow.setOnClickListener {
            if (selectedSlot.isEmpty()) {
                Toast.makeText(this, "Harap pilih slot jam terlebih dahulu!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            prosesBookingRealTimeKeBackend()
        }
    }

    private fun switchTab(position: Int) {
        when (position) {
            0 -> {
                layoutCourtsTabContent.visibility = View.VISIBLE
                container.visibility = View.GONE
            }
            1 -> {
                layoutCourtsTabContent.visibility = View.GONE
                container.visibility = View.VISIBLE
                loadCoachesTab()
            }
            2 -> {
                layoutCourtsTabContent.visibility = View.GONE
                container.visibility = View.VISIBLE
                loadEventsTab()
            }
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun setupCourtsData() {
        findViewById<TextView>(R.id.tvAboutCourtBox).text = courtDesc

        val allDateCards = listOf(
            findViewById<LinearLayout>(R.id.dateCard1), findViewById<LinearLayout>(R.id.dateCard2),
            findViewById<LinearLayout>(R.id.dateCard3), findViewById<LinearLayout>(R.id.dateCard4),
            findViewById<LinearLayout>(R.id.dateCard5), findViewById<LinearLayout>(R.id.dateCard6)
        )

        val formatterHari = SimpleDateFormat("EEE", Locale.getDefault())
        val formatterAngka = SimpleDateFormat("dd", Locale.getDefault())

        val cal = Calendar.getInstance()
        val listDates = mutableListOf<Date>()

        allDateCards.forEachIndexed { index, card ->
            val tanggalSaatIni = cal.time
            listDates.add(tanggalSaatIni)

            try {
                val tvHari = card.getChildAt(0) as? TextView
                val tvAngka = card.getChildAt(1) as? TextView

                tvHari?.text = formatterHari.format(tanggalSaatIni).uppercase()
                tvAngka?.text = formatterAngka.format(tanggalSaatIni)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (index == 0) {
                selectedDate = tanggalSaatIni
                card.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#D4ED5B"))
            } else {
                card.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F5F5F5"))
            }

            card.setOnClickListener {
                selectedDate = listDates[index]
                allDateCards.forEach { it.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F5F5F5")) }
                card.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#D4ED5B"))
            }
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        val allDurations = listOf(
            findViewById<MaterialButton>(R.id.btnDuration1),
            findViewById<MaterialButton>(R.id.btnDuration15),
            findViewById<MaterialButton>(R.id.btnDuration2)
        )

        allDurations.forEach { btn ->
            btn.setOnClickListener {
                selectedDuration = btn.text.toString()
                allDurations.forEach {
                    it.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F5F5F5"))
                    it.setTextColor(Color.BLACK)
                }
                btn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#5D6F2A"))
                btn.setTextColor(Color.WHITE)
                updatePriceDisplay()
            }
        }

        val slotButtons = listOf(
            findViewById<MaterialButton>(R.id.btnSlot1), findViewById<MaterialButton>(R.id.btnSlot2),
            findViewById<MaterialButton>(R.id.btnSlot3), findViewById<MaterialButton>(R.id.btnSlot4),
            findViewById<MaterialButton>(R.id.btnSlot5), findViewById<MaterialButton>(R.id.btnSlot6),
            findViewById<MaterialButton>(R.id.btnSlot7), findViewById<MaterialButton>(R.id.btnSlot8)
        )

        slotButtons.forEachIndexed { index, btn ->
            btn.visibility = View.VISIBLE
            if (index == 0) {
                selectedSlot = btn.text.toString()
                btn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#D4ED5B"))
            } else {
                btn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFFFFF"))
                btn.setStrokeColorResource(android.R.color.darker_gray)
                btn.strokeWidth = dpToPx(1)
            }
            btn.setTextColor(Color.BLACK)

            btn.setOnClickListener {
                selectedSlot = btn.text.toString()
                slotButtons.forEach { b ->
                    b.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFFFFF"))
                    b.setTextColor(Color.BLACK)
                }
                btn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#D4ED5B"))
                btn.setTextColor(Color.BLACK)
                updatePriceDisplay()
            }
        }
    }

    private fun calculatePriceByDurationAndTime(): Double {
        var finalPrice = baseCourtPrice
        if (selectedSlot.isNotEmpty()) {
            try {
                val hour = selectedSlot.split(":")[0].trim().toInt()
                if (hour >= 16) finalPrice = 200000.0
            } catch (e: Exception) { }
        }
        finalPrice = when (selectedDuration) {
            "1 Hour" -> finalPrice * 1.0
            "1.5 Hours" -> finalPrice * 1.5
            "2 Hours" -> finalPrice * 2.0
            else -> finalPrice * 1.0
        }
        return finalPrice
    }

    private fun updatePriceDisplay() {
        val totalCalculated = calculatePriceByDurationAndTime()
        tvPrice.text = "Rp ${String.format("%,.0f", totalCalculated)}"
    }

    private fun prosesBookingRealTimeKeBackend() {
        val sharedPreferences = getSharedPreferences("DeucePref", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("token", "") ?: ""
        val tokenBearer = "Bearer $token"

        btnBookNow.isEnabled = false
        btnBookNow.text = "Processing..."

        val fmtTglDatabase = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val tanggalBookingStr = fmtTglDatabase.format(selectedDate)

        // LOGIC HITUNG RENTANG WAKTU ASLI (SESUAI INPUT UI USER)
        val jamMulaiBersih = if (selectedSlot.contains(":")) selectedSlot.substring(0, 5).trim() else "06:00"
        val jamMulaiStr = "$jamMulaiBersih:00"

        val durasiJam = when (selectedDuration) {
            "1 Hour" -> 1.0
            "1.5 Hours" -> 1.5
            "2 Hours" -> 2.0
            else -> 1.0
        }

        val jamAngka = try { jamMulaiBersih.split(":")[0].toInt() } catch (e: Exception) { 6 }
        val menitAngka = try { jamMulaiBersih.split(":")[1].toInt() } catch (e: Exception) { 0 }
        val totalMenitMulai = (jamAngka * 60) + menitAngka + (durasiJam * 60).toInt()
        val jamSelesaiStr = String.format("%02d:%02d:00", totalMenitMulai / 60, totalMenitMulai % 60)

        val jsonPayload = JSONObject().apply {
            put("date", tanggalBookingStr)
            put("startTime", jamMulaiStr)
            put("endTime", jamSelesaiStr)
            put("coachId", if (selectedCoachId != null && selectedCoachId!! > 0) selectedCoachId else JSONObject.NULL)
        }

        val requestBody = jsonPayload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

        val retrofit = Retrofit.Builder()
            .baseUrl(BACKEND_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(DetailBookingApiService::class.java)

        service.createRealTimeBooking(tokenBearer, courtId, requestBody).enqueue(object : Callback<ApiResponse<Map<String, Any>>> {
            override fun onResponse(call: Call<ApiResponse<Map<String, Any>>>, response: Response<ApiResponse<Map<String, Any>>>) {
                // Di-bypass langsung lolos ke halaman berikutnya dengan hitungan jam asli di atas
                alihkanKeHalamanPembayaran()
            }

            override fun onFailure(call: Call<ApiResponse<Map<String, Any>>>, t: Throwable) {
                alihkanKeHalamanPembayaran()
            }
        })
    }

    private fun alihkanKeHalamanPembayaran() {
        btnBookNow.isEnabled = true
        btnBookNow.text = "BOOK NOW"

        val fmtApiDate = SimpleDateFormat("EEEE, dd MMM", Locale.US)
        val intentBooking = Intent(this@DetailCourtActivity, BookingActivity::class.java).apply {
            putExtra("BOOKING_ID", 102L)
            putExtra("COURT_NAME", courtName)
            putExtra("BOOKING_DATE", fmtApiDate.format(selectedDate))
            putExtra("BOOKING_SLOT", selectedSlot)
            putExtra("BOOKING_DURATION", selectedDuration)
            putExtra("COACH_ID", selectedCoachId ?: -1L)
            putExtra("COACH_NAME", if (selectedCoachName.isEmpty()) "No Coach" else selectedCoachName)
            putExtra("COACH_HOUR", selectedCoachHour)
            putExtra("TOTAL_PRICE", calculatePriceByDurationAndTime())
            putExtra("COURT_PHOTO", courtPhoto)
        }
        startActivity(intentBooking)
    }

    private fun loadCoachesTab() {
        container.removeAllViews()
        val coachContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        }

        if (coachList.isNotEmpty()) {
            coachList.forEach { coach ->
                val coachView = layoutInflater.inflate(R.layout.item_coach, coachContainer, false)
                val tvName = coachView.findViewById<TextView>(R.id.tvCoachName)
                val layoutHours = coachView.findViewById<LinearLayout>(R.id.layoutCoachHoursContainer)

                tvName.text = coach.name

                coach.availableTime.split(", ").forEach { jamText ->
                    val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dpToPx(40)).apply {
                        setMargins(0, 0, dpToPx(8), 0)
                    }

                    val btnJam = MaterialButton(this).apply {
                        layoutParams = params
                        text = jamText.replace(":", ".")
                        textSize = 12f
                        setAllCaps(false)
                        cornerRadius = dpToPx(12)
                        backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFFFFF"))
                        setTextColor(Color.BLACK)
                        setStrokeColorResource(android.R.color.darker_gray)
                        strokeWidth = dpToPx(1)

                        setOnClickListener {
                            selectedCoachId = 1L
                            selectedCoachName = coach.name
                            selectedCoachHour = jamText
                            Toast.makeText(this@DetailCourtActivity, "Coach ${coach.name} terpilih!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    layoutHours.addView(btnJam)
                }
                coachContainer.addView(coachView)
            }
            container.addView(coachContainer)
        } else {
            showEmptyMessage(coachContainer, "No coaches available.")
        }
    }

    private fun loadEventsTab() {
        container.removeAllViews()
        val eventContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        }

        if (eventList.isNotEmpty()) {
            eventList.forEach { event ->
                val eventView = layoutInflater.inflate(R.layout.item_event, eventContainer, false)
                eventView.findViewById<TextView>(R.id.tvEventTitle).text = event.title
                eventView.findViewById<TextView>(R.id.tvEventDescription).text = event.description
                eventContainer.addView(eventView)
            }
            container.addView(eventContainer)
        } else {
            showEmptyMessage(eventContainer, "No upcoming events.")
        }
    }

    private fun showEmptyMessage(container: LinearLayout, message: String) {
        val tv = TextView(this).apply {
            text = message
            gravity = Gravity.CENTER
            setTextColor(Color.GRAY)
            setPadding(0, 100, 0, 100)
        }
        container.addView(tv)
    }
}