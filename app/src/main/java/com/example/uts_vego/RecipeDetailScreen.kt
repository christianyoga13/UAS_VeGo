package com.example.uts_vego

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun RecipeDetailScreen(recipeId: Long) {
    val firestore = FirebaseFirestore.getInstance()
    var recipe by remember { mutableStateOf<Recipe?>(null) }

    LaunchedEffect(recipeId) {
        firestore.collection("recipes")
            .whereEqualTo("timestamp", recipeId) // Pastikan field cocok dengan parameter
            .get()
            .addOnSuccessListener { snapshot ->
                recipe = snapshot.documents.firstOrNull()?.toObject(Recipe::class.java)
            }
            .addOnFailureListener { e ->
                println("Failed to fetch recipe detail: ${e.message}")
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = recipe?.content ?: "Recipe Detail") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            recipe?.let {
                AsyncImage(
                    model = it.imageUrl,
                    contentDescription = it.content,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = it.content, style = MaterialTheme.typography.h6)
                Text(text = "By: ${it.username}", style = MaterialTheme.typography.body2)
            } ?: Text("Loading...")
        }
    }
}

