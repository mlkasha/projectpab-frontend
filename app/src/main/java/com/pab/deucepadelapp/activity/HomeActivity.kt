package com.pab.deucepadelapp.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.pab.deucepadelapp.R
import com.pab.deucepadelapp.adapter.CourtAdapter
import com.pab.deucepadelapp.api.ApiClient
import com.pab.deucepadelapp.model.CourtItem
import com.pab.deucepadelapp.model.CourtResponse
import android.content.res.ColorStateList
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.random.Random

class HomeActivity : AppCompatActivity() {

    private lateinit var rvCourts: RecyclerView
    private lateinit var courtAdapter: CourtAdapter
    private lateinit var etSearch: TextInputEditText
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var tvHomeUserName: TextView

    private lateinit var btnFilterAll: MaterialButton
    private lateinit var btnFilterNear: MaterialButton
    private lateinit var btnFilterRated: MaterialButton

    private lateinit var menuExplore: LinearLayout
    private lateinit var menuBookings: LinearLayout
    private lateinit var menuHistory: LinearLayout
    private lateinit var menuProfile: LinearLayout

    private var originalCourtList: List<CourtItem> = arrayListOf()
    private var shuffledCourtList: List<CourtItem> = arrayListOf()
    private var nearMeCourtList: List<CourtItem> = arrayListOf()

