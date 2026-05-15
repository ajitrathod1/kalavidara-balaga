package com.example.kalavidara_balaga

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kalavidara_balaga.databinding.ActivityRoleSelectionBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class RoleSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRoleSelectionBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoleSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cardArtist.setOnClickListener {
            saveRoleAndMoveOn("artist")
        }

        binding.cardBooker.setOnClickListener {
            saveRoleAndMoveOn("booker")
        }
    }

    private fun saveRoleAndMoveOn(role: String) {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "Session expired, please login again", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Show progress and disable interactions
        binding.progressBar.visibility = View.VISIBLE
        binding.cardArtist.isEnabled = false
        binding.cardBooker.isEnabled = false

        val userId = user.uid
        val userData = hashMapOf(
            "role" to role,
            "email" to (user.email ?: ""),
            "name" to (user.displayName ?: "")
        )

        db.collection("users").document(userId)
            .set(userData, SetOptions.merge())
            .addOnSuccessListener {
                if (role == "artist") {
                    startActivity(Intent(this, CreateTroupeActivity::class.java))
                } else {
                    startActivity(Intent(this, MainActivity::class.java))
                }
                finish()
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                binding.cardArtist.isEnabled = true
                binding.cardBooker.isEnabled = true
                Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("RoleSelection", "Error saving role", e)
            }
    }
}