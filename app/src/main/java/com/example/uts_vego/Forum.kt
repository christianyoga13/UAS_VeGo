package com.example.uts_vego

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Data class untuk Post
data class Post(
    val content: String = "",
    val imageUrl: String = "",
    val username: String = "",
    val timestamp: Timestamp = Timestamp.now()
)

@Composable
fun ForumScreen(navController: NavHostController) {
    val firestore = FirebaseFirestore.getInstance()
    val posts = remember { mutableStateListOf<Post>() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        firestore.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(context, "Gagal memuat posting: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                posts.clear()
                snapshot?.forEach { doc ->
                    posts.add(doc.toObject<Post>())
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Forum") },
                actions = {
                    IconButton(onClick = { navController.navigate("post") }) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("post") }) {
                Icon(Icons.Filled.Add, contentDescription = "Add")
            }
        }
    ) { paddingValues ->
        if (posts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No posts yet.")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(paddingValues)) {
                items(posts) { post ->
                    PostItem(post)
                }
            }
        }
    }
}

@Composable
fun PostItem(post: Post) {
    Card(
        elevation = 4.dp,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = post.username, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = post.content)

            if (post.imageUrl.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Image(
                    painter = rememberAsyncImagePainter(post.imageUrl),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(post.timestamp.toDate())
            Text(text = formattedDate, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun PostForumScreen(
    navController: NavHostController,
    firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    storage: FirebaseStorage = FirebaseStorage.getInstance(),
    auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    var postContent by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var username by remember { mutableStateOf("") }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val getImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
    }

    LaunchedEffect(auth.currentUser) {
        auth.currentUser?.uid?.let { userId ->
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        username = document.getString("name") ?: "Anonymous"
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(context, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Buat Postingan", style = MaterialTheme.typography.h5)

        OutlinedTextField(
            value = postContent,
            onValueChange = { postContent = it },
            label = { Text("Tulis sesuatu...") },
            modifier = Modifier.fillMaxWidth(),
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

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = { getImageLauncher.launch("image/*") }) {
                Text("Pilih Gambar")
            }

            Button(onClick = {
                if (postContent.isNotBlank()) {
                    if (imageUri != null) {
                        uploadPostWithImage(postContent, imageUri!!, username, firestore, storage, context, auth) {
                            postContent = ""
                            imageUri = null
                            navController.navigate("forum") {
                                popUpTo("forum") { inclusive = true } // Menghapus screens sebelumnya dari backstack.
                            }
                        }
                    } else {
                        uploadPostWithoutImage(postContent, username, firestore, context, auth) {
                            postContent = ""
                            imageUri = null
                            navController.navigate("forum") {
                                popUpTo("forum") { inclusive = true } // Menghapus screens sebelumnya dari backstack.

                            }
                        }
                    }
                } else {
                    Toast.makeText(context, "Konten tidak boleh kosong", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text("Posting")
            }
        }
    }
}

private fun uploadPostWithImage(
    postContent: String,
    imageUri: Uri,
    username: String,
    firestore: FirebaseFirestore,
    storage: FirebaseStorage,
    context: Context,
    auth: FirebaseAuth, // Tambahkan FirebaseAuth di sini
    onSuccess: () -> Unit
) {
    val imageRef = storage.reference.child("images/${System.currentTimeMillis()}.jpg")
    imageRef.putFile(imageUri)
        .addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                val userId = auth.currentUser?.uid ?: "" // Ambil userId. Berikan nilai default jika null.

                val post = hashMapOf(
                    "content" to postContent,
                    "imageUrl" to downloadUri.toString(),
                    "username" to username,
                    "userId" to userId, // Tambahkan userId ke data postingan
                    "timestamp" to Timestamp.now()
                )
                firestore.collection("posts")
                    .add(post)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Postingan berhasil diupload!", Toast.LENGTH_SHORT).show()
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Gagal mengupload postingan: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }.addOnFailureListener { e ->
                Toast.makeText(context, "Gagal mengambil URL download gambar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Gagal mengupload gambar: ${e.message}", Toast.LENGTH_SHORT).show()
        }
}

private fun uploadPostWithoutImage(
    postContent: String,
    username: String,
    firestore: FirebaseFirestore,
    context: Context,
    auth: FirebaseAuth, // Tambahkan FirebaseAuth di sini
    onSuccess: () -> Unit
) {
    val userId = auth.currentUser?.uid ?: ""  // Ambil userId. Berikan nilai default jika null.

    val post = hashMapOf(
        "content" to postContent,
        "imageUrl" to "",
        "username" to username,
        "userId" to userId, // Tambahkan userId ke data postingan
        "timestamp" to Timestamp.now()
    )
    firestore.collection("posts")
        .add(post)
        .addOnSuccessListener {
            Toast.makeText(context, "Postingan berhasil diupload!", Toast.LENGTH_SHORT).show()
            onSuccess()
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Gagal mengupload postingan: ${e.message}", Toast.LENGTH_SHORT).show()
        }
}