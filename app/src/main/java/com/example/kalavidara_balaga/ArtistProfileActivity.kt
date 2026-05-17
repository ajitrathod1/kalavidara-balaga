package com.example.kalavidara_balaga

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.example.kalavidara_balaga.adapter.GalleryAdapter
import com.example.kalavidara_balaga.databinding.ActivityArtistProfileBinding
import com.example.kalavidara_balaga.model.Artist
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ArtistProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityArtistProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArtistProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        val artistId = intent.getStringExtra("ARTIST_ID")
        
        if (artistId != null) {
            setupProfileData(artistId)
        }
    }

    private fun setupProfileData(artistId: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("artists").document(artistId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val artist = doc.toObject(Artist::class.java) ?: return@addOnSuccessListener
                    
                    // Basic Details
                    binding.tvArtistName.text = artist.name
                    binding.tvArtistLocation.text = artist.district.ifEmpty { artist.location }.ifEmpty { "Unknown" }
                    binding.tvArtFormSub.text = artist.artForm
                    binding.tvAboutFull.text = artist.about.ifEmpty { "No description provided." }
                    binding.tvEquipment.text = artist.equipment.ifEmpty { "No equipment listed" }
                    
                    // Stats Card
                    val exp = artist.experience
                    binding.tvExpVal.text = if (exp.contains("+") || exp.contains("Year")) exp else "$exp+"
                    binding.tvMembersVal.text = artist.membersCount.ifEmpty { artist.groupSize }.ifEmpty { "0" }
                    binding.tvRatingVal.text = "4.9"

                    // Profile Image & Banner
                    val imageUrl = artist.imageUrl
                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(this).load(imageUrl).placeholder(R.drawable.about_banner).into(binding.ivArtistImage)
                        Glide.with(this).load(imageUrl).placeholder(R.drawable.about_banner).into(binding.ivArtistBanner)
                    } else {
                        binding.ivArtistImage.setImageResource(R.drawable.placeholder_troupe)
                        binding.ivArtistBanner.setImageResource(R.drawable.about_banner)
                    }

                    // Portfolio Gallery
                    if (artist.galleryImages.isNotEmpty()) {
                        binding.rvGallery.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                        binding.rvGallery.adapter = GalleryAdapter(artist.galleryImages)
                        binding.rvGallery.visibility = View.VISIBLE
                        binding.tvNoGallery.visibility = View.GONE
                    } else {
                        binding.rvGallery.visibility = View.GONE
                        binding.tvNoGallery.visibility = View.VISIBLE
                    }

                    // Contact Actions
                    binding.btnCallAction.setOnClickListener {
                        if (artist.phone.isNotEmpty()) {
                            val intent = Intent(Intent.ACTION_DIAL)
                            intent.data = Uri.parse("tel:${artist.phone}")
                            startActivity(intent)
                        } else {
                            Toast.makeText(this, "Phone number not available", Toast.LENGTH_SHORT).show()
                        }
                    }

                    binding.btnWhatsAppAction.setOnClickListener {
                        if (artist.phone.isNotEmpty()) {
                            val url = "https://api.whatsapp.com/send?phone=91${artist.phone}"
                            val i = Intent(Intent.ACTION_VIEW)
                            i.data = Uri.parse(url)
                            startActivity(i)
                        } else {
                            Toast.makeText(this, "Phone number not available", Toast.LENGTH_SHORT).show()
                        }
                    }

                    // Edit Button (Visible for owners)
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    if (currentUser != null && artist.createdBy == currentUser.uid) {
                        binding.btnEdit.visibility = View.VISIBLE
                        binding.btnEdit.setOnClickListener {
                            val intent = Intent(this, CreateTroupeActivity::class.java)
                            intent.putExtra("EDIT_MODE", true)
                            intent.putExtra("ARTIST_ID", artistId)
                            startActivity(intent)
                        }
                    } else {
                        binding.btnEdit.visibility = View.GONE
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
    }
}
