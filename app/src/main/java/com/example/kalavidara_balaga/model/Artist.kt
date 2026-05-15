package com.example.kalavidara_balaga.model

import com.google.firebase.Timestamp

data class Artist(
    val id: String = "",
    val name: String = "",
    val location: String = "",
    val district: String = "", // Added
    val artForm: String = "",
    val imageUrl: String? = null,
    val groupSize: String = "",
    val membersCount: String = "", // Added
    val experience: String = "",
    val leadPerson: String = "", // Added
    val phone: String = "",
    val email: String = "", // Added
    val languages: String = "",
    val status: String = "pending",
    val createdBy: String = "",
    val timestamp: Timestamp? = null,
    val equipment: String = "",
    val serviceArea: String = "",
    val about: String = "",
    val galleryImages: List<String> = emptyList(), // Added
    val galleryVideos: List<String> = emptyList() // Added
)