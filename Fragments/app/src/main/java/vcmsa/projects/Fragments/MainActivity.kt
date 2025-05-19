/* package vcmsa.projects.Fragments

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Initialize views
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        toolbar = findViewById(R.id.tool_bar)

        setSupportActionBar(toolbar)

        // Setup Drawer toggle
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,

        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener(this)

        // Bottom nav item selection
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, Home::class.java))
                    true
                }
                R.id.nav_expenses -> {
                    startActivity(Intent(this, Expense_Entry::class.java))
                    true
                }
                R.id.nav_goals -> {
                    startActivity(Intent(this, Budget_Goals::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, User_Profile::class.java))
                    true
                }
                else -> false
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> startActivity(Intent(this, Home::class.java))
            R.id.nav_expenses -> startActivity(Intent(this, Expense_Entry::class.java))
            R.id.nav_goals -> startActivity(Intent(this, Budget_Goals::class.java))
            R.id.nav_profile -> startActivity(Intent(this, User_Profile::class.java))
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
*/

package vcmsa.projects.Fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import vcmsa.projects.Fragments.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var toolbar: Toolbar
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize views
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        toolbar = findViewById(R.id.tool_bar)
        auth = Firebase.auth

        setSupportActionBar(toolbar)


        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.closeNavigationDrawer,
            R.string.openingNavigationDrawer
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.bringToFront()
        navigationView.setNavigationItemSelectedListener(this)

        // Bottom nav item selection
        bottomNavigationView.setOnItemSelectedListener { item ->
            handleBottomNavigationSelection(item)
            true
        }


    }

    private fun handleBottomNavigationSelection(item: MenuItem) {
        when (item.itemId) {
            R.id.nav_home -> {
                startActivity(Intent(this, Home::class.java))
            }
            R.id.nav_expenses -> {
                startActivity(Intent(this, Expense_Entry::class.java))
            }
            R.id.nav_goals -> {
                startActivity(Intent(this, Budget_Goals::class.java))
            }
            R.id.nav_profile -> {
                // Load the ProfileFragment
                val profileFragment = ProfileFragment()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.nav_host_fragment, profileFragment)
                    .commit()
            }
            R.id.nav_logout -> {
                Log.d("MainActivity", "Drawer menu logout clicked")
                performLogout()
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> startActivity(Intent(this, Home::class.java))
            R.id.nav_expenses -> startActivity(Intent(this, Expense_Entry::class.java))
            R.id.nav_goals -> startActivity(Intent(this, Budget_Goals::class.java))
            R.id.nav_profile -> {
                // Load the ProfileFragment
                val profileFragment = ProfileFragment()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.nav_host_fragment, profileFragment)
                    .commit()
            }
            R.id.nav_logout -> {
                Log.d("MainActivity", "Bottom nav logout clicked")
                performLogout()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun performLogout() {
        Log.d("MainActivity", "performLogout() called")
        auth.signOut()
        Toast.makeText(this, "Logged Out", Toast.LENGTH_SHORT).show()
        // Redirect to Login Activity and clear back stack
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish() // Close MainActivity
    }
}
