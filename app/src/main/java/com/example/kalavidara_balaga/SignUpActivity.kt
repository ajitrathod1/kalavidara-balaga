package com.example.kalavidara_balaga

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kalavidara_balaga.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnSignUp.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                if (password.length < 6) {
                    Toast.makeText(this, "Password should be at least 6 characters", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                binding.btnSignUp.isEnabled = false
                binding.btnSignUp.text = "Creating Account..."

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val userId = auth.currentUser?.uid
                            
                            val userMap = hashMapOf(
                                "name" to name,
                                "email" to email,
                                "role" to "user" // Default role for everyone
                            )

                            if (userId != null) {
                                db.collection("users").document(userId)
                                    .set(userMap)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
                                        startActivity(Intent(this, MainActivity::class.java))
                                        finishAffinity() // Clear activity stack
                                    }
                                    .addOnFailureListener { e ->
                                        binding.btnSignUp.isEnabled = true
                                        binding.btnSignUp.text = "Sign Up"
                                        Toast.makeText(this, "Error saving user data: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        } else {
                            binding.btnSignUp.isEnabled = true
                            binding.btnSignUp.text = "Sign Up"
                            Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvLogin.setOnClickListener {
            finish()
        }
    }
}