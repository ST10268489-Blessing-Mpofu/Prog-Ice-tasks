package vcmsa.projects.Fragments

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import vcmsa.projects.Fragments.databinding.ActivitySignupBinding
import java.util.regex.Pattern

class SignupActivity : AppCompatActivity() {


    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var profileDao: ProfileDao
    // --- Password Complexity Regex ---
    // You can adjust these patterns as needed
    private val passwordMinLength = 8 // Increased minimum length
    private val passwordRequiresUppercase = true
    private val passwordRequiresLowercase = true
    private val passwordRequiresDigit = true
    private val passwordRequiresSpecialChar = true
    private val passwordSpecialChars = "@#$%^&+=!" // Define allowed special chars

    private val uppercasePattern: Pattern = Pattern.compile("[A-Z]")
    private val lowercasePattern: Pattern = Pattern.compile("[a-z]")
    private val digitPattern: Pattern = Pattern.compile("[0-9]")
    private val specialCharPattern: Pattern = Pattern.compile("[$passwordSpecialChars]")
    // --- End Password Complexity Regex ---

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = Firebase.auth
        // Get DAO instance from Room Database Singleton
        profileDao = AppDatabase.getDatabase(applicationContext).profileDao()

        binding.btnSignUp.setOnClickListener {
            performSignUp()
        }