    private val requestLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            checkGpsSystemSettings()
        } else {
            Toast.makeText(this, "Aplikasi membutuhkan izin lokasi untuk mencari lapangan terdekat.", Toast.LENGTH_LONG).show()
        }
    }

    private val gpsSettingLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            getLastUserLocation()
        } else {
            Toast.makeText(this, "Harap aktifkan GPS Anda untuk melihat lapangan terdekat.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_home)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        tvHomeUserName = findViewById(R.id.tvHomeUserName)
        etSearch = findViewById(R.id.etSearch)
        rvCourts = findViewById(R.id.rvCourts)

        btnFilterAll = findViewById(R.id.btnFilterAll)
        btnFilterNear = findViewById(R.id.btnFilterNear)
        btnFilterRated = findViewById(R.id.btnFilterRated)

        menuExplore = findViewById(R.id.menuExplore)
        menuBookings = findViewById(R.id.menuBookings)
        menuHistory = findViewById(R.id.menuHistory)
        menuProfile = findViewById(R.id.menuProfile)

        val sharedPref = getSharedPreferences("DeucePref", Context.MODE_PRIVATE)
        val intentName = intent.getStringExtra("USER_NAME")

        if (intentName != null) {
            sharedPref.edit().putString("USER_NAME", intentName).apply()
        }

        val cachedUserName = sharedPref.getString("USER_NAME", "Mark")
        tvHomeUserName.text = "Hello, $cachedUserName!"

        rvCourts.layoutManager = LinearLayoutManager(this)
        rvCourts.isNestedScrollingEnabled = false

        courtAdapter = CourtAdapter(arrayListOf())
        rvCourts.adapter = courtAdapter

        getCourtsFromApi()
        setupBottomNavigationLogic()

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterData(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnFilterAll.setOnClickListener {
            courtAdapter.updateData(shuffledCourtList)
            updateButtonVisuals(selectedMode = "all")
        }

        btnFilterNear.setOnClickListener {
            checkLocationPermissionAndFetch()
        }

        btnFilterRated.setOnClickListener {
            val topRatedCourts = originalCourtList.sortedByDescending { court -> court.rate }
            courtAdapter.updateData(topRatedCourts)
            updateButtonVisuals(selectedMode = "rated")
        }
    }

    private fun setupBottomNavigationLogic() {
        menuExplore.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }

        menuBookings.setOnClickListener {
            startActivity(
                Intent(this, BookingActivity::class.java)
            )
            overridePendingTransition(0, 0)
        }

        menuHistory.setOnClickListener {
            startActivity(
                Intent(this, BookingHistoryActivity::class.java)
            )
            overridePendingTransition(0, 0)
        }

        menuProfile.setOnClickListener {
            startActivity(
                Intent(this, ProfileActivity::class.java)
            )
            overridePendingTransition(0, 0)
        }
    }

    private fun updateButtonVisuals(selectedMode: String) {
        val activeBg = ColorStateList.valueOf(Color.parseColor("#D4F03B"))
        val inactiveBg = ColorStateList.valueOf(Color.parseColor("#EEEEEE"))
        val activeText = Color.parseColor("#222222")
        val inactiveText = Color.parseColor("#555555")

        when (selectedMode) {
            "all" -> {
                btnFilterAll.backgroundTintList = activeBg
                btnFilterAll.setTextColor(activeText)
                btnFilterNear.backgroundTintList = inactiveBg
                btnFilterNear.setTextColor(inactiveText)
                btnFilterRated.backgroundTintList = inactiveBg
                btnFilterRated.setTextColor(inactiveText)
            }
            "near" -> {
                btnFilterAll.backgroundTintList = inactiveBg
                btnFilterAll.setTextColor(inactiveText)
                btnFilterNear.backgroundTintList = activeBg
                btnFilterNear.setTextColor(activeText)
                btnFilterRated.backgroundTintList = inactiveBg
                btnFilterRated.setTextColor(inactiveText)
            }
            "rated" -> {
                btnFilterAll.backgroundTintList = inactiveBg
                btnFilterAll.setTextColor(inactiveText)
                btnFilterNear.backgroundTintList = inactiveBg
                btnFilterNear.setTextColor(inactiveText)
                btnFilterRated.backgroundTintList = activeBg
                btnFilterRated.setTextColor(activeText)
            }
        }
    }

    private fun checkLocationPermissionAndFetch() {
        val hasFineLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarseLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (hasFineLocation || hasCoarseLocation) {
            checkGpsSystemSettings()
        } else {
            requestLocationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    private fun checkGpsSystemSettings() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build()
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            getLastUserLocation()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution.intentSender).build()
                    gpsSettingLauncher.launch(intentSenderRequest)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Toast.makeText(this, "Gagal memicu setelan GPS.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getLastUserLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    getNearMeCourtsFromApi(location.latitude, location.longitude)
                } else {
                    val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                        .setMinUpdateIntervalMillis(500)
                        .build()

                    fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
                        override fun onLocationResult(locationResult: LocationResult) {
                            val newLocation = locationResult.lastLocation
                            if (newLocation != null) {
                                getNearMeCourtsFromApi(newLocation.latitude, newLocation.longitude)
                                fusedLocationClient.removeLocationUpdates(this)
                            } else {
                                Toast.makeText(this@HomeActivity, "Gagal mendapatkan koordinat GPS.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }, mainLooper)
                }
            }
        } catch (e: SecurityException) {
            Toast.makeText(this, "Gagal memproses lokasi: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCourtsFromApi() {
        ApiClient.instance.getCourts().enqueue(object : Callback<CourtResponse> {
            override fun onResponse(call: Call<CourtResponse>, response: Response<CourtResponse>) {
                if (response.isSuccessful) {
                    val courtResponse = response.body()
                    if (courtResponse != null && courtResponse.success) {
                        originalCourtList = courtResponse.data.map { court ->
                            if (court.rate == 0.0) {
                                val randomRate = String.format("%.1f", Random.nextDouble(4.2, 5.0)).replace(",", ".").toDouble()
                                court.copy(rate = randomRate)
                            } else {
                                court
                            }
                        }
                        shuffledCourtList = originalCourtList.shuffled()
                        courtAdapter.updateData(shuffledCourtList)
                        updateButtonVisuals(selectedMode = "all")
                    }
                } else {
                    Toast.makeText(this@HomeActivity, "Gagal memuat data dari server", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CourtResponse>, t: Throwable) {
                Toast.makeText(this@HomeActivity, "Error Koneksi: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getNearMeCourtsFromApi(latitude: Double, longitude: Double) {
        ApiClient.instance.getNearMeCourts(latitude, longitude).enqueue(object : Callback<CourtResponse> {
            override fun onResponse(call: Call<CourtResponse>, response: Response<CourtResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val rawCourts = response.body()!!.data
                    nearMeCourtList = rawCourts.map { rawCourt ->
                        val matchedCourt = originalCourtList.find { it.id == rawCourt.id }
                        if (rawCourt.rate == 0.0 && matchedCourt != null) {
                            rawCourt.copy(rate = matchedCourt.rate)
                        } else if (rawCourt.rate == 0.0) {
                            rawCourt.copy(rate = 4.5)
                        } else {
                            rawCourt
                        }
                    }
                    courtAdapter.updateData(nearMeCourtList)
                    updateButtonVisuals(selectedMode = "near")
                } else {
                    Toast.makeText(this@HomeActivity, "Gagal mendapatkan data lokasi terdekat", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CourtResponse>, t: Throwable) {
                Toast.makeText(this@HomeActivity, "Error Koneksi Near Me: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filterData(query: String) {
        val currentModeList = if (btnFilterNear.backgroundTintList == ColorStateList.valueOf(Color.parseColor("#D4F03B"))) {
            nearMeCourtList
        } else if (btnFilterRated.backgroundTintList == ColorStateList.valueOf(Color.parseColor("#D4F03B"))) {
            originalCourtList.sortedByDescending { it.rate }
        } else {
            shuffledCourtList
        }

        val filteredList = currentModeList.filter { court ->
            court.name.contains(query, ignoreCase = true) ||
                    court.description.contains(query, ignoreCase = true)
        }
        courtAdapter.updateData(filteredList)
    }
}