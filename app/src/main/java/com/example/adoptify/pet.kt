package com.example.adoptify

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.adoptify.databinding.ActivityPetBinding
import java.io.ByteArrayOutputStream

class pet : AppCompatActivity() {

    private lateinit var binding: ActivityPetBinding
    private lateinit var dbHelper: DatabaseHelper
    private var petId: Int = 0
    private var petPhoto: ByteArray? = null // To hold pet photo as ByteArray
    private var userId: Int = -1 // Variable to hold userId

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)

        // Retrieve userId from SharedPreferences
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        userId = sharedPreferences.getInt("userId", -1) // Get userId

        // Check if editing an existing pet
        petId = intent.getIntExtra("PET_ID", 0)

        if (petId != 0) {
            loadPetData(petId)
        }

        binding.btnsubmit.setOnClickListener { savePetData() }
        binding.btnimg.setOnClickListener { openFilePicker() } // Open file picker instead of gallery
    }

    private fun loadPetData(petId: Int) {
        val cursor = dbHelper.readPet(petId)
        cursor?.use {
            if (it.moveToFirst()) {
                binding.edname.setText(it.getString(it.getColumnIndex(DatabaseHelper.COLUMN_PET_NAME)))
                binding.edbreed.setText(it.getString(it.getColumnIndex(DatabaseHelper.COLUMN_PET_BREED)))
                binding.edage.setText(it.getInt(it.getColumnIndex(DatabaseHelper.COLUMN_PET_AGE)).toString())
                val gender = it.getString(it.getColumnIndex(DatabaseHelper.COLUMN_PET_GENDER))
                binding.radioMale.isChecked = gender == "Male"
                binding.radioFemale.isChecked = gender == "Female"
                binding.edlocation.setText(it.getString(it.getColumnIndex(DatabaseHelper.COLUMN_PET_LOCATION)))

              //  val medicalHistory = it.getString(it.getColumnIndex(DatabaseHelper.COLUMN_PET_MEDICAL_HISTORY))
            //    binding.edmedicalhistory.setText(medicalHistory)

                val vaccinated = it.getString(it.getColumnIndex(DatabaseHelper.COLUMN_PET_VACCINATED))
                binding.radioYes.isChecked = vaccinated == "Yes"
                binding.radioNo.isChecked = vaccinated == "No"

                // Load the photo
                val photoBlob = it.getBlob(it.getColumnIndex(DatabaseHelper.COLUMN_PET_PHOTO))
                petPhoto = photoBlob
                if (photoBlob != null) {
                    val bitmap = BitmapFactory.decodeByteArray(photoBlob, 0, photoBlob.size)
                    binding.imageButton.setImageBitmap(bitmap)
                }
            }
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*" // Only show images
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), FILE_PICKER_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                binding.imageButton.setImageBitmap(bitmap)
                petPhoto = bitmapToByteArray(bitmap) // Convert Bitmap to ByteArray
            }
        }
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    private fun savePetData() {
        val name = binding.edname.text.toString().trim()
        val breed = binding.edbreed.text.toString().trim()
        val age = binding.edage.text.toString().toIntOrNull() ?: 0
        val gender = when {
            binding.radioMale.isChecked -> "Male"
            binding.radioFemale.isChecked -> "Female"
            else -> ""
        }
        val location = binding.edlocation.text.toString().trim()
      //  val medicalHistory = binding.edmedicalhistory.text.toString().trim()
        val vaccinated = when {
            binding.radioYes.isChecked -> "Yes"
            binding.radioNo.isChecked -> "No"
            else -> ""
        }
        val contact = binding.edcontact.text.toString().trim() // Collect contact number from EditText

        // Validate required fields
        if (name.isEmpty() || breed.isEmpty() || gender.isEmpty() || location.isEmpty() || contact.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val pet = PetData(petId, name, breed, age, gender, location, contact, vaccinated, petPhoto)

        if (petId == 0) {
            // Insert new pet
            val newRowId = dbHelper.insertPet(
                pet.petName,
                pet.petBreed,
                pet.petAge,
                pet.petGender,
                pet.petLocation,
                pet.petContact, // Pass contact
                userId, // Pass userId when inserting a new pet
                getCurrentDate(), // Date listed
                pet.petVaccinated,
             //   pet.petMedicalHistory,
                pet.petPhoto
            )
            if (newRowId != -1L) {
                Toast.makeText(this, "Pet added successfully!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Error adding pet!", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Update existing pet
            val rowsAffected = dbHelper.updatePet(
                pet.petId,
                pet.petName,
                pet.petBreed,
                pet.petAge,
                pet.petGender,
                pet.petLocation,
                pet.petContact, // Pass contact
                getCurrentDate(), // Date listed
                pet.petVaccinated, // Vaccination status
               // pet.petMedicalHistory, // Medical history placeholder
                pet.petPhoto
            )
            if (rowsAffected > 0) {
                Toast.makeText(this, "Pet updated successfully!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Error updating pet!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getCurrentDate(): String {
        val calendar = java.util.Calendar.getInstance()
        val year = calendar.get(java.util.Calendar.YEAR)
        val month = calendar.get(java.util.Calendar.MONTH) + 1 // Months are 0-indexed
        val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
        return "$day/$month/$year" // Format: dd/mm/yyyy
    }

    companion object {
        private const val FILE_PICKER_REQUEST_CODE = 1
    }
}
