package com.example.adoptify

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.adoptify.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if user is logged in
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        if (!isLoggedIn) {
            // If the user is not logged in, redirect to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Close MainActivity
            return // Exit the onCreate method
        }

        // Inflate the layout using ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get user details from the intent
        val username = intent.getStringExtra("username") ?: "Guest"
        val email = intent.getStringExtra("email") ?: "No email provided"
        val userId = intent.getIntExtra("userId", -1)

        // Set up the other buttons
        binding.btnListPet.setOnClickListener {
            val intent = Intent(this, pet::class.java)
            startActivity(intent)
        }

        binding.btnViewPet.setOnClickListener {
            val intent = Intent(this, ListActivity::class.java)
            startActivity(intent)
        }
        // Set up Bottom Navigation item selection listener
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Handle home action, possibly refreshing the current activity
                    true
                }
                R.id.nav_profile -> {
                    // Navigate to profile activity
                    val intent = Intent(this, ProfileActivity::class.java).apply {
                        putExtra("username", username)
                        putExtra("email", email)
                        putExtra("userId", userId)
                    }
                    startActivity(intent)
                    true
                }
                R.id.nav_view -> {
                    // Navigate to view pets list activity
                    val intent = Intent(this, ListActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_list_pet -> {
                    // Navigate to list a pet activity
                    val intent = Intent(this, pet::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

}
