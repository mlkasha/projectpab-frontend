package com.pab.deucepadelapp.model

import com.google.gson.annotations.SerializedName

data class CourtResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: List<CourtItem>
)

data class CourtItem(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("price_per_hour", alternate = ["pricePerHour"])
    val pricePerHour: Double,

    @SerializedName("description")
    val description: String,

    @SerializedName("photo_url", alternate = ["photoUrl"])
    val photoUrl: String?,

    @SerializedName("is_active", alternate = ["isActive", "available"])
    val available: Boolean,

    @SerializedName("distance")
    val distance: Double,

    @SerializedName("rate")
    val rate: Double,

    @SerializedName("latitude")
    val latitude: Double,

    @SerializedName("longitude")
    val longitude: Double,

    @SerializedName("coaches")
    val coaches: List<CoachItem>?,

    @SerializedName("events")
    val events: List<EventItem>?,

    @SerializedName("availableSlots")
    val availableSlots: List<String>?
)

data class CoachItem(
    @SerializedName("name") val name: String,
    @SerializedName("availableTime") val availableTime: String
)

data class EventItem(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String
)