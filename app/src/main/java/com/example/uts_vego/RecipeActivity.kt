package com.example.uts_vego

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

// Define Recipe data class
data class Recipe(
    val content: String = "",
    val imageUrl: String = "",
    val username: String = "",
    val userId: String = "",
    val timestamp: Long = 0L
)

class RecipeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            RecipeListScreen(onAddRecipeClick = {
                val intent = Intent(this, Recipe2Activity::class.java)
                startActivity(intent)
            })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(onAddRecipeClick: () -> Unit) {
    val firestore = Firebase.firestore
    var recipes by remember { mutableStateOf<List<Recipe>>(emptyList()) }

    // Fetch recipes from Firestore
    LaunchedEffect(Unit) {
        firestore.collection("recipes")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val fetchedRecipes = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Recipe::class.java)
                }
                recipes = fetchedRecipes
            }
            .addOnFailureListener { e ->
                println("Error loading recipes: ${e.message}")
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Forum Recipe", color = Color.White) },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color(0xFFFFA500)),
                navigationIcon = {
                    IconButton(onClick = { /* Implement back action if needed */ }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddRecipeClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Recipe", tint = Color.White)
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            items(recipes) { recipe ->
                RecipeItem(recipe)
            }
        }
    }
}

@Composable
fun RecipeItem(recipe: Recipe) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "By: ${recipe.username}",
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = recipe.content,
                maxLines = if (expanded) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge
            )

            if (recipe.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = recipe.imageUrl,
                    contentDescription = "Recipe Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }

            TextButton(onClick = { expanded = !expanded }) {
                Text(if (expanded) "Read less" else "Read more")
            }
        }
    }
}
