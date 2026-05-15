package com.example.kalavidara_balaga

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.kalavidara_balaga.databinding.ActivityEventsBinding

class EventsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEventsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // setSupportActionBar(binding.toolbar)
        // supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_bookings -> {
                    startActivity(Intent(this, BookingsActivity::class.java))
                    finish()
                    true
                }
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