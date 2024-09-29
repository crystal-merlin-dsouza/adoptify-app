package com.example.adoptify

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.io.ByteArrayOutputStream
class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "adoptify.db"
        const val DATABASE_VERSION = 8

        // User table constants
        const val USER_TABLE_NAME = "users"
        const val COLUMN_USER_ID = "id"
        const val COLUMN_USERNAME = "username"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_PASSWORD = "password"

        // Pet table constants
        const val PET_TABLE_NAME = "pets"
        const val COLUMN_PET_ID = "petid"
        const val COLUMN_USER_ID_FK = "user_id" // Foreign key for user ID
        const val COLUMN_PET_NAME = "petname"
        const val COLUMN_PET_BREED = "petbreed"
        const val COLUMN_PET_AGE = "petage"
        const val COLUMN_PET_GENDER = "petgender"
        const val COLUMN_PET_LOCATION = "petlocation"
        const val COLUMN_PET_PHOTO = "petphoto"
        const val COLUMN_PET_CONTACT = "petcontact"
        const val COLUMN_PET_DATE_LISTED = "date_listed"
        const val COLUMN_PET_VACCINATED = "petvaccinated"
        //const val COLUMN_PET_MEDICAL_HISTORY = "petmedicalhistory"
        const val COLUMN_PET_ADOPTION_STATUS = "adoption_status"

        // Constants for Adoption Requests table
        const val ADOPTION_REQUEST_TABLE_NAME = "adoption_requests"
        const val COLUMN_ADOPTION_REQUEST_ID = "request_id"
        const val COLUMN_ADOPTION_PET_NAME = "pet_name"
        const val COLUMN_ADOPTION_DATE = "adoption_date"
        const val COLUMN_ADOPTION_MESSAGE = "adoption_message"
        const val COLUMN_ADOPTION_USER_CONTACT = "user_contact"
        const val COLUMN_ADOPTION_USER_EMAIL = "user_email"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createUserTableQuery = """
            CREATE TABLE $USER_TABLE_NAME (
                $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USERNAME TEXT UNIQUE,
                $COLUMN_EMAIL TEXT UNIQUE,
                $COLUMN_PASSWORD TEXT
            );
        """.trimIndent()

        val createPetTableQuery = """
            CREATE TABLE $PET_TABLE_NAME (
                $COLUMN_PET_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_ID_FK INTEGER, 
                $COLUMN_PET_NAME TEXT,
                $COLUMN_PET_BREED TEXT,
                $COLUMN_PET_AGE INTEGER,
                $COLUMN_PET_GENDER TEXT,
                $COLUMN_PET_LOCATION TEXT,
                $COLUMN_PET_PHOTO BLOB,
                $COLUMN_PET_CONTACT TEXT,
                $COLUMN_PET_DATE_LISTED TEXT,
                $COLUMN_PET_VACCINATED TEXT,
                $COLUMN_PET_ADOPTION_STATUS TEXT DEFAULT 'available',
                FOREIGN KEY($COLUMN_USER_ID_FK) REFERENCES $USER_TABLE_NAME($COLUMN_USER_ID)
            );
        """.trimIndent()

        db?.execSQL(createUserTableQuery)
        db?.execSQL(createPetTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $USER_TABLE_NAME")
        db?.execSQL("DROP TABLE IF EXISTS $PET_TABLE_NAME")
        onCreate(db)
    }

    // Insert user method
    fun insertUser(username: String, email: String, password: String): Long {
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, username)
            put(COLUMN_EMAIL, email)
            put(COLUMN_PASSWORD, password)
        }
        val db = writableDatabase
        return db.insert(USER_TABLE_NAME, null, values)
    }

    fun getUserDetails(username: String): User? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $USER_TABLE_NAME WHERE $COLUMN_USERNAME = ?", arrayOf(username))

        return if (cursor != null) {
            if (cursor.moveToFirst()) {
                val userId = cursor.getInt(cursor.getColumnIndex(COLUMN_USER_ID))
                val userName = cursor.getString(cursor.getColumnIndex(COLUMN_USERNAME))
                val email = cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL))

                Log.d("DatabaseHelper", "User found: ID = $userId, Username = $userName, Email = $email")

                User(userId, userName, email).also {
                    cursor.close()
                }
            } else {
                Log.e("DatabaseHelper", "No user found with username: $username")
                cursor.close()
                null
            }
        } else {
            Log.e("DatabaseHelper", "Cursor is null")
            null
        }
    }

    // Check if a user exists by username or email
    fun userExists(username: String? = null, email: String? = null): Boolean {
        val db = readableDatabase
        val selection = mutableListOf<String>()
        val selectionArgs = mutableListOf<String>()

        if (!username.isNullOrEmpty()) {
            selection.add("$COLUMN_USERNAME = ?")
            selectionArgs.add(username)
        }

        if (!email.isNullOrEmpty()) {
            selection.add("$COLUMN_EMAIL = ?")
            selectionArgs.add(email)
        }

        val cursor = db.query(
            USER_TABLE_NAME,
            null,
            selection.joinToString(" OR "),
            selectionArgs.toTypedArray(),
            null,
            null,
            null
        )

        val exists = cursor.moveToFirst()
        cursor.close()
        return exists
    }

    fun insertPet(
        petName: String,
        petBreed: String,
        petAge: Int,
        petGender: String,
        petLocation: String,
        petContact: String,
        userId: Int,
        dateListed: String,
        petVaccinated: String,
     //   petMedicalHistory: String,
        petPhoto: ByteArray?
    ): Long {
        val values = ContentValues().apply {
            put(COLUMN_PET_NAME, petName)
            put(COLUMN_PET_BREED, petBreed)
            put(COLUMN_PET_AGE, petAge)
            put(COLUMN_PET_GENDER, petGender)
            put(COLUMN_PET_LOCATION, petLocation)
            put(COLUMN_PET_CONTACT, petContact)
            put(COLUMN_USER_ID_FK, userId) // Use the correct foreign key constant
            put(COLUMN_PET_DATE_LISTED, dateListed)
            put(COLUMN_PET_VACCINATED, petVaccinated)
          //  put(COLUMN_PET_MEDICAL_HISTORY, petMedicalHistory)
            put(COLUMN_PET_PHOTO, petPhoto)
        }

        return writableDatabase.insert(PET_TABLE_NAME, null, values)
    }

    fun updatePet(
        petId: Int,
        petName: String,
        petBreed: String,
        petAge: Int,
        petGender: String,
        petLocation: String,
        petContact: String,
        dateListed: String,
        petVaccinated: String,
       // petMedicalHistory: String,
        petPhoto: ByteArray?
    ): Int {
        val values = ContentValues().apply {
            put(COLUMN_PET_NAME, petName)
            put(COLUMN_PET_BREED, petBreed)
            put(COLUMN_PET_AGE, petAge)
            put(COLUMN_PET_GENDER, petGender)
            put(COLUMN_PET_LOCATION, petLocation)
            put(COLUMN_PET_CONTACT, petContact)
            put(COLUMN_PET_DATE_LISTED, dateListed)
            put(COLUMN_PET_VACCINATED, petVaccinated)
         //   put(COLUMN_PET_MEDICAL_HISTORY, petMedicalHistory)
            put(COLUMN_PET_PHOTO, petPhoto)
        }

        return writableDatabase.update(PET_TABLE_NAME, values, "$COLUMN_PET_ID = ?", arrayOf(petId.toString()))
    }

    fun getUserId(username: String): Int? {
        val db = readableDatabase
        val cursor = db.query(
            USER_TABLE_NAME,
            arrayOf(COLUMN_USER_ID),
            "$COLUMN_USERNAME = ?",
            arrayOf(username),
            null,
            null,
            null
        )

        return if (cursor.moveToFirst()) {
            val userId = cursor.getInt(cursor.getColumnIndex(COLUMN_USER_ID))
            cursor.close()
            userId
        } else {
            cursor.close()
            null
        }
    }
    // Retrieve pet owner contact number from the pets table
    fun getPetOwnerContactNumber(petName: String): String? {
        val db = this.readableDatabase
        var contactNumber: String? = null

        val cursor = db.rawQuery("SELECT petcontact FROM pets WHERE petName = ?", arrayOf(petName))
        if (cursor.moveToFirst()) {
            contactNumber = cursor.getString(0)
            Log.d("DatabaseHelper", "Retrieved contact number: $contactNumber") // Log contact retrieval
        } else {
            Log.d("DatabaseHelper", "No contact number found for pet: $petName")
        }
        cursor.close()
        return contactNumber
    }



    // Assuming you're using SQLiteOpenHelper in your DatabaseHelper class
    fun getDistinctPetBreeds(): Cursor {
        return readableDatabase.rawQuery("SELECT DISTINCT $COLUMN_PET_BREED FROM $PET_TABLE_NAME", null)
    }

    fun getDistinctPetLocations(): Cursor {
        return readableDatabase.rawQuery("SELECT DISTINCT $COLUMN_PET_LOCATION FROM $PET_TABLE_NAME", null)
    }

    fun getPets(petType: String?, location: String?): Cursor {
        val database = this.readableDatabase
        val queryBuilder = StringBuilder("SELECT * FROM $PET_TABLE_NAME ")
        val whereClauses = mutableListOf<String>()
        val selectionArgs = mutableListOf<String>()

        if (!petType.isNullOrEmpty()) {
            whereClauses.add("LOWER($COLUMN_PET_BREED) = ?")
            selectionArgs.add(petType.lowercase())
        }

        if (!location.isNullOrEmpty()) {
            whereClauses.add("LOWER($COLUMN_PET_LOCATION) = ?")
            selectionArgs.add(location.lowercase())
        }

        if (whereClauses.isNotEmpty()) {
            queryBuilder.append(" WHERE ")
            queryBuilder.append(whereClauses.joinToString(" AND "))
        }

        // Ensure that pets are sorted by date listed, newest first
        queryBuilder.append(" ORDER BY $COLUMN_PET_ID DESC")

        Log.d("DatabaseHelper", "Executing query: $queryBuilder with arguments: ${selectionArgs.joinToString(", ")}")

        return database.rawQuery(queryBuilder.toString(), selectionArgs.toTypedArray())
    }



    // Retrieve pet details by ID
    fun readPet(petId: Int): Cursor? {
        val db = readableDatabase
        return db.rawQuery("SELECT * FROM $PET_TABLE_NAME WHERE $COLUMN_PET_ID = ?", arrayOf(petId.toString()))
    }

    fun deletePet(petId: Int) {
        val db = this.writableDatabase
        db.delete("pets", "petid = ?", arrayOf(petId.toString()))
    }

    fun getListedPetsByUser(userId: Int): List<PetData> {
        val pets = mutableListOf<PetData>()
        val db = this.readableDatabase
        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery("SELECT * FROM $PET_TABLE_NAME WHERE $COLUMN_USER_ID_FK = ?", arrayOf(userId.toString()))

            // Check if cursor is not empty
            if (cursor.moveToFirst()) {
                do {
                    val petId = cursor.getInt(cursor.getColumnIndex(COLUMN_PET_ID))
                    val petName = cursor.getString(cursor.getColumnIndex(COLUMN_PET_NAME))
                    val petBreed = cursor.getString(cursor.getColumnIndex(COLUMN_PET_BREED))
                    val petAge = cursor.getInt(cursor.getColumnIndex(COLUMN_PET_AGE))
                    val petGender = cursor.getString(cursor.getColumnIndex(COLUMN_PET_GENDER))
                    val petLocation = cursor.getString(cursor.getColumnIndex(COLUMN_PET_LOCATION))
                    val petContact = cursor.getString(cursor.getColumnIndex(COLUMN_PET_CONTACT))
                    val petVaccinated = cursor.getString(cursor.getColumnIndex(COLUMN_PET_VACCINATED))
                 //   val petMedicalHistory = cursor.getString(cursor.getColumnIndex(COLUMN_PET_MEDICAL_HISTORY))
                    val petPhoto = cursor.getBlob(cursor.getColumnIndex(COLUMN_PET_PHOTO))
                    val petStatus = cursor.getString(cursor.getColumnIndex(COLUMN_PET_ADOPTION_STATUS))

                    // Add the pet to the list
                    val pet = PetData(
                        petId,
                        petName,
                        petBreed,
                        petAge,
                        petGender,
                        petLocation,
                        petContact,
                        petVaccinated,
                       // petMedicalHistory,
                        petPhoto
                    )
                    pets.add(pet)
                } while (cursor.moveToNext())
            }
        } finally {
            cursor?.close() // Ensure cursor is closed to avoid memory leaks
        }

        return pets
    }

    fun removePet(petId: String): Boolean {
        val db = this.writableDatabase
        return db.delete("pets", "petid = ?", arrayOf(petId)) > 0
    }
    fun validateUser(username: String, password: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM users WHERE username = ? AND password = ?", arrayOf(username, password))
        val isValid = cursor.count > 0
        cursor.close()
        return isValid
    }
    // Assuming you're using SQLiteOpenHelper in your DatabaseHelper class
    fun getPetsListedByUser(userId: Int): Cursor {
        val db = this.readableDatabase
        val query = "SELECT * FROM pets WHERE user_id = ?"
        return db.rawQuery(query, arrayOf(userId.toString()))
    }
    fun updatePetAdoptionStatus(petId: Int, status: String) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("adoption_status", status)

        db.update("pets", values, "petid = ?", arrayOf(petId.toString()))
    }
}

