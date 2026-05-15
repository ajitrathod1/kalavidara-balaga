package com.example.kalavidara_balaga

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.kalavidara_balaga.adapter.GalleryAdapter
import com.example.kalavidara_balaga.databinding.ActivityGalleryBinding

class GalleryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGalleryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGalleryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val images = intent.getStringArrayListExtra("IMAGES") ?: arrayListOf()
        val videos = intent.getStringArrayListExtra("VIDEOS") ?: arrayListOf()

        setupGallery(images, videos)
    }

    private fun setupGallery(images: List<String>, videos: List<String>) {
        val allMedia = mutableListOf<Pair<String, Boolean>>() // URL, isVideo
        images.forEach { allMedia.add(it to false) }
        videos.forEach { allMedia.add(it to true) }

        if (allMedia.isEmpty()) {
            // Toast or show empty state if needed
        }

        binding.rvGallery.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            adapter = GalleryAdapter(allMedia)
        }
    }
}