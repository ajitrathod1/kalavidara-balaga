package com.example.kalavidara_balaga

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kalavidara_balaga.adapter.ArtistAdapter
import com.example.kalavidara_balaga.databinding.ActivitySearchBinding
import com.example.kalavidara_balaga.model.Artist
import com.google.firebase.firestore.FirebaseFirestore

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private var allArtists = mutableListOf<Artist>()
    private var selectedDistrict = ""
    private var selectedArtType = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        setupDistrictSpinner()
        setupBottomNavigation()
        setupSearchAndChips()
        
        binding.rvArtists.layoutManager = LinearLayoutManager(this)
        
        loadAllArtists()
    }

    private fun setupDistrictSpinner() {
        val districts = arrayOf("All Districts", "Bangalore", "Mysore", "Belgaum", "Dharwad", "Udupi", "Hassan", "Tumakuru", "Shivamogga")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, districts)
        binding.spinnerDistrict.setAdapter(adapter)
        
        binding.spinnerDistrict.setOnItemClickListener { _, _, position, _ ->
            selectedDistrict = if (position == 0) "" else districts[position]
            applyFilters()
        }
    }

    private fun setupSearchAndChips() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.chipAll.setOnClickListener { 
            selectedArtType = ""
            updateChipUI(it.id)
            applyFilters() 
        }
        binding.chipDollu.setOnClickListener { 
            selectedArtType = "Dollu Kunitha"
            updateChipUI(it.id)
            applyFilters() 
        }
        binding.chipPooja.setOnClickListener { 
            selectedArtType = "Pooja Kunitha"
            updateChipUI(it.id)
            applyFilters() 
        }
        binding.chipYakshagana.setOnClickListener { 
            selectedArtType = "Yakshagana"
            updateChipUI(it.id)
            applyFilters() 
        }
    }

    private fun updateChipUI(selectedId: Int) {
        val chips = listOf(binding.chipAll, binding.chipDollu, binding.chipPooja, binding.chipYakshagana)
        chips.forEach { chip ->
            if (chip.id == selectedId) {
                chip.setBackgroundResource(R.drawable.circle_primary)
                chip.setTextColor(resources.getColor(R.color.white))
            } else {
                chip.setBackgroundResource(R.drawable.bg_rounded_grey)
                chip.setTextColor(resources.getColor(R.color.black))
            }
        }
    }

    private fun loadAllArtists() {
        val db = FirebaseFirestore.getInstance()
        db.collection("artists")
            .whereEqualTo("status", "approved") // Only show approved troupes
            .get()
            .addOnSuccessListener { documents ->
                allArtists.clear()
                for (doc in documents) {
                    val artist = doc.toObject(Artist::class.java).copy(id = doc.id)
                    allArtists.add(artist)
                }
                applyFilters()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load artists: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun applyFilters() {
        val query = binding.etSearch.text.toString().lowercase()
        
        val filteredList = allArtists.filter { artist ->
            val matchesDistrict = selectedDistrict.isEmpty() || artist.location.contains(selectedDistrict, ignoreCase = true)
            val matchesArtType = selectedArtType.isEmpty() || artist.artForm.contains(selectedArtType, ignoreCase = true)
            val matchesQuery = query.isEmpty() || 
                               artist.name.lowercase().contains(query) || 
                               artist.artForm.lowercase().contains(query)
            
            matchesDistrict && matchesArtType && matchesQuery
        }
        
        updateRecyclerView(filteredList)
    }

    private fun updateRecyclerView(artists: List<Artist>) {
        val adapter = ArtistAdapter(artists) { artist ->
            val intent = Intent(this, ArtistProfileActivity::class.java)
            intent.putExtra("ARTIST_ID", artist.id)
            startActivity(intent)
        }
        binding.rvArtists.adapter = adapter
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = 0 // Search is not in menu anymore
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