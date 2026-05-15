package com.example.kalavidara_balaga

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.kalavidara_balaga.databinding.ActivityCreateTroupeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.tasks.Task

class CreateTroupeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateTroupeBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    
    private var profileImageUri: Uri? = null
    private var selectedPresetRes: String? = null
    private val galleryImageUris = mutableListOf<Uri>()
    private val videoUris = mutableListOf<Uri>()
    
    private var isEditMode = false
    private var existingArtistId: String? = null
    private var membersCount = 10

    private val profileImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            profileImageUri = uri
            selectedPresetRes = null
            binding.ivTroupeImage.setImageURI(uri)
        }
    }

    private val galleryImagesLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        galleryImageUris.clear()
        galleryImageUris.addAll(uris)
        binding.tvPhotoCount.text = "${uris.size} photos selected"
    }

    private val videosLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        videoUris.clear()
        videoUris.addAll(uris)
        binding.tvVideoCount.text = "${uris.size} videos selected"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateTroupeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isEditMode = intent.getBooleanExtra("EDIT_MODE", false)
        existingArtistId = intent.getStringExtra("ARTIST_ID")

        if (isEditMode) {
            binding.btnSubmitProfile.text = "Update Profile"
            fetchExistingTroupeData()
        }

        setupSpinners()
        setupMemberCounter()
        setupPresets()

        binding.btnBack.setOnClickListener { finish() }

        binding.btnUploadImage.setOnClickListener {
            profileImageLauncher.launch("image/*")
        }

        binding.btnAddGalleryPhoto.setOnClickListener {
            galleryImagesLauncher.launch("image/*")
        }

        binding.btnAddVideo.setOnClickListener {
            videosLauncher.launch("video/*")
        }

        binding.btnSubmitProfile.setOnClickListener {
            startComplexUpload()
        }
    }

    private fun setupPresets() {
        binding.preset1.setOnClickListener {
            selectedPresetRes = "res/drawable/folk_banner"
            profileImageUri = null
            binding.ivTroupeImage.setImageResource(R.drawable.folk_banner)
            Toast.makeText(this, "Banner theme selected", Toast.LENGTH_SHORT).show()
        }
        binding.preset2.setOnClickListener {
            selectedPresetRes = "res/drawable/about_banner"
            profileImageUri = null
            binding.ivTroupeImage.setImageResource(R.drawable.about_banner)
            Toast.makeText(this, "Traditional theme selected", Toast.LENGTH_SHORT).show()
        }
        binding.preset3.setOnClickListener {
            selectedPresetRes = "res/drawable/folk_illustration"
            profileImageUri = null
            binding.ivTroupeImage.setImageResource(R.drawable.folk_illustration)
            Toast.makeText(this, "Illustration theme selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupMemberCounter() {
        binding.tvMemberCount.text = membersCount.toString()
        binding.btnIncrement.setOnClickListener {
            membersCount++
            binding.tvMemberCount.text = membersCount.toString()
        }
        binding.btnDecrement.setOnClickListener {
            if (membersCount > 1) {
                membersCount--
                binding.tvMemberCount.text = membersCount.toString()
            }
        }
    }

    private fun setupSpinners() {
        val districts = arrayOf("Bagalkot", "Ballari", "Belagavi", "Bengaluru", "Bidar", "Chamarajanagar", "Chikkaballapur", "Chikkamagaluru", "Chitradurga", "Dakshina Kannada", "Davanagere", "Dharwad", "Gadag", "Hassan", "Haveri", "Kalaburagi", "Kodagu", "Kolar", "Koppal", "Mandya", "Mysuru", "Raichur", "Ramanagara", "Shivamogga", "Tumakuru", "Udupi", "Uttara Kannada", "Vijayapura", "Yadgir")
        val districtAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, districts)
        binding.spinnerDistrict.setAdapter(districtAdapter)

        val artForms = arrayOf("Dollu Kunitha", "Yakshagana", "Pooja Kunitha", "Veeragase", "Kamsale", "Suggi Kunitha", "Goravara Kunitha", "Bhootha Aradhane")
        val artFormAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, artForms)
        binding.spinnerArtForm.setAdapter(artFormAdapter)

        val expOptions = arrayOf("1-2 Years", "3-5 Years", "5-10 Years", "10-15 Years", "15+ Years")
        val expAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, expOptions)
        binding.spinnerExperience.setAdapter(expAdapter)
    }

    private fun fetchExistingTroupeData() {
        val artistId = existingArtistId ?: return
        db.collection("artists").document(artistId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    binding.etTroupeName.setText(doc.getString("name"))
                    binding.spinnerDistrict.setText(doc.getString("district"), false)
                    binding.spinnerArtForm.setText(doc.getString("artForm"), false)
                    binding.etLeadPerson.setText(doc.getString("leadPerson"))
                    binding.etPhone.setText(doc.getString("phone"))
                    binding.etEmail.setText(doc.getString("email"))
                    binding.etAbout.setText(doc.getString("about"))
                    binding.spinnerExperience.setText(doc.getString("experience"), false)
                    binding.etEquipment.setText(doc.getString("equipment"))
                    binding.etServiceArea.setText(doc.getString("serviceArea"))
                    
                    membersCount = doc.getString("membersCount")?.toIntOrNull() ?: 10
                    binding.tvMemberCount.text = membersCount.toString()

                    val imageUrl = doc.getString("imageUrl")
                    if (!imageUrl.isNullOrEmpty()) {
                        if (imageUrl.startsWith("res/")) {
                             val resPath = imageUrl.substringAfter("res/")
                             val resourceId = resources.getIdentifier(resPath, null, packageName)
                             if (resourceId != 0) binding.ivTroupeImage.setImageResource(resourceId)
                        } else {
                             Glide.with(this).load(imageUrl).into(binding.ivTroupeImage)
                        }
                    }
                }
            }
    }

    private fun startComplexUpload() {
        val userId = auth.currentUser?.uid ?: return
        
        val name = binding.etTroupeName.text.toString().trim()
        val district = binding.spinnerDistrict.text.toString()
        val artForm = binding.spinnerArtForm.text.toString()
        val leadPerson = binding.etLeadPerson.text.toString()
        val phone = binding.etPhone.text.toString()

        if (name.isEmpty() || district.isEmpty() || artForm.isEmpty() || leadPerson.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnSubmitProfile.isEnabled = false
        binding.btnSubmitProfile.text = "Processing..."

        val uploadTasks = mutableListOf<Task<Uri>>()

        profileImageUri?.let {
            val ref = storage.reference.child("troupes/${userId}/profile_${System.currentTimeMillis()}.jpg")
            uploadTasks.add(ref.putFile(it).continueWithTask { task -> 
                if (!task.isSuccessful) task.exception?.let { throw it }
                ref.downloadUrl 
            })
        }

        galleryImageUris.forEachIndexed { index, uri ->
            val ref = storage.reference.child("troupes/${userId}/gallery/img_${System.currentTimeMillis()}_$index.jpg")
            uploadTasks.add(ref.putFile(uri).continueWithTask { task -> 
                if (!task.isSuccessful) task.exception?.let { throw it }
                ref.downloadUrl 
            })
        }

        videoUris.forEachIndexed { index, uri ->
            val ref = storage.reference.child("troupes/${userId}/videos/vid_${System.currentTimeMillis()}_$index.mp4")
            uploadTasks.add(ref.putFile(uri).continueWithTask { task -> 
                if (!task.isSuccessful) task.exception?.let { throw it }
                ref.downloadUrl 
            })
        }

        if (uploadTasks.isEmpty()) {
            submitData(selectedPresetRes, emptyList(), emptyList())
        } else {
            Tasks.whenAllComplete(uploadTasks).addOnCompleteListener { 
                var profileUrl = selectedPresetRes
                val galleryUrls = mutableListOf<String>()
                val videoUrls = mutableListOf<String>()

                var currentTaskIdx = 0
                
                if (profileImageUri != null) {
                    if (uploadTasks[currentTaskIdx].isSuccessful) {
                        profileUrl = uploadTasks[currentTaskIdx].result.toString()
                    }
                    currentTaskIdx++
                }

                galleryImageUris.forEach { _ ->
                    if (uploadTasks[currentTaskIdx].isSuccessful) {
                        galleryUrls.add(uploadTasks[currentTaskIdx].result.toString())
                    }
                    currentTaskIdx++
                }

                videoUris.forEach { _ ->
                    if (uploadTasks[currentTaskIdx].isSuccessful) {
                        videoUrls.add(uploadTasks[currentTaskIdx].result.toString())
                    }
                    currentTaskIdx++
                }

                submitData(profileUrl, galleryUrls, videoUrls)
            }
        }
    }

    private fun submitData(profileUrl: String?, galleryUrls: List<String>, videoUrls: List<String>) {
        val name = binding.etTroupeName.text.toString().trim()
        val district = binding.spinnerDistrict.text.toString()
        val artForm = binding.spinnerArtForm.text.toString()
        val leadPerson = binding.etLeadPerson.text.toString()
        val phone = binding.etPhone.text.toString()
        val email = binding.etEmail.text.toString()
        val about = binding.etAbout.text.toString()
        val experience = binding.spinnerExperience.text.toString()
        val equipment = binding.etEquipment.text.toString()
        val serviceArea = binding.etServiceArea.text.toString()

        val userId = auth.currentUser?.uid ?: return

        val troupeData = hashMapOf(
            "name" to name,
            "district" to district,
            "artForm" to artForm,
            "leadPerson" to leadPerson,
            "phone" to phone,
            "email" to email,
            "about" to about,
            "experience" to experience,
            "membersCount" to membersCount.toString(),
            "equipment" to equipment,
            "serviceArea" to serviceArea,
            "galleryImages" to galleryUrls,
            "galleryVideos" to videoUrls,
            "createdBy" to userId,
            "status" to "approved", // Default approved for easy testing
            "timestamp" to com.google.firebase.Timestamp.now()
        )
        
        profileUrl?.let { troupeData["imageUrl"] = it }

        binding.btnSubmitProfile.text = "Saving..."

        val task = if (isEditMode && existingArtistId != null) {
            db.collection("artists").document(existingArtistId!!).update(troupeData as Map<String, Any>)
        } else {
            db.collection("artists").add(troupeData)
        }

        task.addOnSuccessListener {
            db.collection("users").document(userId)
                .set(mapOf("role" to "artist"), com.google.firebase.firestore.SetOptions.merge())
                .addOnCompleteListener {
                    Toast.makeText(this, "Profile Saved Successfully!", Toast.LENGTH_LONG).show()
                    finish()
                }
        }.addOnFailureListener { e ->
            Log.e("FirestoreError", "Error: ${e.message}")
            Toast.makeText(this, "Save Failed: ${e.message}", Toast.LENGTH_LONG).show()
            binding.btnSubmitProfile.isEnabled = true
            binding.btnSubmitProfile.text = if (isEditMode) "Update Profile" else "Create Profile ✨"
        }
    }
}