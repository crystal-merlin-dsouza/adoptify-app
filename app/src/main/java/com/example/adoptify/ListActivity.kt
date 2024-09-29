package com.example.adoptify

import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adoptify.databinding.ActivityListBinding

class ListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityListBinding
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var db: SQLiteDatabase
    private lateinit var itemArrayList: ArrayList<DataSet>
    private lateinit var adapter: ItmAdapter

    // UI Elements for Filtering
    private lateinit var spinnerPetType: Spinner
    private lateinit var spinnerLocation: Spinner
    private lateinit var buttonApplyFilter: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseHelper = DatabaseHelper(this)
        db = databaseHelper.readableDatabase


        Log.d("ListActivity", "Database initialized successfully.")

        binding.itmList.layoutManager = LinearLayoutManager(this)

        binding.itmList.setHasFixedSize(true)
        itemArrayList = arrayListOf()

        adapter = ItmAdapter(itemArrayList)
        binding.itmList.adapter = adapter
        adapter.setOnItemClickListener(listener = object : ItmAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val itmPos = itemArrayList[position]
                val intent = Intent(this@ListActivity, PetDetailsActivity::class.java).apply {
                    putExtra("pet_name", itmPos.petname)
                    putExtra("pet_breed", itmPos.petbreed)
                    putExtra("pet_age", itmPos.petage)
                    putExtra("pet_gender", itmPos.petgender)
                    putExtra("pet_contact", itmPos.petcontact.toString())
                    putExtra("pet_location", itmPos.petlocation)
                    putExtra("pet_photo", itmPos.petphoto) // Pass the byte array
                }

                Log.d("ListActivity", "Passing image size: ${itmPos.petphoto.size} bytes")
                startActivity(intent)
            }

            override fun onAdoptClick(position: Int) {
                val itmPos = itemArrayList[position]
                val userId = getUserId()  // Replace with the actual logic to retrieve the user ID

                val intent = Intent(this@ListActivity, ProfileActivity::class.java).apply {
                    putExtra("PET_NAME", itmPos.petname)  // Pass the pet name
                    putExtra("USER_ID", userId)           // Pass the user ID
                }

                Log.d("ListActivity", "Starting AdoptFormActivity with PET_NAME: ${itmPos.petname}, USER_ID: $userId")
                startActivity(intent)
            }
        })

        // Initialize Filter UI Elements via binding
        spinnerPetType = findViewById(R.id.spinnerPetType)
        spinnerLocation = findViewById(R.id.spinnerLocation)
        buttonApplyFilter = findViewById(R.id.buttonApplyFilter)

        // Populate Spinners with Data
        populatePetTypeSpinner()
        populateLocationSpinner()

        // Set OnClickListener for Filter Button
        buttonApplyFilter.setOnClickListener {
            val selectedPetType = spinnerPetType.selectedItem.toString()
            val selectedLocation = spinnerLocation.selectedItem.toString()

            // Apply filters; pass null if "All" is selected
            val petTypeFilter = if (selectedPetType != "All") selectedPetType else null
            val locationFilter = if (selectedLocation != "All") selectedLocation else null

            Log.d("ListActivity", "Applying filter - Pet Type: $petTypeFilter, Location: $locationFilter")

            showDataList(petTypeFilter, locationFilter)
        }


        // Initially display all pets
        showDataList()
    }
    private fun getUserId(): Int {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        return sharedPreferences.getInt("USER_ID", -1)  // Return -1 if no user ID is found
    }

    private fun populatePetTypeSpinner() {
        try {
            val petTypes = mutableListOf("All")

            val cursor = databaseHelper.getDistinctPetBreeds()
            if (cursor.moveToFirst()) {
                do {
                    val type = cursor.getString(cursor.getColumnIndexOrThrow("petbreed"))
                    petTypes.add(type.capitalize())
                } while (cursor.moveToNext())
            }
            cursor.close()

            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, petTypes)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerPetType.adapter = adapter

            Log.d("ListActivity", "Pet types spinner populated with ${petTypes.size} items.")
        } catch (ex: Exception) {
            Toast.makeText(this, "Error loading pet types: ${ex.message}", Toast.LENGTH_SHORT).show()
            Log.e("ListActivity", "Error loading pet types", ex)
        }
    }

    private fun populateLocationSpinner() {
        try {
            val locations = mutableListOf("All")

            val cursor = databaseHelper.getDistinctPetLocations()
            if (cursor.moveToFirst()) {
                do {
                    val location = cursor.getString(cursor.getColumnIndexOrThrow("petlocation"))
                    locations.add(location.capitalize())
                } while (cursor.moveToNext())
            }
            cursor.close()

            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, locations)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerLocation.adapter = adapter

            Log.d("ListActivity", "Location spinner populated with ${locations.size} items.")
        } catch (ex: Exception) {
            Toast.makeText(this, "Error loading locations: ${ex.message}", Toast.LENGTH_SHORT).show()
            Log.e("ListActivity", "Error loading locations", ex)
        }
    }

    private fun showDataList(petType: String? = null, location: String? = null) {
        try {
            itemArrayList.clear()

            // Log the filter criteria
            Log.d("ListActivity", "Filter criteria - Pet Type: ${petType ?: "All"}, Location: ${location ?: "All"}")

            val cursor = databaseHelper.getPets(petType, location)
            if (cursor.moveToFirst()) {
                do {
                    val pid = cursor.getInt(cursor.getColumnIndexOrThrow("petid"))
                    val pname = cursor.getString(cursor.getColumnIndexOrThrow("petname"))
                    val pbreed = cursor.getString(cursor.getColumnIndexOrThrow("petbreed"))
                    val page = cursor.getInt(cursor.getColumnIndexOrThrow("petage"))
                    val pgender = cursor.getString(cursor.getColumnIndexOrThrow("petgender"))
                    val pcontact = cursor.getInt(cursor.getColumnIndexOrThrow("petcontact"))
                    val ploc = cursor.getString(cursor.getColumnIndexOrThrow("petlocation"))
                    val imageBlob = cursor.getBlob(cursor.getColumnIndexOrThrow("petphoto"))

                    val safeImageBlob = imageBlob ?: ByteArray(0)

                    val itemDS = DataSet(pid, pname, pbreed, page, pgender, pcontact, ploc, safeImageBlob)
                    itemArrayList.add(itemDS)
                } while (cursor.moveToNext())
                cursor.close()

                Log.d("ListActivity", "Fetched ${itemArrayList.size} pets.")
                adapter.notifyDataSetChanged()
            } else {
                cursor.close()
                Toast.makeText(this, "No pets found for the selected criteria.", Toast.LENGTH_SHORT).show()
                Log.d("ListActivity", "No pets found for the selected criteria.")
                itemArrayList.clear()
                adapter.notifyDataSetChanged()
            }
        } catch (ex: Exception) {
            Toast.makeText(this, "Error fetching data: ${ex.message}", Toast.LENGTH_SHORT).show()
            Log.e("ListActivity", "Error fetching data", ex)
        }
    }

}
