package com.pab.deucepadelapp.model

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val phone: String? = null
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val data: AuthData?
)

data class AuthData(
    val token: String,
    val user: UserData
)

data class UserData(
    val id: Int,
    val name: String,
    val email: String,
    val phone: String?,
    val role: String
)

data class ProfileResponse(
    val success: Boolean,
    val message: String,
    val data: UserData?
)

data class BookingRequest(
    val courtId: Long,
    val bookingDate: String,
    val startTime: String,
    val endTime: String,
    val notes: String?
)

data class BookingResponse(
    val success: Boolean,
    val message: String,
    val data: BookingDetailData?
)

data class BookingListResponse(
    val success: Boolean,
    val message: String,
    val data: List<BookingDetailData>
)

data class BookingDetailResponse(
    val success: Boolean,
    val message: String,
    val data: BookingDetailData?
)

data class BookingDetailData(
    val id: Int,
    val courtId: Int,
    val courtName: String?,
    val courtPhotoUrl: String?,
    val bookingDate: String,
    val startTime: String,
    val endTime: String,
    val durationHours: Double,
    val totalPrice: Double,
    val status: String,
    val notes: String?,
    val qrToken: String?,
    val paymentMethod: String?,
    val paymentStatus: String?,
    val proofImageUrl: String?,
    val createdAt: String
)

data class PaymentMethodRequest(
    val method: String
)

data class PaymentStatusResponse(
    val success: Boolean,
    val message: String,
    val data: PaymentData?
)

data class PaymentData(
    val id: Int,
    val bookingId: Int,
    val method: String?,
    val amount: Double,
    val proofImageUrl: String?,
    val status: String,
    val verifiedAt: String?,
    val verifiedBy: String?
)

data class SlotResponse(
    val success: Boolean,
    val message: String,
    val data: List<SlotItem>
)

data class SlotItem(
    val startTime: String,
    val endTime: String,
    val available: Boolean
)

data class NotificationResponse(
    val success: Boolean,
    val message: String,
    val data: List<NotificationItem>
)

data class NotificationItem(
    val id: Int,
    val title: String,
    val message: String,
    val isRead: Boolean,
    val createdAt: String
)

data class BaseResponse(
    val success: Boolean,
    val message: String
)

data class CourtDetailResponse(
    val success: Boolean,
    val message: String,
    val data: CourtItem?
)