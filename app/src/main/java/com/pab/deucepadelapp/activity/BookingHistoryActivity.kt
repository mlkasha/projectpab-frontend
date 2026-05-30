package com.pab.deucepadelapp.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pab.deucepadelapp.R

class BookingHistoryActivity : AppCompatActivity() {

    private lateinit var rvBookingHistory: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_booking_history)

        initViews()
        setupRecyclerView()
        loadBookingHistoryData()
    }

    private fun initViews() {
        rvBookingHistory = findViewById(R.id.rvBookingHistory)
    }

    private fun setupRecyclerView() {
        rvBookingHistory.layoutManager = LinearLayoutManager(this)
    }

    private fun loadBookingHistoryData() {
        Toast.makeText(this, "Memuat riwayat booking...", Toast.LENGTH_SHORT).show()
    }
}