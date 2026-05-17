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

    private fun addDummyDataIfEmpty() {
        db.collection("artists").limit(1).get().addOnSuccessListener { result ->
            if (result.isEmpty) {
                val dummyArtists = listOf(
                    mapOf(
                        "name" to "Sri Banashankari Dollu Balaga", "district" to "Tumakuru", "artForm" to "Dollu Kunitha",
                        "experience" to "15", "membersCount" to "12-15", "status" to "approved",
                        "imageUrl" to "https://images.unsplash.com/photo-1582373468547-52646399ba71?auto=format&fit=crop&q=80&w=800",
                        "galleryImages" to listOf(
                            "https://images.unsplash.com/photo-1582373468547-52646399ba71?auto=format&fit=crop&q=80&w=600",
                            "https://images.unsplash.com/photo-1628155930542-3c7a64e2c833?auto=format&fit=crop&q=80&w=600",
                            "https://images.unsplash.com/photo-1514525253344-934d70ef1d10?auto=format&fit=crop&q=80&w=600"
                        ),
                        "about" to "We are a professional Dollu Kunitha troupe from Tumakuru. We perform at weddings, festivals, temple events and corporate shows across Karnataka.", "phone" to "8123456789",
                        "equipment" to "10 Traditional Dollu Drums, Cymbals, Uniform Costumes"
                    ),
                    mapOf(
                        "name" to "Coastal Yakshagana Mandali", "district" to "Udupi", "artForm" to "Yakshagana",
                        "experience" to "25", "membersCount" to "20-25", "status" to "approved",
                        "imageUrl" to "https://images.unsplash.com/photo-1621360841013-c7683c659ec6?auto=format&fit=crop&q=80&w=800",
                        "galleryImages" to listOf(
                            "https://images.unsplash.com/photo-1621360841013-c7683c659ec6?auto=format&fit=crop&q=80&w=600",
                            "https://images.unsplash.com/photo-1583089892943-e02e5b017b6a?auto=format&fit=crop&q=80&w=600"
                        ),
                        "about" to "Traditional Yakshagana Balaga with a rich history of over 25 years in presenting Tenkutittu and Badagutittu styles.", "phone" to "7012345678",
                        "equipment" to "Complete Yakshagana Headgear, Costumes, Chande and Maddale drums"
                    ),
                    mapOf(
                        "name" to "Shivashakti Veeragase Balaga", "district" to "Chikkamagaluru", "artForm" to "Veeragase",
                        "experience" to "12", "membersCount" to "8-10", "status" to "approved",
                        "imageUrl" to "https://images.unsplash.com/photo-1614613535308-eb5fbd3d2c17?auto=format&fit=crop&q=80&w=800",
                        "galleryImages" to listOf(
                            "https://images.unsplash.com/photo-1614613535308-eb5fbd3d2c17?auto=format&fit=crop&q=80&w=600",
                            "https://images.unsplash.com/photo-1599739291060-4578e77dac5d?auto=format&fit=crop&q=80&w=600"
                        ),
                        "about" to "Energetic Veeragase performers known for their powerful Shiva Stuti and traditional dance forms.", "phone" to "9900887766",
                        "equipment" to "Traditional Swords, Trishula, Brass Ornaments"
                    ),
                    mapOf(
                        "name" to "Malnad Janapada Gayakaru", "district" to "Shivamogga", "artForm" to "Folk Singing",
                        "experience" to "20", "membersCount" to "4-6", "status" to "approved",
                        "imageUrl" to "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?auto=format&fit=crop&q=80&w=800",
                        "galleryImages" to listOf(
                            "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?auto=format&fit=crop&q=80&w=600",
                            "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?auto=format&fit=crop&q=80&w=600"
                        ),
                        "about" to "Soulful folk melodies and Janapada songs that bring out the true essence of rural Karnataka.", "phone" to "8877665544",
                        "equipment" to "Harmonium, Tabla, Tala, Tambura"
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
                        artistList.add(artist)
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
