package com.example.uts_vego

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load // Using Coil for image loading

class RecipeAdapter(private val recipes: List<Recipe>, private val onClick: (Recipe) -> Unit) :
    RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    // To track expanded/collapsed state of each recipe
    private val expandedState = mutableListOf<Boolean>().apply {
        // Initialize with false (collapsed state)
        repeat(recipes.size) { add(false) }
    }

    class RecipeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val recipeContent: TextView = view.findViewById(R.id.text_recipe_content)
        val recipeImage: ImageView = view.findViewById(R.id.image_recipe)
        val readMoreButton: Button = view.findViewById(R.id.btn_show_more)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipes[position]
        holder.recipeContent.text = recipe.content // Set the content

        // Set initial state for collapsed or expanded content
        if (expandedState[position]) {
            holder.recipeContent.maxLines = Int.MAX_VALUE // No limit to lines when expanded
            holder.readMoreButton.text = "Read less" // Change button text when expanded
            holder.recipeImage.visibility = View.VISIBLE // Show image if expanded
        } else {
            holder.recipeContent.maxLines = 3 // Limit to 3 lines for collapsed state
            holder.readMoreButton.text = "Read more" // Set button text for collapsed state
            holder.recipeImage.visibility = View.GONE // Hide image when collapsed
        }

        if (recipe.imageUrl.isNotEmpty()) {
            holder.recipeImage.load(recipe.imageUrl) // Load image using Coil
        } else {
            holder.recipeImage.visibility = View.GONE // Hide if no URL
        }

        // Toggle recipe expansion/collapse when button is clicked
        holder.readMoreButton.setOnClickListener {
            expandedState[position] = !expandedState[position] // Toggle the state
            notifyItemChanged(position) // Notify adapter to update the item
        }

        // Handle item click (if necessary for navigation or other actions)
        holder.itemView.setOnClickListener { onClick(recipe) }
    }

    override fun getItemCount(): Int {
        return recipes.size
    }
}
