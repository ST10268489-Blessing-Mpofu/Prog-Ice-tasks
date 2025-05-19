package vcmsa.projects.Fragments

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import vcmsa.projects.Fragments.databinding.ActivityForgotPasswordBinding

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var auth: FirebaseAuth
    private val TAG = "ForgotPasswordActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        Log.d(TAG, "onCreate called.")

        auth = Firebase.auth

        binding.btnSendResetLink.setOnClickListener {
            sendPasswordReset()
        }

        // Handle back navigation
        binding.ivBack.setOnClickListener {
            Log.d(TAG, "Back button clicked.")
            finish() // Closes this activity, goes back to the previous one (Login)
        }
    }
    private fun sendPasswordReset() {
        val email = binding.etEmail.text.toString().trim()

        if (!validateInput(email)) {
            return // Stop if validation fails
        }

        Log.d(TAG, "Attempting to send password reset email to: $email")
        setLoading(true)

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                setLoading(false) // Hide progress bar regardless of outcome
                if (task.isSuccessful) {
                    Log.d(TAG, "Password reset email sent successfully.")
                    Toast.makeText(
                        baseContext,
                        "Password reset link sent to your email.",
                        Toast.LENGTH_LONG
                    ).show()
                    // Optional: Automatically navigate back after a delay, or let user click back
                    finish() // Close the activity and return to Login screen
                } else {
                    Log.w(TAG, "sendPasswordResetEmail:failure", task.exception)
                    // Provide more specific feedback if possible
                    val errorMessage = task.exception?.message ?: "Unknown error occurred."
                    binding.tilEmail.error = "Failed: $errorMessage" // Show error on the input field
                    Toast.makeText(
                        baseContext,
                        "Failed to send reset link. $errorMessage",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun validateInput(email: String): Boolean {
        var isValid = true

        // Clear previous errors
        binding.tilEmail.error = null

        if (email.isEmpty()) {
            binding.tilEmail.error = "Email address is required"
            binding.etEmail.requestFocus()
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Please enter a valid email address"
            binding.etEmail.requestFocus()
            isValid = false
        }
        // Add other specific checks if needed (e.g., check if it looks like a valid domain)

        return isValid
    }


    private fun setLoading(isLoading: Boolean) {
        Log.d(TAG, "Setting loading state: $isLoading")
        binding.btnSendResetLink.isEnabled = !isLoading
        binding.etEmail.isEnabled = !isLoading // Disable input field during loading
        binding.ivBack.isEnabled = !isLoading // Disable back button during loading
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}