package com.pab.deucepadelapp.api

import retrofit2.Call
import retrofit2.http.*
import okhttp3.MultipartBody
import com.pab.deucepadelapp.model.* // Otomatis mengimpor semua data class model yang digunakan

interface ApiService {

    // ==========================================
    // 1. AUTHENTICATION (AUTENTIKASI)
    // ==========================================

    @POST("api/auth/register")
    fun register(@Body request: RegisterRequest): Call<AuthResponse>

    @POST("api/auth/login")
    fun login(@Body request: LoginRequest): Call<AuthResponse>

    @GET("api/auth/me")
    fun getMyProfile(@Header("Authorization") token: String): Call<ProfileResponse>

    @POST("api/auth/logout")
    fun logout(@Header("Authorization") token: String): Call<BaseResponse>


    // ==========================================
    // 2. COURTS (LAPANGAN)
    // ==========================================

    // Mengambil daftar semua lapangan aktif untuk halaman Home
    @GET("api/courts")
    fun getCourts(): Call<CourtResponse>

    // Mengambil detail satu lapangan berdasarkan ID
    @GET("api/courts/{id}")
    fun getCourtDetail(@Path("id") courtId: Int): Call<CourtDetailResponse>

    // Mengecek ketersediaan slot waktu pada tanggal tertentu (format: YYYY-MM-DD)
    @GET("api/courts/{id}/slots")
    fun getCourtSlots(
        @Path("id") courtId: Int,
        @Query("date") date: String
    ): Call<SlotResponse>


    // ==========================================
    // 3. BOOKINGS (PEMESANAN LAPANGAN)
    // ==========================================

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


    // ==========================================
    // 4. PAYMENTS (PEMBAYARAN)
    // ==========================================

    @POST("api/payments/{bookingId}/method")
    fun choosePaymentMethod(
        @Header("Authorization") token: String,
        @Path("bookingId") bookingId: Int,
        @Body request: PaymentMethodRequest
    ): Call<BaseResponse>

    // Endpoint untuk upload bukti bayar menggunakan format file/gambar (Multipart)
    @Multipart
    @POST("api/payments/{bookingId}/proof")
    fun uploadPaymentProof(
        @Header("Authorization") token: String,
        @Path("bookingId") bookingId: Int,
        @Part proof: MultipartBody.Part
    ): Call<BaseResponse>

    @GET("api/payments/{bookingId}")
    fun getPaymentStatus(
        @Header("Authorization") token: String,
        @Path("bookingId") bookingId: Int
    ): Call<PaymentStatusResponse>


    // ==========================================
    // 5. NOTIFICATIONS (NOTIFIKASI)
    // ==========================================

    @GET("api/notifications")
    fun getNotifications(@Header("Authorization") token: String): Call<NotificationResponse>


    // ==========================================
    // 6. ADMIN (FITUR KHUSUS ADMIN)
    // ==========================================

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


    // ==========================================
    // 7. REAL LOCATION-BASED FILTER
    // ==========================================

    // Mengambil rekomendasi lapangan terdekat berdasarkan koordinat live GPS pengguna
    @GET("api/courts/near-me")
    fun getNearMeCourts(
        @Query("lat") latitude: Double,
        @Query("lng") longitude: Double
    ): Call<CourtResponse>

}