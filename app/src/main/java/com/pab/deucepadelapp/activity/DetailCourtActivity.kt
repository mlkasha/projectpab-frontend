package com.pab.deucepadelapp.activity

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
import java.text.SimpleDateFormat
import java.util.*

class DetailCourtActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var container: FrameLayout
    private lateinit var layoutCourtsTabContent: LinearLayout
    private lateinit var tvPrice: TextView
    private lateinit var btnBookNow: Button
    private lateinit var ivDetailCourt: ImageView // 🛠️ FIX: Sesuai dengan ID XML kamu

    private var coachList: List<CoachItem> = emptyList()
    private var eventList: List<EventItem> = emptyList()
    private var courtDesc: String = ""
    private var baseCourtPrice: Double = 150000.0
    private var courtName: String = ""
    private var courtPhoto: String = "lap1" // Penampung string nama gambar

    private var selectedDate: Date = Date()
    private var selectedSlot: String = "06:00"
    private var selectedDuration: String = "1.5 Hours"
    private var selectedCoachId: Long? = null
    private var selectedCoachName: String = ""
    private var selectedCoachHour: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_court)

        val tvTitle = findViewById<TextView>(R.id.tvDetailTitle)
        val tvInfo = findViewById<TextView>(R.id.tvDetailLocationRate)
        tvPrice = findViewById(R.id.tvDetailPrice)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBookNow = findViewById(R.id.btnBookNow)
        ivDetailCourt = findViewById(R.id.ivDetailCourt) // 🛠️ FIX: Mengisi komponen gambar asli dari XML kamu

        layoutCourtsTabContent = findViewById(R.id.layoutCourtsTabContent)
        container = findViewById(R.id.tabContentContainer)

        btnBack.setOnClickListener { finish() }

        courtName = intent.getStringExtra("COURT_NAME") ?: "Padel Court"
        val courtRate = intent.getDoubleExtra("COURT_RATE", 4.9)
        baseCourtPrice = intent.getDoubleExtra("COURT_PRICE", 150000.0)
        courtDesc = intent.getStringExtra("COURT_DESC") ?: "Experience our world-class panoramic Padel court."

        // 🛠️ FIX: Tangkap data nama gambar dari halaman utama (HomeActivity / Adapter)
        courtPhoto = intent.getStringExtra("COURT_PHOTO") ?: "lap1"

        // 🛠️ FIX: Tampilkan gambar lapangan yang sesuai di halaman Detail
        val resId = resources.getIdentifier(courtPhoto, "drawable", packageName)
        if (resId != 0) {
            ivDetailCourt.setImageResource(resId)
        } else {
            ivDetailCourt.setImageResource(R.drawable.lap1) // Gambar cadangan jika error
        }

        tvTitle.text = courtName
        tvInfo.text = "📍 Jakarta Selatan • 1.2 km • ⭐ $courtRate"

        updatePriceDisplay()

        val gson = Gson()
        val coachesJson = intent.getStringExtra("COURT_COACHES")
        val eventsJson = intent.getStringExtra("COURT_EVENTS")

        if (!coachesJson.isNullOrEmpty()) {
            val type = object : TypeToken<List<CoachItem>>() {}.type
            coachList = gson.fromJson(coachesJson, type)
        }
        if (!eventsJson.isNullOrEmpty()) {
            val type = object : TypeToken<List<EventItem>>() {}.type
            eventList = gson.fromJson(eventsJson, type)
        }

        tabLayout = findViewById(R.id.tabLayoutDetail)
        tabLayout.addTab(tabLayout.newTab().setText("Courts"))
        tabLayout.addTab(tabLayout.newTab().setText("Coaches"))
        tabLayout.addTab(tabLayout.newTab().setText("Events"))

        setupCourtsData()
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

            val fmtApiDate = SimpleDateFormat("EEEE, dd MMM", Locale.US)
            val intentBooking = Intent(this, BookingActivity::class.java).apply {
                putExtra("COURT_NAME", courtName)
                putExtra("BOOKING_DATE", fmtApiDate.format(selectedDate))
                putExtra("BOOKING_SLOT", selectedSlot)
                putExtra("BOOKING_DURATION", selectedDuration)
                putExtra("COACH_ID", selectedCoachId ?: -1L)
                putExtra("COACH_NAME", if (selectedCoachName.isEmpty()) "No Coach" else selectedCoachName)
                putExtra("COACH_HOUR", selectedCoachHour)
                putExtra("TOTAL_PRICE", calculatePriceByDurationAndTime())

                // 🛠️ FIX: Kirim estafet data gambar lapangan ke BookingActivity
                putExtra("COURT_PHOTO", courtPhoto)
            }
            startActivity(intentBooking)
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

        val cal = Calendar.getInstance()
        val listDates = mutableListOf<Date>()
        for (i in 0 until 6) {
            listDates.add(cal.time)
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        allDateCards.forEachIndexed { index, card ->
            card.setOnClickListener {
                selectedDate = listDates[index]
                allDateCards.forEach { it.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F5F5F5")) }
                card.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#D4ED5B"))
            }
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

        slotButtons.forEach { btn ->
            btn.setOnClickListener {
                selectedSlot = btn.text.toString()

                slotButtons.forEach {
                    it.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#EAEAEA"))
                    it.setTextColor(Color.BLACK)
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
                val hour = selectedSlot.split(":")[0].toInt()
                if (hour >= 16) finalPrice = 200000.0
            } catch (e: Exception) { }
        }
        finalPrice = when (selectedDuration) {
            "1 Hour" -> finalPrice * 1.0
            "1.5 Hours" -> finalPrice * 1.5
            "2 Hours" -> finalPrice * 2.0
            else -> finalPrice * 1.5
        }
        return finalPrice
    }

    private fun updatePriceDisplay() {
        val totalCalculated = calculatePriceByDurationAndTime()
        tvPrice.text = "Rp ${String.format("%,.0f", totalCalculated)}"
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
                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        dpToPx(40)
                    ).apply {
                        setMargins(0, 0, dpToPx(8), 0)
                    }

                    val btnJam = MaterialButton(this).apply {
                        layoutParams = params
                        text = jamText.replace(":", ".")
                        textSize = 12f
                        setAllCaps(false)
                        cornerRadius = dpToPx(12)
                        insetTop = 0
                        insetBottom = 0
                        setPadding(dpToPx(14), 0, dpToPx(14), 0)
                        backgroundTintList = ColorStateList.valueOf(Color.parseColor("#EAEAEA"))
                        setTextColor(Color.BLACK)

                        setOnClickListener {
                            selectedCoachId = try {
                                val fields = coach.javaClass.declaredFields
                                val idField = fields.firstOrNull { it.name.lowercase().contains("id") }
                                if (idField != null) {
                                    idField.isAccessible = true
                                    val value = idField.get(coach)
                                    if (value is Number) value.toLong() else value.toString().toLongOrNull() ?: -1L
                                } else { -1L }
                            } catch (e: Exception) { -1L }

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

                val tvTitle = eventView.findViewById<TextView>(R.id.tvEventTitle)
                val tvDesc = eventView.findViewById<TextView>(R.id.tvEventDescription)

                tvTitle.text = event.title
                tvDesc.text = event.description

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