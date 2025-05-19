package vcmsa.projects.Fragments

import android.content.Intent
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
import vcmsa.projects.Fragments.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private val TAG = "LoginActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        Log.d(TAG, "onCreate called.")

        auth = Firebase.auth

        // Don't check currentUser here in onCreate, do it in onStart

        binding.btnLogin.setOnClickListener {
            performLogin()
        }

        binding.tvSignUpLink.setOnClickListener {
            Log.d(TAG, "Sign Up link clicked.")
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        binding.tvForgotPassword.setOnClickListener {
            Log.d(TAG, "Forgot Password link clicked.")
            // Start the ForgotPasswordActivity
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
            // DO NOT call finish() here - we want the user to be able to come back
        }
    }

    // Check if user is already logged in when the activity starts
    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        Log.d("LoginActivity", "onStart called. Current user: ${currentUser?.email}") // <-- Add Log
        if (currentUser != null) {
            Log.d("LoginActivity", "User detected, navigating to Main.") // <-- Add Log
            navigateToMain()
        } else {
            Log.d("LoginActivity", "User is null, staying on Login.") // <-- Add Log
        }
    }

    private fun performLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        // --- Input Validations ---
        if (email.isEmpty()) {
            binding.etEmail.error = "Email is required"
            binding.etEmail.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Please enter a valid email"
            binding.etEmail.requestFocus()
            return
        }

        if (password.isEmpty()) {
            binding.etPassword.error = "Password is required"
            binding.etPassword.requestFocus()
            return
        }
        // --- End Input Validations ---

        setLoading(true) // Show progress bar

        Log.d(TAG, "Attempting login for ${binding.etEmail.text}")
        setLoading(true)
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                setLoading(false)
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithEmail:success")
                    Toast.makeText(baseContext, "Login Successful.", Toast.LENGTH_SHORT).show()
                    navigateToMain()
                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed: ${task.exception?.message}",
                        Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        // Clear back stack
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish() // Finish LoginActivity
    }


    private fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.btnLogin.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.btnLogin.isEnabled = true
            binding.progressBar.visibility = View.GONE
        }
    }
}