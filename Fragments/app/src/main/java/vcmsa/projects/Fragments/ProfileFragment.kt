package vcmsa.projects.Fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import vcmsa.projects.Fragments.databinding.DialogEditProfileBinding
import vcmsa.projects.Fragments.databinding.FragmentProfileBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {
    // Use nullable binding and handle null checks
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private var _dialogBinding: DialogEditProfileBinding? = null

    // Remove: private lateinit var dbHelper: DatabaseHelper
    private lateinit var profileDao: ProfileDao // Add DAO instance
    private lateinit var auth: FirebaseAuth

    private var currentUserEmail: String? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        // Get DAO instance from Room Database Singleton
        profileDao = AppDatabase.getDatabase(requireContext().applicationContext).profileDao()
        auth = Firebase.auth
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentUserEmail = auth.currentUser?.email

        if (currentUserEmail != null) {
            observeProfileData(currentUserEmail!!) // Start observing
        } else {
            // Handle not logged in state
            updateUiOnError("User not logged in.")
            Toast.makeText(requireContext(), "User not logged in.", Toast.LENGTH_SHORT).show()
        }

        binding.btnEditProfile.setOnClickListener {
            // Get the *current* profile state before showing dialog
            // We use a one-off suspend call here to get the latest data *right now*
            // for the dialog, avoiding potential stale data if LiveData hasn't updated yet.
            viewLifecycleOwner.lifecycleScope.launch { // Launch on main thread is fine here
                val profile = withContext(Dispatchers.IO) { // DB access on IO
                    currentUserEmail?.let { profileDao.getProfileByEmail(it) }
                }
                if (profile != null) {
                    showEditProfileDialog(profile) // Pass current profile to dialog
                } else {
                    Toast.makeText(requireContext(), "Profile data not available.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun observeProfileData(email: String) {
        // Observe the LiveData returned by Room
        profileDao.getProfileByEmailLiveData(email).observe(viewLifecycleOwner, Observer { profile ->
            // This block executes whenever the data for this email changes in the DB
            if (profile != null) {
                updateUi(profile)
            } else {
                // Profile not found in DB for this logged-in user
                updateUiOnError(getString(R.string.profile_fetch_failed))
                // Maybe the profile wasn't created? Or deleted?
                Log.w("ProfileFragment", "Profile data is null for email: $email")
            }
        })
    }

    private fun updateUi(profile: UserProfile) {
        binding.tvFullName.text = "${profile.firstName} ${profile.lastName}"
        binding.tvEmail.text = profile.email
        binding.tvBio.text = profile.bio
        binding.btnEditProfile.isEnabled = true // Enable edit button
    }

    private fun updateUiOnError(errorMessage: String) {
        binding.tvFullName.text = "N/A"
        binding.tvEmail.text = currentUserEmail ?: "N/A" // Show email if available
        binding.tvBio.text = errorMessage
        binding.btnEditProfile.isEnabled = false
    }

    // Pass the current profile data to pre-fill the dialog
    private fun showEditProfileDialog(profileToEdit: UserProfile) {
        _dialogBinding = DialogEditProfileBinding.inflate(LayoutInflater.from(requireContext()))
        val dialogView = _dialogBinding!!.root

        // Pre-populate dialog
        _dialogBinding?.etEditFirstName?.setText(profileToEdit.firstName)
        _dialogBinding?.etEditLastName?.setText(profileToEdit.lastName)
        _dialogBinding?.etEditBio?.setText(profileToEdit.bio)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.edit_profile_dialog_title))
            .setView(dialogView)
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
                _dialogBinding = null
            }
            .setPositiveButton(getString(R.string.save)) { dialog, _ ->
                val newFirstName = _dialogBinding?.etEditFirstName?.text.toString().trim()
                val newLastName = _dialogBinding?.etEditLastName?.text.toString().trim()
                val newBio = _dialogBinding?.etEditBio?.text.toString().trim()

                // Basic Validation (keep if needed)
                if (newFirstName.isEmpty() || newLastName.isEmpty()) {
                    Toast.makeText(requireContext(), "First and Last Name cannot be empty", Toast.LENGTH_SHORT).show()
                    // Optionally, prevent dialog closing here if you implement custom dialog logic
                    // return@setPositiveButton // (This won't work with standard builder)
                    _dialogBinding = null // Clean up binding even on validation fail for now
                    return@setPositiveButton
                }

                // Create updated entity - IMPORTANT: Use the original email!
                val updatedProfile = UserProfile(
                    email = profileToEdit.email, // Use the original primary key
                    firstName = newFirstName,
                    lastName = newLastName,
                    bio = newBio
                )

                // Update in database using Room in a Coroutine
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        profileDao.insertOrUpdateProfile(updatedProfile)
                        // Show success message on main thread
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), getString(R.string.profile_update_success), Toast.LENGTH_SHORT).show()
                        }
                        // LiveData observer will automatically update the UI, no need to call loadProfileData() manually
                    } catch (e: Exception) {
                        Log.e("ProfileFragment", "Error updating profile in Room", e)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), getString(R.string.profile_update_failed), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                dialog.dismiss() // Dismiss dialog after initiating save
                _dialogBinding = null // Clean up dialog binding
            }
            .setOnDismissListener {
                _dialogBinding = null // Ensure cleanup
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clean up fragment binding
    }
}