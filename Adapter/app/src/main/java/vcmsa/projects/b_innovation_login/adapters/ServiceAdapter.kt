package vcmsa.projects.b_innovation_login.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import vcmsa.projects.b_innovation_login.R
import vcmsa.projects.b_innovation_login.ServiceDetailsActivity

import vcmsa.projects.b_innovation_login.models.Service

class ServiceAdapter(private val context: Context, private val services: List<Service>) : BaseAdapter() {

    override fun getCount(): Int = services.size
    override fun getItem(position: Int): Any = services[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        // Inflate the item layout (activity_service_item.xml)
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.activity_service_item, parent, false)

        val service = services[position]
        val serviceImage = view.findViewById<ImageView>(R.id.service_detail_image)
        val serviceName = view.findViewById<TextView>(R.id.service_detail_name)

        // Set the service image and name
        serviceImage.setImageResource(service.imageResId)
        serviceName.text = service.name

        // Set an OnClickListener to handle the item click
        view.setOnClickListener {
            // Pass the data to the next activity (ServiceDetailsActivity)
            val intent = Intent(context, ServiceDetailsActivity::class.java).apply {
                putExtra("SERVICE_NAME", service.name)
                putExtra("SERVICE_DESCRIPTION", service.description)
                putExtra("SERVICE_IMAGE", service.imageResId)
            }
            context.startActivity(intent)
        }

        return view
    }
}
