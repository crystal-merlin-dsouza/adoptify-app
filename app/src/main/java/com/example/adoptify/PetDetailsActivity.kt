package com.example.adoptify

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.adoptify.com.example.adoptify.AdoptFormActivity
import java.io.File

object PetDataHolder {
    var petPhoto: ByteArray? = null
}

class PetDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pet_details)

        // Retrieve pet details from intent
        val petName = intent.getStringExtra("pet_name")
        val petBreed = intent.getStringExtra("pet_breed")
        val petAge = intent.getIntExtra("pet_age", 0)
        val petGender = intent.getStringExtra("pet_gender")
        val petVaccinated = intent.getBooleanExtra("pet_vaccinated", false)
        val petMedicalHistory = intent.getStringExtra("pet_medical_history")
        val petLocation = intent.getStringExtra("pet_location")
        val petPhotoPath = intent.getStringExtra("pet_photo_path")

        // Load the pet photo
        loadPetPhoto(petPhotoPath)

        // Display pet details in TextViews
        findViewById<TextView>(R.id.tv_pet_name).text = petName ?: "N/A"
        findViewById<TextView>(R.id.tv_pet_breed).text = petBreed ?: "N/A"
        findViewById<TextView>(R.id.tv_pet_age).text = petAge.toString()
        findViewById<TextView>(R.id.tv_pet_gender).text = petGender ?: "N/A"
        findViewById<TextView>(R.id.tv_pet_vaccination).text = if (petVaccinated) "Yes" else "No"
       // findViewById<TextView>(R.id.tv_pet_medical).text = petMedicalHistory ?: "N/A"
        findViewById<TextView>(R.id.tv_pet_location).text = petLocation ?: "N/A"

        Log.d("PetDetailsActivity", "Received Pet Name: $petName")
        Log.d("PetDetailsActivity", "Received Pet Breed: $petBreed")
        Log.d("PetDetailsActivity", "Received Pet Age: $petAge")
        Log.d("PetDetailsActivity", "Received Pet Gender: $petGender")
        Log.d("PetDetailsActivity", "Received Vaccinated: $petVaccinated")
        Log.d("PetDetailsActivity", "Received Medical History: $petMedicalHistory")
        Log.d("PetDetailsActivity", "Received Location: $petLocation")
        Log.d("PetDetailsActivity", "Received Photo Path: $petPhotoPath")

        // Set up the button to navigate to AdoptFormActivity
        val btnAdopt = findViewById<Button>(R.id.btn_interested_adopt)
        btnAdopt.setOnClickListener {
            Log.d("PetDetailsActivity", "Adopt button clicked")

            // Pass pet name to AdoptFormActivity
            val intent = Intent(this, AdoptFormActivity::class.java).apply {
                putExtra("PET_NAME", petName) // Pass the pet's name to AdoptFormActivity
            }
            startActivity(intent)
        }
    }

    private fun loadPetPhoto(photoPath: String?) {
        val imageView = findViewById<ImageView>(R.id.iv_pet_photo)

        // Check if a valid photo path is provided
        if (!photoPath.isNullOrEmpty()) {
            // Load image from file path
            val bitmap = BitmapFactory.decodeFile(photoPath)
            imageView.setImageBitmap(bitmap)
        } else {
            // If no valid photo path, check PetDataHolder for the byte array
            val petPhoto = PetDataHolder.petPhoto
            if (petPhoto != null) {
                // Save the image to the file system and display it
                val filePath = saveImageToFileSystem(petPhoto)
                val bitmap = BitmapFactory.decodeFile(filePath)
                imageView.setImageBitmap(bitmap)
            } else {
                // Handle case where no image data is available
                //imageView.setImageResource(R.drawable.placeholder_image) // Set a placeholder image
            }
        }
    }

    private fun saveImageToFileSystem(imageData: ByteArray): String {
        val file = File(filesDir, "pet_image_${System.currentTimeMillis()}.png")
        file.writeBytes(imageData)
        return file.absolutePath
    }
}
