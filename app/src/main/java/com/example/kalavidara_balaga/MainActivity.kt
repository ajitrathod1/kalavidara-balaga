package com.example.kalavidara_balaga

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.kalavidara_balaga.adapter.ArtistAdapter
import com.example.kalavidara_balaga.databinding.ActivityMainBinding
import com.example.kalavidara_balaga.model.Artist
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        addDummyDataIfEmpty()
        fetchArtistsFromFirestore()
        
        // Setup Bottom Navigation
        binding.bottomNavigation.selectedItemId = R.id.nav_home
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.nav_home -> true
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

    private fun addDummyDataIfEmpty() {
        db.collection("artists").limit(1).get().addOnSuccessListener { result ->
            if (result.isEmpty) {
                val dummyArtists = listOf(
                    mapOf(
                        "name" to "Veera Karnataka", "district" to "Tumakuru", "artForm" to "Dollu Kunitha",
                        "experience" to "15", "membersCount" to "12-15", "status" to "approved",
                        "imageUrl" to "res/drawable/folk_banner",
                        "galleryImages" to listOf("res/drawable/folk_banner", "res/drawable/about_banner", "res/drawable/folk_illustration"),
                        "about" to "We are a professional Dollu Kunitha troupe from Tumakuru. We perform at weddings, festivals, temple events and corporate shows across Karnataka.", "phone" to "8123456789"
                    ),
                    mapOf(
                        "name" to "Udupi Yakshagana Balaga", "district" to "Udupi", "artForm" to "Yakshagana",
                        "experience" to "25", "membersCount" to "20-25", "status" to "approved",
                        "imageUrl" to "res/drawable/about_banner",
                        "galleryImages" to listOf("res/drawable/folk_banner", "res/drawable/about_banner", "res/drawable/folk_illustration"),
                        "about" to "Traditional Yakshagana Balaga with a rich history of over 25 years in presenting Tenkutittu and Badagutittu styles.", "phone" to "7012345678"
                    ),
                    mapOf(
                        "name" to "Kedarlinga Veeragase", "district" to "Chikkamagaluru", "artForm" to "Veeragase",
                        "experience" to "12", "membersCount" to "8-10", "status" to "approved",
                        "imageUrl" to "res/drawable/folk_banner",
                        "galleryImages" to listOf("res/drawable/folk_illustration", "res/drawable/folk_banner", "res/drawable/about_banner"),
                        "about" to "Energetic Veeragase performers known for their powerful Shiva Stuti and traditional dance forms.", "phone" to "9900887766"
                    ),
                    mapOf(
                        "name" to "Haveri Janapada Balaga", "district" to "Haveri", "artForm" to "Folk Singing",
                        "experience" to "20", "membersCount" to "4-6", "status" to "approved",
                        "imageUrl" to "res/drawable/folk_illustration",
                        "galleryImages" to listOf("res/drawable/about_banner", "res/drawable/folk_illustration", "res/drawable/folk_banner"),
                        "about" to "Soulful folk melodies and Janapada songs that bring out the true essence of rural Karnataka.", "phone" to "8877665544"
                    )
                )
                
                for (data in dummyArtists) {
                    db.collection("artists").add(data).addOnSuccessListener {
                        fetchArtistsFromFirestore()
                    }
                }
            }
        }
    }

    private fun fetchArtistsFromFirestore() {
        db.collection("artists")
            .get()
            .addOnSuccessListener { result ->
                val artistList = mutableListOf<Artist>()
                for (document in result) {
                    try {
                        val artist = document.toObject(Artist::class.java).copy(id = document.id)
                        // Temporary filter to remove "Mysuru Palace Troupe" if it exists in DB
                        if (!artist.name.contains("Mysuru Palace", ignoreCase = true)) {
                            artistList.add(artist)
                        } else {
                            // Optionally delete it from DB so it's gone forever
                            db.collection("artists").document(document.id).delete()
                        }
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Error parsing artist: ${document.id}", e)
                    }
                }
                setupRecyclerView(artistList)
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreError", "Error getting documents: ", exception)
                Toast.makeText(this, "Failed to load artists", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupRecyclerView(artists: List<Artist>) {
        if (artists.isEmpty()) return
        
        binding.rvPopularTroupes.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 2)
            adapter = ArtistAdapter(artists) { artist ->
                val intent = Intent(this@MainActivity, ArtistProfileActivity::class.java)
                intent.putExtra("ARTIST_ID", artist.id)
                startActivity(intent)
            }
        }
    }
}