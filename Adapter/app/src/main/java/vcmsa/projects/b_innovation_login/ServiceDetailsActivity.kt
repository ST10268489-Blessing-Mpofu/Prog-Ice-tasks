package vcmsa.projects.b_innovation_login

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity


class ServiceDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service_details)

        // Find the views in the layout
        val serviceImage = findViewById<ImageView>(R.id.service_detail_image)
        val serviceName = findViewById<TextView>(R.id.service_detail_name)
        val serviceDescription = findViewById<TextView>(R.id.service_detail_description)
        val backButton = findViewById<Button>(R.id.back_button)

        // Retrieve the data passed from the previous activity
        val name = intent.getStringExtra("SERVICE_NAME")
        val description = intent.getStringExtra("SERVICE_DESCRIPTION")
        val imageResId = intent.getIntExtra("SERVICE_IMAGE", R.drawable.num2) // Default image

        // Set the values in the UI components
        serviceName.text = name
        serviceDescription.text = description
        serviceImage.setImageResource(imageResId)

        // Handle the back button click to finish the activity
        backButton.setOnClickListener {
            finish() // Close this activity and go back to the previous one
        }
    }
}
