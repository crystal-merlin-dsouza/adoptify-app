package com.example.adoptify

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.adoptify.adapters.PetAdapter

class ListedPetActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PetAdapter
    private lateinit var databaseHelper: DatabaseHelper
    private var userId: Int = -1 // Declare userId as a class variable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listed_pet)

        recyclerView = findViewById(R.id.recyclerViewListedPets)
        recyclerView.layoutManager = LinearLayoutManager(this)

        databaseHelper = DatabaseHelper(this)

        // Access userId from SharedPreferences
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        userId = sharedPreferences.getInt("userId", -1) // Retrieve userId and assign it to the class variable

        if (userId == -1) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show()
            finish() // Exit if no user ID is found
            return
        }

        loadListedPets() // Call to load pets
    }

    override fun onResume() {
        super.onResume()
        loadListedPets() // Reload pets when the activity is resumed
    }

    private fun loadListedPets() {
        if (userId == -1) return // Ensure userId is valid

        val pets = databaseHelper.getListedPetsByUser(userId)

        if (pets.isEmpty()) {
            Toast.makeText(this, "No pets found for this user.", Toast.LENGTH_SHORT).show()
        } else {
            val petDataList = pets.map { pet ->
                PetData(
                    petId = pet.petId,
                    petName = pet.petName,
                    petBreed = pet.petBreed,
                    petAge = pet.petAge,
                    petGender = pet.petGender,
                    petLocation = pet.petLocation,
                    petContact = pet.petContact,
                    petVaccinated = pet.petVaccinated,
                    //petMedicalHistory = pet.petMedicalHistory,
                    petPhoto = pet.petPhoto
                )
            }

            adapter = PetAdapter(petDataList) { petData ->
                markAsAdopted(petData)
            }
            recyclerView.adapter = adapter
        }
    }

    private fun markAsAdopted(pet: PetData) {
        val isDeleted = databaseHelper.removePet(pet.petId.toString())
        if (isDeleted) {
            Toast.makeText(this, "${pet.petName} has been adopted!", Toast.LENGTH_SHORT).show()
            loadListedPets() // Reload the list with the current userId
        } else {
            Toast.makeText(this, "Failed to adopt ${pet.petName}.", Toast.LENGTH_SHORT).show()
        }
    }
}
