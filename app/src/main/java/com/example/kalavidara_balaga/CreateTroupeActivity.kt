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
    private var selectedPresetUrl: String? = null
    private val galleryImageUris = mutableListOf<Uri>()
    private val videoUris = mutableListOf<Uri>()
    
    private var isEditMode = false
    private var existingArtistId: String? = null
    private var membersCount = 10

    private val profileImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            profileImageUri = uri
            selectedPresetUrl = null
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
            if (validateFields()) {
                startComplexUpload()
            }
        }
    }

    private fun setupPresets() {
        val dolluUrl = "https://images.unsplash.com/photo-1582373468547-52646399ba71?auto=format&fit=crop&q=80&w=600"
        val yakshaganaUrl = "https://images.unsplash.com/photo-1621360841013-c7683c659ec6?auto=format&fit=crop&q=80&w=600"
        val veeragaseUrl = "https://images.unsplash.com/photo-1614613535308-eb5fbd3d2c17?auto=format&fit=crop&q=80&w=600"

        val img1 = binding.preset1.getChildAt(0) as android.widget.ImageView
        val img2 = binding.preset2.getChildAt(0) as android.widget.ImageView
        val img3 = binding.preset3.getChildAt(0) as android.widget.ImageView

        Glide.with(this).load(dolluUrl).centerCrop().into(img1)
        Glide.with(this).load(yakshaganaUrl).centerCrop().into(img2)
        Glide.with(this).load(veeragaseUrl).centerCrop().into(img3)

        binding.preset1.setOnClickListener {
            selectedPresetUrl = dolluUrl
            profileImageUri = null
            Glide.with(this).load(dolluUrl).into(binding.ivTroupeImage)
            Toast.makeText(this, "Dollu Kunitha Theme Selected", Toast.LENGTH_SHORT).show()
        }
        binding.preset2.setOnClickListener {
            selectedPresetUrl = yakshaganaUrl
            profileImageUri = null
            Glide.with(this).load(yakshaganaUrl).into(binding.ivTroupeImage)
            Toast.makeText(this, "Yakshagana Theme Selected", Toast.LENGTH_SHORT).show()
        }
        binding.preset3.setOnClickListener {
            selectedPresetUrl = veeragaseUrl
            profileImageUri = null
            Glide.with(this).load(veeragaseUrl).into(binding.ivTroupeImage)
            Toast.makeText(this, "Veeragase Theme Selected", Toast.LENGTH_SHORT).show()
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
                        Glide.with(this).load(imageUrl).into(binding.ivTroupeImage)
                    }
                }
            }
    }

    private fun validateFields(): Boolean {
        var isValid = true
        
        // Reset errors
        binding.tilTroupeName.error = null
        binding.tilArtForm.error = null
        binding.tilDistrict.error = null
        binding.tilLeadPerson.error = null
        binding.tilPhone.error = null
        binding.tilAbout.error = null
        binding.tilExperience.error = null
        binding.tilEquipment.error = null
        binding.tilServiceArea.error = null

        if (binding.etTroupeName.text.toString().trim().isEmpty()) {
            binding.tilTroupeName.error = "Name is required"
            isValid = false
        }
        if (binding.spinnerArtForm.text.toString().isEmpty()) {
            binding.tilArtForm.error = "Art form is required"
            isValid = false
        }
        if (binding.spinnerDistrict.text.toString().isEmpty()) {
            binding.tilDistrict.error = "District is required"
            isValid = false
        }
        if (binding.etLeadPerson.text.toString().trim().isEmpty()) {
            binding.tilLeadPerson.error = "Lead person name is required"
            isValid = false
        }
        val phone = binding.etPhone.text.toString().trim()
        if (phone.isEmpty()) {
            binding.tilPhone.error = "Phone number is required"
            isValid = false
        } else if (phone.length < 10) {
            binding.tilPhone.error = "Enter valid 10-digit number"
            isValid = false
        }
        if (binding.etAbout.text.toString().trim().isEmpty()) {
            binding.tilAbout.error = "About section is required"
            isValid = false
        }
        if (binding.spinnerExperience.text.toString().isEmpty()) {
            binding.tilExperience.error = "Experience is required"
            isValid = false
        }
        if (binding.etEquipment.text.toString().trim().isEmpty()) {
            binding.tilEquipment.error = "Equipment list is required"
            isValid = false
        }
        if (binding.etServiceArea.text.toString().trim().isEmpty()) {
            binding.tilServiceArea.error = "Service area is required"
            isValid = false
        }

        if (!isValid) {
            Toast.makeText(this, "Please fix highlighted errors", Toast.LENGTH_SHORT).show()
        }
        
        return isValid
    }

    private fun startComplexUpload() {
        val userId = auth.currentUser?.uid ?: return

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
            submitData(selectedPresetUrl, emptyList(), emptyList())
        } else {
            Tasks.whenAllComplete(uploadTasks).addOnCompleteListener { 
                var profileUrl = selectedPresetUrl
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
            "status" to "approved",
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
