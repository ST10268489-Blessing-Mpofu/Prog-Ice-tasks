package vcmsa.projects.b_innovation_login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    lateinit var usernameInput: EditText //used to declare  non-nullable variable
    lateinit var passwordInput: EditText
    lateinit var loginBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
            usernameInput = findViewById(R.id.username_input)
            passwordInput = findViewById(R.id.password_input)
            loginBtn = findViewById(R.id.login_btn)

            loginBtn.setOnClickListener {
                val username = usernameInput.text.toString()
                val password = passwordInput.text.toString()
                //                Log.i("Test Credetials", "Username: $username and Password: $password")
                Toast.makeText(this,"Welcome $username",Toast.LENGTH_LONG).show()
                val homeIntent : Intent =Intent(this@MainActivity,Listviewdemo::class.java)//this is how you call the next page
                startActivity(homeIntent)

            }



    }
}