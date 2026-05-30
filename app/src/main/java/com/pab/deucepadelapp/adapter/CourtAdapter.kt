package com.pab.deucepadelapp.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pab.deucepadelapp.R
import com.pab.deucepadelapp.activity.DetailCourtActivity
import com.pab.deucepadelapp.model.CourtItem
import com.google.gson.Gson
import java.util.ArrayList

class CourtAdapter(private var courtList: ArrayList<CourtItem>) :
    RecyclerView.Adapter<CourtAdapter.CourtViewHolder>() {

    fun updateData(newCourts: List<CourtItem>) {
        courtList.clear()
        courtList.addAll(newCourts)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourtViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_court, parent, false)
        return CourtViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourtViewHolder, position: Int) {
        val court = courtList[position]
        val context = holder.itemView.context

        // 1. WAJIB: Clear view Glide terlebih dahulu agar sisa gambar lama tidak tersangkut saat scroll
        Glide.with(context).clear(holder.ivCourtImage)

        // Set teks informasi dasar lapangan
        holder.tvCourtName.text = court.name
        holder.tvCourtLocation.text = court.description
        holder.tvCourtRating.text = court.rate.toString()

        // Proteksi kalkulasi harga agar berformat ribuan rapi (Rp 150.000)
        val courtPrice = try {
            court.pricePerHour
        } catch (e: Exception) {
            150000.0
        }
        holder.tvCourtPrice.text = "Rp ${String.format("%,.0f", courtPrice)}"

        // =====================================================================
        // FIX GAMBAR DOUBLE: Cek ID berdasarkan Angka ATAU String Teks bawaan API
        // =====================================================================
        val idString = court.id.toString().trim()

        val imageResource = when {
            idString.contains("1") -> R.drawable.lap1
            idString.contains("2") -> R.drawable.lap2
            idString.contains("3") -> R.drawable.lap3
            idString.contains("4") -> R.drawable.lap4
            idString.contains("5") -> R.drawable.lap5
            else -> {
                // Jika ID berupa string acak (tidak ada angka 1-5), bagi posisi item agar gambar bervariasi
                when (position % 5) {
                    0 -> R.drawable.lap1
                    1 -> R.drawable.lap2
                    2 -> R.drawable.lap3
                    3 -> R.drawable.lap4
                    else -> R.drawable.lap5
                }
            }
        }

        // Render gambar menggunakan Glide secara aman
        Glide.with(context)
            .load(imageResource)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_menu_report_image)
            .into(holder.ivCourtImage)

        // =====================================================================
        // LOGIKA KLIK MENUJU DETAIL COURT ACTIVITY
        // =====================================================================
        holder.itemView.setOnClickListener {
            val intent = Intent(context, DetailCourtActivity::class.java)

            val courtId = try { court.id.toLong() } catch (e: Exception) { -1L }
            intent.putExtra("COURT_ID", courtId)
            intent.putExtra("COURT_NAME", court.name)
            intent.putExtra("COURT_DESC", court.description)
            intent.putExtra("COURT_PRICE", courtPrice)
            intent.putExtra("COURT_RATE", court.rate)

            // Mengonversi ID Ke String nama drawable agar halaman Detail ikut sinkron gambarnya
            val photoNameString = when (imageResource) {
                R.drawable.lap1 -> "lap1"
                R.drawable.lap2 -> "lap2"
                R.drawable.lap3 -> "lap3"
                R.drawable.lap4 -> "lap4"
                R.drawable.lap5 -> "lap5"
                else -> "lap1"
            }
            intent.putExtra("COURT_PHOTO", photoNameString)

            // Mengirim data objek pelengkap menggunakan JSON String
            val gson = Gson()
            try { intent.putExtra("COURT_COACHES", gson.toJson(court.coaches)) } catch (e: Exception) {}
            try { intent.putExtra("COURT_EVENTS", gson.toJson(court.events)) } catch (e: Exception) {}
            try { intent.putExtra("COURT_SLOTS", gson.toJson(court.availableSlots)) } catch (e: Exception) {}

            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = courtList.size

    class CourtViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivCourtImage: ImageView = itemView.findViewById(R.id.ivCourtImage)
        val tvCourtName: TextView = itemView.findViewById(R.id.tvCourtName)
        val tvCourtLocation: TextView = itemView.findViewById(R.id.tvCourtLocation)
        val tvCourtPrice: TextView = itemView.findViewById(R.id.tvCourtPrice)
        val tvCourtRating: TextView = itemView.findViewById(R.id.tvCourtRating)
    }
}