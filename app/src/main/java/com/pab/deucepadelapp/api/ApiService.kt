package com.pab.deucepadelapp.api

import retrofit2.Call
import retrofit2.http.*
import okhttp3.MultipartBody
import com.pab.deucepadelapp.model.*

interface ApiService {

    @POST("api/auth/register")
    fun register(@Body request: RegisterRequest): Call<AuthResponse>

    @POST("api/auth/login")
    fun login(@Body request: LoginRequest): Call<AuthResponse>

    @GET("api/auth/me")
    fun getMyProfile(@Header("Authorization") token: String): Call<ProfileResponse>

    @POST("api/auth/logout")
    fun logout(@Header("Authorization") token: String): Call<BaseResponse>

    @GET("api/courts")
    fun getCourts(): Call<CourtResponse>

    @GET("api/courts/{id}")
    fun getCourtDetail(@Path("id") courtId: Int): Call<CourtDetailResponse>

    @GET("api/courts/{id}/slots")
    fun getCourtSlots(
        @Path("id") courtId: Int,
        @Query("date") date: String
    ): Call<SlotResponse>

    @POST("api/bookings")
    fun createBooking(
        @Header("Authorization") token: String,
        @Body request: BookingRequest
    ): Call<BookingResponse>

    @GET("api/bookings/my")
    fun getMyBookings(@Header("Authorization") token: String): Call<BookingListResponse>

    @GET("api/bookings/{id}")
    fun getBookingDetail(
        @Header("Authorization") token: String,
        @Path("id") bookingId: Int
    ): Call<BookingDetailResponse>

    @PATCH("api/bookings/{id}/cancel")
    fun cancelBooking(
        @Header("Authorization") token: String,
        @Path("id") bookingId: Int
    ): Call<BaseResponse>

    @POST("api/payments/{bookingId}/method")
    fun choosePaymentMethod(
        @Header("Authorization") token: String,
        @Path("bookingId") bookingId: Int,
        @Body request: PaymentMethodRequest
    ): Call<BaseResponse>

    @Multipart
    @POST("api/payments/{bookingId}/upload")
    fun uploadPaymentProof(
        @Header("Authorization") token: String,
        @Path("bookingId") bookingId: Int,
        @Part file: MultipartBody.Part
    ): Call<BaseResponse>

    @GET("api/payments/{bookingId}")
    fun getPaymentStatus(
        @Header("Authorization") token: String,
        @Path("bookingId") bookingId: Int
    ): Call<PaymentStatusResponse>

    @GET("api/notifications")
    fun getNotifications(@Header("Authorization") token: String): Call<NotificationResponse>

    @GET("api/bookings/admin/all")
    fun adminGetAllBookings(@Header("Authorization") token: String): Call<BookingListResponse>

    @PATCH("api/bookings/admin/{id}/confirm")
    fun adminConfirmBooking(
        @Header("Authorization") token: String,
        @Path("id") bookingId: Int
    ): Call<BaseResponse>

    @PATCH("api/bookings/admin/{id}/reject")
    fun adminRejectBooking(
        @Header("Authorization") token: String,
        @Path("id") bookingId: Int
    ): Call<BaseResponse>

    @PATCH("api/payments/admin/{bookingId}/verify")
    fun adminVerifyPayment(
        @Header("Authorization") token: String,
        @Path("bookingId") bookingId: Int
    ): Call<BaseResponse>

    @GET("api/courts/near-me")
    fun getNearMeCourts(
        @Query("lat") latitude: Double,
        @Query("lng") longitude: Double
    ): Call<CourtResponse>

    @PUT("api/users/profile/update-name")
    fun updateName(
        @Header("Authorization") token: String,
        @Body request: UpdateNameRequest
    ): Call<BaseResponse>

    @PUT("api/users/profile/update-account")
    fun updateAccount(
        @Header("Authorization") token: String,
        @Body request: UpdateAccountRequest
    ): Call<BaseResponse>

    @Multipart
    @POST("api/users/profile/update-avatar")
    fun updateAvatar(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part
    ): Call<BaseResponse>
}