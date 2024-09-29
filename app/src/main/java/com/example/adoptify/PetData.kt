package com.example.adoptify

// Data class for Pet
data class PetData(
    val petId: Int,
    val petName: String,
    val petBreed: String,
    val petAge: Int,
    val petGender: String,
    val petLocation: String,
    val petContact: String,
    val petVaccinated: String,
    //val petMedicalHistory: String,
    val petPhoto: ByteArray?
)