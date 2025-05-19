package vcmsa.projects.Fragments

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [UserProfile::class], // List all entities here
    version = 1,                      // Increment version on schema changes
    exportSchema = false              // Set true for production & migrations
)
abstract class AppDatabase : RoomDatabase() {

    // Abstract function for each DAO
    abstract fun profileDao(): ProfileDao
    // Add other DAOs here (e.g., abstract fun budgetDao(): BudgetDao)

    companion object {
        // Volatile ensures visibility across threads
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private const val DATABASE_NAME = "AppLocalDatabase.db" // New name is fine

        fun getDatabase(context: Context): AppDatabase {
            // Return existing instance or create a new one synchronously
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    // **IMPORTANT for Development:** Use fallbackToDestructiveMigration
                    // This clears the DB if schema changes. Replace with proper migrations
                    // using .addMigrations(...) for production releases.
                    .fallbackToDestructiveMigration()
                    // Optional: Allow main thread queries (AVOID IN PRODUCTION!)
                    // .allowMainThreadQueries()
                    .build()
                INSTANCE = instance
                // return instance
                instance // Return the created instance
            }
        }
    }
}