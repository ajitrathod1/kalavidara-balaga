package com.example.kalavidara_balaga

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kalavidara_balaga.databinding.ActivityBookingInquiryBinding
import com.example.kalavidara_balaga.model.Booking
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class BookingInquiryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookingInquiryBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var artistId: String? = null
    private var artistName: String? = null
    private var artistImage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookingInquiryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        artistId = intent.getStringExtra("ARTIST_ID")
        
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        if (artistId != null) {
            fetchArtistDetails(artistId!!)
        }

        setupSpinners()
        setupDatePicker()

        binding.btnSubmitBooking.setOnClickListener {
            submitBooking()
        }
    }

    private fun fetchArtistDetails(id: String) {
        db.collection("artists").document(id).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    artistName = doc.getString("name")
                    artistImage = doc.getString("imageUrl")
                }
            }
    }

    private fun setupSpinners() {
        val eventTypes = arrayOf("Wedding", "Temple Festival", "Corporate Event", "Private Party", "Government Program", "Other")
        binding.spinnerEventType.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, eventTypes))

        val durations = arrayOf("1-2 Hours", "2-4 Hours", "Full Day", "Multiple Days")
        binding.spinnerDuration.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, durations))

        val budgets = arrayOf("Under ₹10,000", "₹10,000 - ₹25,000", "₹25,000 - ₹50,000", "₹50,000 - ₹1,00,000", "Above ₹1,00,000")
        binding.spinnerBudget.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, budgets))
    }

    private fun setupDatePicker() {
        binding.etEventDate.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val dpd = DatePickerDialog(this, { _, y, m, d ->
                binding.etEventDate.setText("$d/${m + 1}/$y")
            }, year, month, day)
            
            dpd.datePicker.minDate = System.currentTimeMillis() - 1000
            dpd.show()
        }
    }

    private fun submitBooking() {
        val userId = auth.currentUser?.uid ?: return
        val eventType = binding.spinnerEventType.text.toString()
        val eventDate = binding.etEventDate.text.toString()
        val location = binding.etLocation.text.toString()
        val duration = binding.spinnerDuration.text.toString()
        val budget = binding.spinnerBudget.text.toString()
        val message = binding.etMessage.text.toString()

        if (eventType.isEmpty() || eventDate.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnSubmitBooking.isEnabled = false
        binding.btnSubmitBooking.text = "Sending..."

        val booking = Booking(
            artistId = artistId ?: "",
            bookerId = userId,
            artistName = artistName ?: "Unknown Artist",
            artistImage = artistImage,
            eventType = eventType,
            eventDate = eventDate,
            eventLocation = location,
            duration = duration,
            budget = budget,
            message = message,
            status = "pending",
            timestamp = Timestamp.now()
        )

        db.collection("bookings").add(booking)
            .addOnSuccessListener {
                Toast.makeText(this, "Booking Inquiry Sent Successfully!", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                binding.btnSubmitBooking.isEnabled = true
                binding.btnSubmitBooking.text = "Send Inquiry"
            }
    }
}