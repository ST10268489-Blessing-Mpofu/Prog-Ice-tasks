package vcmsa.projects.b_innovation_login

import android.os.Bundle
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import vcmsa.projects.b_innovation_login.adapters.ServiceAdapter
import vcmsa.projects.b_innovation_login.models.Service



class Listviewdemo : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listviewdemo) // Make sure this layout exists

        // Find ListView in the layout
        val listView: ListView = findViewById(R.id.B_innovatonServices)

        // Sample data for the services
        val services = listOf(
            Service("Web Development", "Build modern websites and applications", R.drawable.web_dev),
            Service("Graphic Design", "Create stunning visuals and branding", R.drawable.graphic_design),
            Service("App Development", "Develop Android and iOS applications", R.drawable.app_dev),
            Service("Digital Marketing", "Grow your business with SEO and social media", R.drawable.digital_marketing)
        )

        // Set up the adapter
        val adapter = ServiceAdapter(this, services)
        listView.adapter = adapter
    }
}
