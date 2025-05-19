package vcmsa.projects.Fragments

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "UserProfile.db"
        private const val TABLE_PROFILES = "profiles"

        // Column Names
        private const val KEY_EMAIL = "email" // Treat email as primary key for simplicity here
        private const val KEY_FIRST_NAME = "first_name"
        private const val KEY_LAST_NAME = "last_name"
        private const val KEY_BIO = "bio"
        // Add other columns if needed

        private const val TAG = "DatabaseHelper"
    }

    // Create Table Query
    private val CREATE_TABLE_PROFILES = ("CREATE TABLE " + TABLE_PROFILES + "("
            + KEY_EMAIL + " TEXT PRIMARY KEY," // Email is unique identifier
            + KEY_FIRST_NAME + " TEXT,"
            + KEY_LAST_NAME + " TEXT,"
            + KEY_BIO + " TEXT" + ")")

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(CREATE_TABLE_PROFILES)
        Log.d(TAG, "Database table created: $TABLE_PROFILES")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Drop older table if existed (Simple upgrade strategy)
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_PROFILES")
        // Create tables again
        onCreate(db)
        Log.w(TAG, "Database upgraded from v$oldVersion to v$newVersion")
    }

    /**
     * Adds a new user profile to the database.
     * Returns true if successful, false otherwise.
     */
    fun addProfile(email: String, firstName: String, lastName: String, bio: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_EMAIL, email)
            put(KEY_FIRST_NAME, firstName)
            put(KEY_LAST_NAME, lastName)
            put(KEY_BIO, bio)
        }

        var success = false
        try {
            // Inserting Row, returns -1 if error
            val result = db.insertOrThrow(TABLE_PROFILES, null, values)
            success = result != -1L
            if (success) {
                Log.d(TAG, "Profile added successfully for email: $email")
            } else {
                Log.e(TAG, "Failed to add profile for email: $email, result: $result")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error adding profile for email: $email", e)
            success = false
        } finally {
            db.close() // Close the database connection
        }
        return success
    }

    /**
     * Retrieves a user profile based on email.
     * Returns UserProfile object or null if not found or error.
     */
    fun getProfileByEmail(email: String): UserProfile? {
        val db = this.readableDatabase
        var userProfile: UserProfile? = null
        val cursor = db.query(
            TABLE_PROFILES, // Table
            arrayOf(KEY_EMAIL, KEY_FIRST_NAME, KEY_LAST_NAME, KEY_BIO), // Columns
            "$KEY_EMAIL = ?", // WHERE clause
            arrayOf(email), // WHERE arguments
            null, null, null, "1" // Limit 1
        )

        try {
            if (cursor != null && cursor.moveToFirst()) {
                userProfile = UserProfile(
                    email = cursor.getString(cursor.getColumnIndexOrThrow(KEY_EMAIL)),
                    firstName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_FIRST_NAME)),
                    lastName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_LAST_NAME)),
                    bio = cursor.getString(cursor.getColumnIndexOrThrow(KEY_BIO))
                )
                Log.d(TAG, "Profile retrieved successfully for email: $email")
            } else {
                Log.w(TAG, "Profile not found for email: $email")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving profile for email: $email", e)
            userProfile = null // Ensure null is returned on error
        } finally {
            cursor?.close() // Close the cursor
            db.close()    // Close the database connection
        }
        return userProfile
    }

    /**
     * Updates an existing user profile.
     * Returns true if successful, false otherwise.
     */
    fun updateProfile(email: String, firstName: String, lastName: String, bio: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_FIRST_NAME, firstName)
            put(KEY_LAST_NAME, lastName)
            put(KEY_BIO, bio)
        }

        var success = false
        try {
            // Updating row
            val rowsAffected = db.update(
                TABLE_PROFILES,
                values,
                "$KEY_EMAIL = ?",
                arrayOf(email)
            )
            success = rowsAffected > 0
            if (success) {
                Log.d(TAG, "Profile updated successfully for email: $email")
            } else {
                Log.w(TAG, "Failed to update profile or no changes for email: $email, rows affected: $rowsAffected")
                // It might return 0 if the data provided is the same as existing data
                // Consider checking if the profile exists before attempting update if 0 rows is an error
                success = getProfileByEmail(email) != null // Check if profile exists if 0 rows affected
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating profile for email: $email", e)
            success = false
        } finally {
            db.close() // Close the database connection
        }
        return success
    }

}