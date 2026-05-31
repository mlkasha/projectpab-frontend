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

        Glide.with(context).clear(holder.ivCourtImage)

        holder.tvCourtName.text = court.name
        holder.tvCourtLocation.text = court.description
        holder.tvCourtRating.text = court.rate.toString()

        val courtPrice = try {
            court.pricePerHour
        } catch (e: Exception) {
            150000.0
        }
        holder.tvCourtPrice.text = "Rp ${String.format("%,.0f", courtPrice)}"

        val imageResource = when (court.photoUrl) {
            "lap1" -> R.drawable.lap1
            "lap2" -> R.drawable.lap2
            "lap3" -> R.drawable.lap3
            "lap4" -> R.drawable.lap4
            "lap5" -> R.drawable.lap5
            else -> R.drawable.lap1
        }

        Glide.with(context)
            .load(imageResource)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_menu_report_image)
            .into(holder.ivCourtImage)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, DetailCourtActivity::class.java)

            val courtId = try { court.id.toLong() } catch (e: Exception) { -1L }
            intent.putExtra("COURT_ID", courtId)
            intent.putExtra("COURT_NAME", court.name)
            intent.putExtra("COURT_DESC", court.description)
            intent.putExtra("COURT_PRICE", courtPrice)
            intent.putExtra("COURT_RATE", court.rate)

            val photoNameString = when (imageResource) {
                R.drawable.lap1 -> "lap1"
                R.drawable.lap2 -> "lap2"
                R.drawable.lap3 -> "lap3"
                R.drawable.lap4 -> "lap4"
                R.drawable.lap5 -> "lap5"
                else -> "lap1"
            }
            intent.putExtra("COURT_PHOTO", photoNameString)

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