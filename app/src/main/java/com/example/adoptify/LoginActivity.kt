package com.example.adoptify

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.adoptify.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if user is already logged in
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        // If logged in, redirect to MainActivity
        if (isLoggedIn) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        // Inflate the login layout
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set click listener for the signup redirect button
        binding.signupRedirect.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
            finish()
        }

        // Set click listener for the login button
        binding.loginButton.setOnClickListener {
            val username = binding.loginUsername.text.toString().trim()
            val password = binding.loginPassword.text.toString().trim()

            // Validate input
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Both fields are required", Toast.LENGTH_SHORT).show()
            } else {
                // Validate user credentials with the database
                val db = DatabaseHelper(this)
                Log.d("LoginActivity", "Username: $username, Password: $password")

                if (db.validateUser(username, password)) {
                    // Get user details after successful validation
                    val user = db.getUserDetails(username)

                    if (user != null) {
                        // Save login state in SharedPreferences
                        with(sharedPreferences.edit()) {
                            putBoolean("isLoggedIn", true)
                            putString("username", user.username)
                            putString("email", user.email)
                            putInt("userId", user.userId) // Storing userId for future use
                            apply()
                        }

                        // Redirect to MainActivity with user details
                        val intent = Intent(this, MainActivity::class.java).apply {
                            putExtra("username", user.username)
                            putExtra("email", user.email)
                            putExtra("userId", user.userId) // Passing userId to MainActivity
                        }
                        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                        startActivity(intent)
                        finish()
                    } else {
                        Log.e("LoginActivity", "User not found in database")
                        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
