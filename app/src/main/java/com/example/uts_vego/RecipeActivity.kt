package com.example.uts_vego

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.ComposeView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

// Define Post data class
data class Recipe(
    val content: String = "",
    val imageUrl: String = "",
    val username: String = "",  // This field must exist
    val timestamp: Long = 0L
)

class RecipeActivity : ComponentActivity() {

    @SuppressLint("MissingInflatedId", "UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe) // Set the content view from XML

        // Setup RecyclerView
        val recyclerView: RecyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize Firestore
        val firestore = Firebase.firestore
        val recipes = mutableListOf<Recipe>()

        // Load recipes from Firestore
        firestore.collection("posts").orderBy("timestamp").get().addOnSuccessListener { snapshot ->
            recipes.clear()
            for (document in snapshot.documents) {
                val recipe = document.toObject(Recipe::class.java)
                recipe?.let { recipes.add(it) }
            }

            // Attach the adapter with data
            recyclerView.adapter = RecipeAdapter(recipes) { recipe ->
                // Handle click event here (e.g., navigate to details)
                println("Clicked recipe: ${recipe.content}") // Replace with your action
            }
        }
    }
}
