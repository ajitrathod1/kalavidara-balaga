package com.example.kalavidara_balaga

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.kalavidara_balaga.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupFeatureItems()
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        // "About" is inside "Profile", so we select Profile tab
        binding.bottomNavigation.selectedItemId = R.id.nav_profile
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

    private fun setupFeatureItems() {
        binding.feature1.tvFeatureTitle.text = "Empowering Folk Artists"
        // binding.feature1.ivFeatureIcon.setImageResource(...)

        binding.feature2.tvFeatureTitle.text = "Promoting Cultural Heritage"
        // binding.feature2.ivFeatureIcon.setImageResource(...)

        binding.feature3.tvFeatureTitle.text = "Building a Sustainable Future"
        // binding.feature3.ivFeatureIcon.setImageResource(...)
    }
}