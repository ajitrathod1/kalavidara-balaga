package com.example.kalavidara_balaga.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kalavidara_balaga.R
import com.example.kalavidara_balaga.model.Booking
import com.google.android.material.button.MaterialButton

class BookingAdapter(
    private val bookings: List<Booking>,
    private val currentUserId: String,
    private val onAction: (Booking, String) -> Unit // Booking, New Status
) : RecyclerView.Adapter<BookingAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivArtist: ImageView = view.findViewById(R.id.ivArtist)
        val tvArtistName: TextView = view.findViewById(R.id.tvArtistName)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvEventInfo: TextView = view.findViewById(R.id.tvEventInfo)
        val tvLocation: TextView = view.findViewById(R.id.tvLocation)
        val tvBookingDate: TextView = view.findViewById(R.id.tvBookingDate)
        val tvUserMessage: TextView = view.findViewById(R.id.tvUserMessage)
        val tvArtistResponse: TextView = view.findViewById(R.id.tvArtistResponse)
        val layoutArtistActions: LinearLayout = view.findViewById(R.id.layoutArtistActions)
        val btnAccept: MaterialButton = view.findViewById(R.id.btnAccept)
        val btnReject: MaterialButton = view.findViewById(R.id.btnReject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_booking, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val booking = bookings[position]
        
        holder.tvArtistName.text = booking.artistName
        holder.tvStatus.text = booking.status.replaceFirstChar { it.uppercase() }
        holder.tvEventInfo.text = "${booking.eventType} • ${booking.eventDate}"
        holder.tvLocation.text = booking.eventLocation
        
        val dateStr = booking.timestamp?.toDate()?.toString()?.substring(4, 10) ?: "N/A"
        holder.tvBookingDate.text = "Requested on: $dateStr"

        // Show User Message
        if (booking.message.isNotEmpty()) {
            holder.tvUserMessage.visibility = View.VISIBLE
            holder.tvUserMessage.text = "Message: ${booking.message}"
        } else {
            holder.tvUserMessage.visibility = View.GONE
        }

        // Show Artist Response
        if (booking.artistResponse.isNotEmpty()) {
            holder.tvArtistResponse.visibility = View.VISIBLE
            holder.tvArtistResponse.text = "Response: ${booking.artistResponse}"
        } else {
            holder.tvArtistResponse.visibility = View.GONE
        }

        // Status styling
        when (booking.status) {
            "pending" -> holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#F28C30"))
            "accepted" -> holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
            "rejected" -> holder.tvStatus.setTextColor(android.graphics.Color.RED)
        }

        // Action Buttons visibility (Only for Artist and if status is pending)
        if (booking.artistId == currentUserId && booking.status == "pending") {
            holder.layoutArtistActions.visibility = View.VISIBLE
            holder.btnAccept.setOnClickListener { onAction(booking, "accepted") }
            holder.btnReject.setOnClickListener { onAction(booking, "rejected") }
        } else {
            holder.layoutArtistActions.visibility = View.GONE
        }

        // Load Image
        if (!booking.artistImage.isNullOrEmpty()) {
            val context = holder.itemView.context
            if (booking.artistImage.startsWith("res/")) {
                val resPath = booking.artistImage.substringAfter("res/")
                val resourceId = context.resources.getIdentifier(resPath, null, context.packageName)
                if (resourceId != 0) {
                    holder.ivArtist.setImageResource(resourceId)
                } else {
                    holder.ivArtist.setImageResource(R.drawable.folk_banner)
                }
            } else {
                Glide.with(context)
                    .load(booking.artistImage)
                    .placeholder(R.drawable.folk_banner)
                    .into(holder.ivArtist)
            }
        }
    }

    override fun getItemCount() = bookings.size
}