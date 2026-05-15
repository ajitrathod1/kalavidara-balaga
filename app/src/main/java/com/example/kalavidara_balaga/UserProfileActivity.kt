package com.example.kalavidara_balaga

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.kalavidara_balaga.databinding.ActivityUserProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserProfileBinding
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    private var artistId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            binding.tvUserEmail.text = currentUser.email ?: "No Email"
            
            // Fetch name from Firestore
            db.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        binding.tvUserName.text = document.getString("name") ?: "No Name"
                    }
                }
            
            // Always check if this user has a troupe profile
            checkIfTroupeExists(currentUser.uid)
        }

        binding.btnArtistAction.setOnClickListener {
            val intent = Intent(this, CreateTroupeActivity::class.java)
            if (artistId != null) {
                intent.putExtra("EDIT_MODE", true)
                intent.putExtra("ARTIST_ID", artistId)
            }
            startActivity(intent)
        }

        binding.btnMyBookings.setOnClickListener {
            startActivity(Intent(this, BookingsActivity::class.java))
        }

        binding.btnAbout.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        setupBottomNavigation()
    }

    private fun checkIfTroupeExists(userId: String) {
        db.collection("artists")
            .whereEqualTo("createdBy", userId)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val doc = documents.documents[0]
                    artistId = doc.id
                    updateArtistUI(true)
                } else {
                    updateArtistUI(false)
                }
            }
    }

    private fun updateArtistUI(hasTroupe: Boolean) {
        if (hasTroupe) {
            binding.tvArtistActionTitle.text = "Manage My Troupe"
            binding.tvArtistActionSub.text = "Update your profile, gallery and videos"
            binding.ivArtistActionIcon.setImageResource(R.drawable.ic_about)
        } else {
            binding.tvArtistActionTitle.text = "Become an Artist"
            binding.tvArtistActionSub.text = "Create your troupe profile and get bookings"
            binding.ivArtistActionIcon.setImageResource(android.R.drawable.ic_menu_edit)
        }
    }

    private fun setupBottomNavigation() {
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
                R.id.nav_profile -> true
                else -> false
            }
        }
    }
}