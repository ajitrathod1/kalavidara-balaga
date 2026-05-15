package com.example.kalavidara_balaga

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.kalavidara_balaga.databinding.ActivityBookingBinding

class BookingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupBookingData()

        binding.btnBookingCall.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:+919876543210")
            startActivity(intent)
        }

        binding.btnShareProfile.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this artist troupe: Dollu Kunitha Group")
            startActivity(Intent.createChooser(shareIntent, "Share via"))
        }
    }

    private fun setupBookingData() {
        binding.tvBookingTroupeName.text = "Dollu Kunitha Group"
        binding.tvBookingLocation.text = "Belgaum, Karnataka"

        // Setup contact rows
        binding.rowPhone.tvRowLabel.text = "Phone"
        binding.rowPhone.tvRowValue.text = "+91 98765 43210"
        binding.rowPhone.ivRowIcon.setImageResource(android.R.drawable.ic_menu_call)

        binding.rowWhatsApp.tvRowLabel.text = "WhatsApp"
        binding.rowWhatsApp.tvRowValue.text = "Chat on WhatsApp"
        binding.rowWhatsApp.ivRowIcon.setImageResource(android.R.drawable.stat_notify_chat)

        binding.rowEmail.tvRowLabel.text = "Email"
        binding.rowEmail.tvRowValue.text = "dollukunitha@gmail.com"
        binding.rowEmail.ivRowIcon.setImageResource(android.R.drawable.ic_dialog_email)
    }
}