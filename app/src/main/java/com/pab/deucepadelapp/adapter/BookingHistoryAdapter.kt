package com.pab.deucepadelapp.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.pab.deucepadelapp.R
import com.pab.deucepadelapp.network.PaymentData // Diarahkan ke package model yang baru
import java.text.NumberFormat
import java.util.Locale

class BookingHistoryAdapter(private val listPayment: List<PaymentData>) :
    RecyclerView.Adapter<BookingHistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCourtName: TextView = view.findViewById(R.id.tvCourtName)
        val tvTransactionCode: TextView = view.findViewById(R.id.tvTransactionCode)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val cardStatus: MaterialCardView = view.findViewById(R.id.cardStatus)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_booking_history, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val payment = listPayment[position]

        // Menampilkan nama lapangan asli dari backend (jika null, gunakan fallback)
        holder.tvCourtName.text = payment.courtName ?: "Padel Court #${payment.bookingId}"
        holder.tvTransactionCode.text = "TXN-9823${payment.id}"

        // Parsing tanggal bertipe ISO String dari backend ke format ringkas
        holder.tvDate.text = if (!payment.createdAt.isNullOrEmpty()) {
            payment.createdAt.replace("T", " ").substring(0, 16)
        } else {
            "-- : --"
        }

        // Format angka ke format mata uang Rupiah Indonesia (Rp xx.xxx)
        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        holder.tvAmount.text = formatRupiah.format(payment.amount).replace("Rp", "Rp ").replace(",00", "")

        // Dinamisasi warna card dan teks berdasarkan status transaksi backend
        when (payment.status.uppercase()) {
            "VERIFIED" -> {
                holder.tvStatus.text = "Success"
                holder.cardStatus.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.primary_lime))
                holder.tvStatus.setTextColor(Color.parseColor("#000000"))
            }
            "UPLOADED" -> {
                holder.tvStatus.text = "Reviewing"
                holder.cardStatus.setCardBackgroundColor(Color.parseColor("#FFE082"))
                holder.tvStatus.setTextColor(Color.parseColor("#795548"))
            }
            "FAILED" -> {
                holder.tvStatus.text = "Failed"
                holder.cardStatus.setCardBackgroundColor(Color.parseColor("#FFCDD2"))
                holder.tvStatus.setTextColor(Color.parseColor("#B71C1C"))
            }
            else -> { // Status UNPAID
                holder.tvStatus.text = "Pending"
                holder.cardStatus.setCardBackgroundColor(Color.parseColor("#E0E0E0"))
                holder.tvStatus.setTextColor(Color.parseColor("#616161"))
            }
        }
    }

    override fun getItemCount(): Int = listPayment.size
}