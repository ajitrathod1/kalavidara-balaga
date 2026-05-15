package com.example.kalavidara_balaga

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kalavidara_balaga.adapter.BookingAdapter
import com.example.kalavidara_balaga.databinding.ActivityBookingsBinding
import com.example.kalavidara_balaga.model.Booking
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BookingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookingsBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnExplore.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        fetchBookings()
        setupBottomNavigation()
    }

    private fun fetchBookings() {
        val userId = auth.currentUser?.uid ?: return
        
        // Fetch bookings where either bookerId or artistId matches current user
        // Note: In real app, you might want separate tabs, but for now we fetch all relevant ones
        
        db.collection("bookings")
            .get()
            .addOnSuccessListener { documents ->
                val allBookings = mutableListOf<Booking>()
                for (doc in documents) {
                    val booking = doc.toObject(Booking::class.java).apply { id = doc.id }
                    if (booking.bookerId == userId || booking.artistId == userId) {
                        allBookings.add(booking)
                    }
                }

                if (allBookings.isEmpty()) {
                    binding.layoutEmpty.visibility = View.VISIBLE
                    binding.rvBookings.visibility = View.GONE
                } else {
                    binding.layoutEmpty.visibility = View.GONE
                    binding.rvBookings.visibility = View.VISIBLE
                    
                    val adapter = BookingAdapter(allBookings, userId) { booking, newStatus ->
                        showResponseDialog(booking, newStatus)
                    }
                    binding.rvBookings.layoutManager = LinearLayoutManager(this)
                    binding.rvBookings.adapter = adapter
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showResponseDialog(booking: Booking, newStatus: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(if (newStatus == "accepted") "Accept Booking" else "Reject Booking")
        
        val input = EditText(this)
        input.hint = "Enter your message to the client..."
        builder.setView(input)

        builder.setPositiveButton("Submit") { _, _ ->
            val response = input.text.toString()
            updateBookingStatus(booking.id, newStatus, response)
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun updateBookingStatus(bookingId: String, status: String, response: String) {
        db.collection("bookings").document(bookingId)
            .update(mapOf(
                "status" to status,
                "artistResponse" to response
            ))
            .addOnSuccessListener {
                Toast.makeText(this, "Booking updated!", Toast.LENGTH_SHORT).show()
                fetchBookings() // Refresh list
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_bookings
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_bookings -> true
                R.id.nav_profile -> {
                    startActivity(Intent(this, UserProfileActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}