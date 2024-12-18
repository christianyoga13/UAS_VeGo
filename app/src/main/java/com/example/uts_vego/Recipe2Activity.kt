package com.example.uts_vego

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.uts_vego.ui.theme.UTSMobappTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class Recipe2Activity : ComponentActivity() {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UTSMobappTheme {
                Recipe2Screen(
                    modifier = Modifier.fillMaxSize(),
                    firestore = firestore,
                    storage = storage,
                    auth = auth,
                    context = this@Recipe2Activity
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Recipe2Screen(
    modifier: Modifier = Modifier,
    firestore: FirebaseFirestore,
    storage: FirebaseStorage,
    auth: FirebaseAuth,
    context: Context
) {
    var recipeContent by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // Register for the result of the image picker
    val getImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { imageUri = it }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Apa cerita anda hari ini?",
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = recipeContent,
            onValueChange = { recipeContent = it },
            label = { Text("Content", color = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = 16.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )

        imageUri?.let {
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(bottom = 16.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { getImageLauncher.launch("image/*") },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text("Add Image")
            }

            Button(
                onClick = {
                    if (recipeContent.isNotBlank()) {
                        if (imageUri != null) {
                            uploadRecipe2WithImage(recipeContent, imageUri!!, firestore, storage, context) {
                                recipeContent = ""
                                imageUri = null
                                showToast(context, "Recipe uploaded successfully")
                            }
                        } else {
                            uploadRecipe2WithoutImage(recipeContent, firestore, context) {
                                recipeContent = ""
                                imageUri = null
                                showToast(context, "Recipe uploaded successfully")
                            }
                        }
                    } else {
                        showToast(context, "Content cannot be empty")
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text("Post")
            }
        }
    }
}

// Function to upload recipe with image
private fun uploadRecipe2WithImage(
    recipeContent: String,
    imageUri: Uri,
    firestore: FirebaseFirestore,
    storage: FirebaseStorage,
    context: Context,
    onSuccess: () -> Unit
) {
    val imageRef: StorageReference = storage.reference.child("images/${System.currentTimeMillis()}.jpg")
    imageRef.putFile(imageUri).addOnSuccessListener {
        imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
            val recipe = hashMapOf(
                "content" to recipeContent,
                "imageUrl" to downloadUri.toString(),
                "timestamp" to System.currentTimeMillis(),
            )
            firestore.collection("recipes")
                .add(recipe)
                .addOnSuccessListener {
                    showToast(context, "Recipe with image uploaded successfully")
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    showToast(context, "Failed to upload recipe: ${e.message}")
                }
        }
    }.addOnFailureListener { e ->
        showToast(context, "Failed to upload image: ${e.message}")
    }
}

// Function to upload recipe without image
private fun uploadRecipe2WithoutImage(
    recipeContent: String,
    firestore: FirebaseFirestore,
    context: Context,
    onSuccess: () -> Unit
) {
    val recipe = hashMapOf(
        "content" to recipeContent,
        "imageUrl" to "", // No image URL
        "timestamp" to System.currentTimeMillis(),
    )
    firestore.collection("recipes")
        .add(recipe)
        .addOnSuccessListener {
            showToast(context, "Recipe without image uploaded successfully")
            onSuccess()
        }
        .addOnFailureListener { e ->
            showToast(context, "Failed to upload recipe: ${e.message}")
        }
}

// Function to show a toast message
fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}
