package com.pab.deucepadelapp.network

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

// ─── 1. GLOBAL RESPONSE WRAPPER ───
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T?
)

// ─── 2. DTO REQUEST PROFIL ───
data class NameRequest(val name: String)
data class AccountRequest(val email: String, val newPassword: String)

// ─── 3. DATA MODEL TRANSAKSI (SINKRON DENGAN BACKEND & ADAPTER) ───
data class PaymentData(
    val id: Long,
    val bookingId: Long,
    val amount: Int,           // Bertipe Int agar NumberFormat di adapter tidak crash
    val method: String,
    val status: String,
    val proofImageUrl: String?,
    val verifiedAt: String?,
    val verifiedBy: String?,
    val createdAt: String?,    // Menggunakan CamelCase sesuai kiriman Spring Boot
    val updatedAt: String?,
    val courtName: String?     // Menangkap nama lapangan asli dari database
)

// ─── 4. INTERFACE API USER (UNTUK MANAJEMEN PROFIL) ───
interface UserApiService {

    @PUT("api/users/profile/update-name")
    fun updateNama(
        @Header("Authorization") token: String,
        @Body request: NameRequest
    ): Call<ApiResponse<String>>

    @Multipart
    @POST("api/users/profile/update-avatar")
    fun updateAvatar(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part
    ): Call<ApiResponse<String>>

    @PUT("api/users/profile/update-account")
    fun updateAkun(
        @Header("Authorization") token: String,
        @Body request: AccountRequest
    ): Call<ApiResponse<String>>
}

// ─── 5. INTERFACE API PAYMENT (UNTUK TRANSAKSI & HISTORY) ───
interface PaymentApiService {

    @GET("api/payments/history")
    fun getHistoryPembayaran(
        @Header("Authorization") token: String
    ): Call<ApiResponse<List<PaymentData>>>

    @Multipart
    @POST("api/payments/{bookingId}/upload")
    fun uploadBuktiBayar(
        @Path("bookingId") bookingId: Long,
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part
    ): Call<ApiResponse<PaymentData>>
}