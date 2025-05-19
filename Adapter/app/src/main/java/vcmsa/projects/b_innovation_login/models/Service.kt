package vcmsa.projects.b_innovation_login.models


//This is how you do your data class
    data class Service(
        val name: String,
        val description: String,
        val imageResId: Int // Reference to a drawable resource
    )
