package vcmsa.projects.Fragments

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles") // Define the table name
data class UserProfile(
    @PrimaryKey // Email is the unique identifier
    @ColumnInfo(name = "email") // Explicit column name
    val email: String,

    @ColumnInfo(name = "first_name")
    var firstName: String,

    @ColumnInfo(name = "last_name")
    var lastName: String,

    @ColumnInfo(name = "bio")
    var bio: String
    // Add other fields if needed, annotate with @ColumnInfo
)