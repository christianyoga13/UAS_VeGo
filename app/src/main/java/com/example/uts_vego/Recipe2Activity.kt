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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
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

    val getImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { imageUri = it }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Share Your Vegan Recipe") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(paddingValues),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedTextField(
                value = recipeContent,
                onValueChange = { recipeContent = it },
                label = { Text("Recipe Content") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
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
                Button(onClick = { getImageLauncher.launch("image/*") }) {
                    Text("Add Image")
                }
                Button(
                    onClick = {
                        if (recipeContent.isNotBlank()) {
                            if (imageUri != null) {
                                uploadRecipe2WithImage(
                                    recipeContent,
                                    imageUri!!,
                                    firestore,
                                    storage,
                                    auth,
                                    context
                                ) {
                                    recipeContent = ""
                                    imageUri = null
                                    showToast(context, "Recipe uploaded successfully")
                                }
                            } else {
                                uploadRecipe2WithoutImage(
                                    recipeContent,
                                    firestore,
                                    auth,
                                    context
                                ) {
                                    recipeContent = ""
                                    imageUri = null
                                    showToast(context, "Recipe uploaded successfully")
                                }
                            }
                        } else {
                            showToast(context, "Content cannot be empty")
                        }
                    }
                ) {
                    Text("Post")
                }
            }
        }
    }
}

private fun uploadRecipe2WithImage(
    recipeContent: String,
    imageUri: Uri,
    firestore: FirebaseFirestore,
    storage: FirebaseStorage,
    auth: FirebaseAuth,
    context: Context,
    onSuccess: () -> Unit
) {
    val currentUser = auth.currentUser
    if (currentUser == null) {
        showToast(context, "User not authenticated")
        return
    }

    val userId = currentUser.uid
    val imageRef: StorageReference = storage.reference.child("images/${System.currentTimeMillis()}.jpg")
    imageRef.putFile(imageUri).addOnSuccessListener {
        imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { userDoc ->
                    val username = userDoc.getString("name") ?: "Unknown"
                    val recipe = hashMapOf(
                        "content" to recipeContent,
                        "imageUrl" to downloadUri.toString(),
                        "userId" to userId,
                        "username" to username,
                        "timestamp" to System.currentTimeMillis()
                    )
                    firestore.collection("recipes")
                        .add(recipe)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { e ->
                            showToast(context, "Failed to upload recipe: ${e.message}")
                        }
                }
                .addOnFailureListener { e ->
                    showToast(context, "Failed to fetch user: ${e.message}")
                }

        }
    }.addOnFailureListener { e ->
        showToast(context, "Failed to upload image: ${e.message}")
    }
}

private fun uploadRecipe2WithoutImage(
    recipeContent: String,
    firestore: FirebaseFirestore,
    auth: FirebaseAuth,
    context: Context,
    onSuccess: () -> Unit
) {
    val currentUser = auth.currentUser
    if (currentUser == null) {
        showToast(context, "User not authenticated")
        return
    }

    val userId = currentUser.uid
    firestore.collection("users").document(userId).get()
        .addOnSuccessListener { userDoc ->
            val username = userDoc.getString("name") ?: "Unknown"
            val recipe = hashMapOf(
                "content" to recipeContent,
                "imageUrl" to "",
                "userId" to userId,
                "username" to username,
                "timestamp" to System.currentTimeMillis()
            )
            firestore.collection("recipes")
                .add(recipe)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e ->
                    showToast(context, "Failed to upload recipe: ${e.message}")
                }
        }
        .addOnFailureListener { e ->
            showToast(context, "Failed to fetch user: ${e.message}")
        }

}

fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}
