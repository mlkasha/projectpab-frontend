package com.pab.deucepadelapp.model

data class BookingData(
    val id: Long? = null,
    val courtName: String?,
    val courtCode: String?,
    val scheduleDate: String?,
    val startTime: String?,
    val endTime: String?,
    val duration: Double?,
    val totalPrice: Double?,
    val status: String?,
    val courtPhoto: String? = "lap1"
)