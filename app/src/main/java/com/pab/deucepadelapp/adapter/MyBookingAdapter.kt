package com.pab.deucepadelapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.pab.deucepadelapp.R
import com.pab.deucepadelapp.model.BookingData

class MyBookingAdapter(private var listBooking: List<BookingData>) :
    RecyclerView.Adapter<MyBookingAdapter.BookingViewHolder>() {

    // KUNCI UTAMA: Fungsi untuk menyegarkan data adapter saat dipanggil dari activity
    fun updateData(newList: List<BookingData>) {
        this.listBooking = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_my_booking, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = listBooking[position]
        val context = holder.itemView.context

        holder.tvCourtName.text = booking.courtName ?: "Padel Court"
        holder.tvCourtBadge.text = booking.courtCode ?: "C01"
        holder.tvDate.text = booking.scheduleDate ?: ""
        holder.tvTime.text = "${booking.startTime ?: "16:00"} - ${booking.endTime ?: "17:30"}"
        holder.tvDuration.text = "Duration: ${booking.duration} Hours"
        holder.tvPrice.text = "Rp ${String.format("%,.0f", booking.totalPrice)}"

        // Ambil status aman dengan fallback string kosong jika null
        val statusAman = booking.status?.uppercase() ?: "PENDING"

        // Menyesuaikan penamaan dengan enum PaymentStatus Backend kamu
        when (statusAman) {
            "VERIFIED", "CONFIRMED", "SUCCESS" -> {
                holder.tvStatusBadge.text = "Confirmed"
                holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark))
            }
            "UPLOADED" -> {
                holder.tvStatusBadge.text = "Reviewing"
                holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, android.R.color.holo_orange_dark))
            }
            "FAILED", "REJECTED" -> {
                holder.tvStatusBadge.text = "Failed"
                holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark))
            }
            else -> {
                holder.tvStatusBadge.text = "Pending"
                holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
            }
        }

        val fullPhotoName = booking.courtPhoto ?: "lap1"
        val cleanPhotoName = if (fullPhotoName.contains(".")) {
            fullPhotoName.substringBefore(".")
        } else {
            fullPhotoName
        }

        val resId = context.resources.getIdentifier(cleanPhotoName, "drawable", context.packageName)
        if (resId != 0) {
            holder.ivCourt.setImageResource(resId)
        } else {
            val defaultResId = context.resources.getIdentifier("lap1", "drawable", context.packageName)
            if (defaultResId != 0) {
                holder.ivCourt.setImageResource(defaultResId)
            } else {
                holder.ivCourt.setImageResource(android.R.color.darker_gray)
            }
        }
    }

    override fun getItemCount(): Int = listBooking.size

    class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivCourt: ShapeableImageView = itemView.findViewById(R.id.ivCourt)
        val tvCourtBadge: TextView = itemView.findViewById(R.id.tvCourtBadge)
        val tvCourtName: TextView = itemView.findViewById(R.id.tvCourtName)
        val tvStatusBadge: TextView = itemView.findViewById(R.id.tvStatusBadge)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val tvDuration: TextView = itemView.findViewById(R.id.tvDuration)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
    }
}