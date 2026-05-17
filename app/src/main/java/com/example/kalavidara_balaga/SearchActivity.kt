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
        setupArtTypeSpinner()
        setupBottomNavigation()
        setupSearchLogic()
        
        binding.rvArtists.layoutManager = androidx.recyclerview.widget.StaggeredGridLayoutManager(2, androidx.recyclerview.widget.StaggeredGridLayoutManager.VERTICAL)
        
        loadAllArtists()
    }

    private fun setupDistrictSpinner() {
        val districts = arrayOf(
            "All Districts", "Bagalkot", "Ballari", "Belagavi", "Bengaluru Rural", "Bengaluru Urban", 
            "Bidar", "Chamarajanagar", "Chikkaballapur", "Chikkamagaluru", "Chitradurga", 
            "Dakshina Kannada", "Davanagere", "Dharwad", "Gadag", "Hassan", "Haveri", 
            "Kalaburagi", "Kodagu", "Kolar", "Koppal", "Mandya", "Mysuru", "Raichur", 
            "Ramanagara", "Shivamogga", "Tumakuru", "Udupi", "Uttara Kannada", "Vijayapura", 
            "Yadgir", "Vijayanagara"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, districts)
        binding.spinnerDistrict.setAdapter(adapter)
        
        binding.spinnerDistrict.setOnItemClickListener { _, _, position, _ ->
            selectedDistrict = if (position == 0) "" else districts[position]
            applyFilters()
        }
    }

    private fun setupArtTypeSpinner() {
        val artForms = arrayOf("All Art Forms", "Dollu Kunitha", "Yakshagana", "Pooja Kunitha", "Veeragase", "Kamsale", "Suggi Kunitha", "Goravara Kunitha", "Bhootha Aradhane")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, artForms)
        binding.spinnerArtType.setAdapter(adapter)

        binding.spinnerArtType.setOnItemClickListener { _, _, position, _ ->
            selectedArtType = if (position == 0) "" else artForms[position]
            applyFilters()
        }
    }

    private fun setupSearchLogic() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
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
            val matchesDistrict = selectedDistrict.isEmpty() || 
                    artist.district.contains(selectedDistrict, ignoreCase = true) || 
                    artist.location.contains(selectedDistrict, ignoreCase = true)
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