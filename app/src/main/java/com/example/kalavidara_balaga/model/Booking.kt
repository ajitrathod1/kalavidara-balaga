package com.example.kalavidara_balaga.model

import com.google.firebase.Timestamp

data class Booking(
    var id: String = "",
    val artistId: String = "",
    val bookerId: String = "",
    val artistName: String = "",
    val artistImage: String? = null,
    val eventType: String = "",
    val eventDate: String = "",
    val eventLocation: String = "",
    val duration: String = "",
    val budget: String = "",
    val message: String = "",
    val artistResponse: String = "", // Message from artist when accepting/rejecting
    val status: String = "pending", // pending, accepted, rejected, completed
    val timestamp: Timestamp? = null
)