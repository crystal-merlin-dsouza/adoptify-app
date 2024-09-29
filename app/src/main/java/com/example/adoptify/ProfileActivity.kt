package com.example.adoptify

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {
    private lateinit var userName: TextView
    private lateinit var userEmail: TextView
    private lateinit var listedPetsButton: Button
    private lateinit var logout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Initialize views
        userName = findViewById(R.id.usernameTextView)
        userEmail = findViewById(R.id.emailTextView)
        listedPetsButton = findViewById(R.id.listedPetsTextView)
        logout = findViewById(R.id.logoutButton)

        // Retrieve user details from SharedPreferences
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "Guest") ?: "Guest"
        val email = sharedPreferences.getString("email", "No email provided") ?: "No email provided"

        // Log the received values
        Log.d("ProfileActivity", "Retrieved username: $username, email: $email")

        // Display user details
        userName.text = "Username: $username\n"
        userEmail.text = "Email: $email"

        // Set up button to navigate to the listed pets page
        listedPetsButton.setOnClickListener {
            val userId = sharedPreferences.getInt("userId", -1) // Retrieve user ID, default to -1 if not found
            val intent = Intent(this, ListedPetActivity::class.java)
            intent.putExtra("userId", userId)  // Pass the user ID
            startActivity(intent)
        }

        logout.setOnClickListener {
            Log.d("MainActivity", "Logout function called") // Debugging

            // Clear user session from SharedPreferences
            val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            sharedPreferences.edit().clear().apply() // Clear all saved data

            // Redirect to SignupActivity
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish() // Close MainActivity
        }
    }
}
