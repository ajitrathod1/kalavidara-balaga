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

        fetchArtistsFromFirestore()

        binding.cardSearch.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }
        
        // Setup Bottom Navigation
        binding.bottomNavigation.selectedItemId = R.id.nav_home
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.nav_home -> true
                R.id.nav_profile -> {
                    startActivity(Intent(this, UserProfileActivity::class.java))
                    finish()
                    true
                }
                else -> false
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
                        // Filter out dummy/example troupes
                        val dummyNames = listOf("Udupi", "Haveri", "Mysore", "Mysuru", "Chikkamagaluru", "Tumakuru", "Veera Karnataka", "Kedarlinga")
                        val isDummy = dummyNames.any { artist.name.contains(it, ignoreCase = true) }
                        
                        if (!isDummy) {
                            artistList.add(artist)
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
