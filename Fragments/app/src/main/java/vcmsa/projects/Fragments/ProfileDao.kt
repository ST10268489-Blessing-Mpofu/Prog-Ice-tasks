package vcmsa.projects.Fragments

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ProfileDao {

    // --- Insert / Update ---
    // OnConflictStrategy.REPLACE: If a profile with the same primary key (email)
    // exists, it will be replaced. This handles both inserts and updates easily.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfile) // Use suspend for coroutines

    // --- Read ---
    // Get profile by email, wrapped in LiveData for automatic UI updates
    @Query("SELECT * FROM profiles WHERE email = :email LIMIT 1")
    fun getProfileByEmailLiveData(email: String): LiveData<UserProfile?> // Nullable if not found

    // Get profile by email (non-LiveData version, useful for one-off checks)
    @Query("SELECT * FROM profiles WHERE email = :email LIMIT 1")
    suspend fun getProfileByEmail(email: String): UserProfile? // Suspend function

    // --- Delete (Optional) ---
    @Query("DELETE FROM profiles WHERE email = :email")
    suspend fun deleteProfileByEmail(email: String)

    // You could also add @Delete taking a UserProfile object
    // @Delete
    // suspend fun deleteProfile(profile: UserProfile)

    // --- Generic Read All (Example for future use) ---
    // @Query("SELECT * FROM profiles ORDER BY last_name ASC")
    // fun getAllProfilesLiveData(): LiveData<List<UserProfile>>
}