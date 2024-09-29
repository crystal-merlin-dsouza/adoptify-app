package com.example.adoptify.com.example.adoptify

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.content.pm.PackageManager
import com.example.adoptify.DatabaseHelper
import com.example.adoptify.MainActivity
import com.example.adoptify.R

class AdoptFormActivity : AppCompatActivity() {

    private lateinit var etPreferredDate: EditText
    private lateinit var etMessage: EditText
    private lateinit var btnSubmit: Button
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var petName: String // New variable to hold pet name

    companion object {
        private const val SMS_PERMISSION_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adopt_app)

        // Initialize your views
        etPreferredDate = findViewById(R.id.et_adoption_date)
        etMessage = findViewById(R.id.et_reason_for_adoption)
        btnSubmit = findViewById(R.id.btn_submit_adopt)
        dbHelper = DatabaseHelper(this)

        // Retrieve pet name from the Intent
        petName = intent.getStringExtra("PET_NAME") ?: "Pet" // Default to "Pet" if not provided

        checkPermissions() // Check for SMS permissions

        btnSubmit.setOnClickListener {
            Log.d(
                "AdoptFormActivity",
                "Submit button clicked"
            ) // Add this line to check if the button is responding
            sendAdoptionRequest() // Proceed with the SMS process
        }

    }

    private fun checkPermissions() {
        if (checkSelfPermission(android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.SEND_SMS), SMS_PERMISSION_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("AdoptFormActivity", "SMS permission granted")
                Toast.makeText(this, "SMS permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Log.d("AdoptFormActivity", "SMS permission denied")
                Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun sendAdoptionRequest() {
        val preferredDate = etPreferredDate.text.toString()
        val message = etMessage.text.toString()

        // Validate user input
        if (preferredDate.isEmpty() || message.isEmpty()) {
            if (preferredDate.isEmpty()) {
                etPreferredDate.error = "Preferred adoption date is required"
            }
            if (message.isEmpty()) {
                etMessage.error = "Message is required"
            }
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Logging to ensure petName is retrieved from Intent
        Log.d("AdoptFormActivity", "Sending adoption request for pet: $petName")

        // Retrieve the pet owner's contact number from the database using the pet name
        val petOwnerContactNumber = dbHelper.getPetOwnerContactNumber(petName)

        Log.d("AdoptFormActivity", "Pet Owner's Contact Number: $petOwnerContactNumber")

        // Check if the contact number was retrieved successfully
        if (!petOwnerContactNumber.isNullOrEmpty()) {
            // Continue with form submission
            val fullMessage = "Interested in adopting $petName!\n" +
                    "Preferred Adoption Date: $preferredDate\n" +
                    "Message: $message"
            sendSMS(petOwnerContactNumber, fullMessage)
        } else {
            Toast.makeText(this, "Pet owner's contact number not found", Toast.LENGTH_SHORT).show()
            Log.e("AdoptFormActivity", "Failed to retrieve pet owner's contact number.")
        }
    }


    private fun sendSMS(phoneNumber: String?, message: String) {
        if (phoneNumber.isNullOrEmpty()) {
            Log.e(
                "AdoptFormActivity",
                "Contact number is empty. Cannot send SMS."
            ) // Log for empty contact
            Toast.makeText(this, "Contact number is empty. Cannot send SMS.", Toast.LENGTH_SHORT)
                .show()
            return
        }

        try {
            Log.d("AdoptFormActivity", "Sending SMS to: $phoneNumber")
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            Log.d("AdoptFormActivity", "SMS sent successfully!")
            Toast.makeText(this, "SMS sent successfully!", Toast.LENGTH_SHORT).show()
            // Navigate back to MainActivity after SMS is sent
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e("AdoptFormActivity", "SMS failed to send: ${e.message}")
            Toast.makeText(this, "SMS failed to send: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

}