        binding.tvLoginLink.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        // --- Set up Terms & Conditions Link ---
        binding.tvViewTermsLink.setOnClickListener {
            showTermsDialog()
        }
        // Make the checkbox itself (which is disabled) non-clickable visually too
        binding.cbTerms.isClickable = false
    }

    private fun performSignUp() {
        val firstName = binding.etFirstName.text.toString().trim() // Get first name
        val lastName = binding.etLastName.text.toString().trim()   // Get last name
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val termsAccepted = binding.cbTerms.isChecked

        // --- Input Validations ---
        if (firstName.isEmpty()) { // Add validation for first name
            binding.etFirstName.error = getString(R.string.error_first_name_required)
            binding.etFirstName.requestFocus()
            return
        }

        if (lastName.isEmpty()) { // Add validation for last name
            binding.etLastName.error = getString(R.string.error_last_name_required)
            binding.etLastName.requestFocus()
            return
        }

        if (email.isEmpty()) {
            binding.etEmail.error = getString(R.string.error_email_required)
            binding.etEmail.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = getString(R.string.error_invalid_email)
            binding.etEmail.requestFocus()
            return
        }

        if (!isPasswordValid(password)) {
            binding.etPassword.requestFocus()
            return
        }

        if (!termsAccepted) {
            Toast.makeText(
                baseContext,
                getString(R.string.error_terms_required),
                Toast.LENGTH_LONG
            ).show()
            return
        }
        // --- End Input Validations ---


        setLoading(true) // Show progress bar

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val firebaseUser = task.result?.user
                    if (firebaseUser?.email != null) {
                        val userEmail = firebaseUser.email!! // Non-null asserted by check
                        val defaultBio = getString(R.string.default_bio)

                        // Create the UserProfile Entity
                        val newUserProfile = UserProfile(
                            email = userEmail,
                            firstName = firstName,
                            lastName = lastName,
                            bio = defaultBio
                        )

                        // --- Save profile using Room in a Coroutine ---
                        lifecycleScope.launch(Dispatchers.IO) { // Use IO dispatcher for DB operations
                            try {
                                profileDao.insertOrUpdateProfile(newUserProfile)
                                // Profile saved successfully in the background
                                // We can navigate immediately on the main thread
                            } catch (e: Exception) {
                                // Log the error (use a proper logging framework)
                                Log.e("SignupActivity", "Error saving profile to Room", e)
                                // Show error on main thread (optional)
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        baseContext,
                                        getString(R.string.profile_creation_failed),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                // Decide if navigation should still happen despite DB error
                            }
                        }
                        // --- Navigation can happen immediately after launching coroutine ---
                        setLoading(false) // Hide progress bar
                        Toast.makeText(
                            baseContext, getString(R.string.signup_success),
                            Toast.LENGTH_SHORT
                        ).show()
                        navigateToMain() // Proceed to main activity

                    } else {
                        setLoading(false)
                        Toast.makeText(baseContext, "Sign up succeeded but user data unavailable.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    // Firebase sign up failed
                    setLoading(false)
                    val errorMessage = task.exception?.message ?: "Unknown error"
                    Toast.makeText(
                        baseContext, getString(R.string.auth_failed, errorMessage),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java) // Assume MainActivity hosts fragments
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish() // Finish SignUpActivity
    }

    /**
     * Validates the password against complexity rules.
     * Sets error on the password field if invalid.
     * @return true if valid, false otherwise.
     */
    private fun isPasswordValid(password: String): Boolean {
        binding.etPassword.error = null // Clear previous error

        if (password.isEmpty()) {
            binding.etPassword.error = getString(R.string.error_password_required)
            return false
        }

        if (password.length < passwordMinLength) {
            binding.etPassword.error = getString(R.string.error_password_too_short)
            return false
        }

        if (passwordRequiresUppercase && !uppercasePattern.matcher(password).find()) {
            binding.etPassword.error = getString(R.string.error_password_needs_uppercase)
            return false
        }

        if (passwordRequiresLowercase && !lowercasePattern.matcher(password).find()) {
            binding.etPassword.error = getString(R.string.error_password_needs_lowercase)
            return false
        }

        if (passwordRequiresDigit && !digitPattern.matcher(password).find()) {
            binding.etPassword.error = getString(R.string.error_password_needs_digit)
            return false
        }

        if (passwordRequiresSpecialChar && !specialCharPattern.matcher(password).find()) {
            binding.etPassword.error = getString(R.string.error_password_needs_special)
            return false
        }

        // Add any other rules here

        return true // Password is valid
    }

    /**
     * Displays the Terms and Conditions dialog.
     * Sets the checkbox state based on user action.
     */
    private fun showTermsDialog() {
        val termsTextView = android.widget.TextView(this).apply {
            // Use Html.fromHtml for basic formatting defined in strings.xml
            // Note: For newer Android versions (N+), use the version with FROM_HTML_MODE_LEGACY
            text = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                Html.fromHtml(getString(R.string.terms_content), Html.FROM_HTML_MODE_LEGACY)
            } else {
                @Suppress("DEPRECATION")
                Html.fromHtml(getString(R.string.terms_content))
            }
            movementMethod = LinkMovementMethod.getInstance() // Allows clicking links if any are in the HTML
            val padding = resources.getDimensionPixelSize(com.google.android.material.R.dimen.mtrl_btn_text_btn_icon_padding) // Use material padding
            setPadding(padding, padding/2, padding, padding/2) // Add some padding
            // Optional: Make the text scrollable if it's very long
            // movementMethod = ScrollingMovementMethod()
        }

        // Create a ScrollView to handle potentially long terms content
        val scrollView = android.widget.ScrollView(this).apply {
            addView(termsTextView)
        }


        AlertDialog.Builder(this)
            .setTitle(R.string.terms_dialog_title)
            // Set the ScrollView containing the TextView as the view
            .setView(scrollView)
            // Consider making the dialog non-cancelable until a choice is made
            // .setCancelable(false)
            .setPositiveButton(R.string.terms_dialog_accept) { dialog, _ ->
                binding.cbTerms.isChecked = true // Set checkbox to checked on Accept
                dialog.dismiss()
            }
            .setNegativeButton(R.string.terms_dialog_decline) { dialog, _ ->
                binding.cbTerms.isChecked = false // Ensure checkbox is unchecked on Decline
                dialog.dismiss()
                // Optional: Show a message that they must accept to sign up
                // Toast.makeText(this, "You must accept the terms to sign up.", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun setLoading(isLoading: Boolean) {
        binding.btnSignUp.isEnabled = !isLoading
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        // Optionally disable other inputs while loading
        binding.etEmail.isEnabled = !isLoading
        binding.etPassword.isEnabled = !isLoading
        binding.tvViewTermsLink.isEnabled = !isLoading
        binding.tvLoginLink.isEnabled = !isLoading

    }
}