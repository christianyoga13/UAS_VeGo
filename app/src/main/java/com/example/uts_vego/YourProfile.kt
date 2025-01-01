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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class YourProfile : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            YourProfileScreen(navController = navController)
        }
    }
}

@Composable
fun YourProfileScreen(navController: NavController) {
    val placeholderImagePainter = painterResource(id = R.drawable.profile_placeholder)
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    val user = auth.currentUser

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var profileImageUrl by remember { mutableStateOf("") }
    var editedName by remember { mutableStateOf("") }
    var editedEmail by remember { mutableStateOf("") }
    var editedPhoneNumber by remember { mutableStateOf("") }
    var editedBirthDate by remember { mutableStateOf("") }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it
        }
    }

    LaunchedEffect(user) {
        user?.uid?.let { uid ->
            firestore.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        profileImageUrl = document.getString("profileImage") ?: ""
                        name = document.getString("name") ?: "N/A"
                        email = document.getString("email") ?: "N/A"
                        phoneNumber = document.getString("phoneNumber") ?: "N/A"
                        birthDate = document.getString("birthDate") ?: "N/A"
                        editedName = name
                        editedEmail = email
                        editedPhoneNumber = phoneNumber
                        editedBirthDate = birthDate
                    } else {
                        Toast.makeText(context, "No profile found.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(context, "Error loading profile: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } ?: Toast.makeText(context, "User not logged in.", Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .background(Color(0xFFFFA500))
                    .statusBarsPadding()
            ) {
                TopAppBar(
                    title = {
                        Text(
                            "My Profile",
                            color = Color(0xFFFFA500),
                            fontWeight = FontWeight.Bold
                        )
                    },
                    backgroundColor = Color.White,
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color(0xFFFFA500)
                            )
                        }
                    },
                    actions = {
                        if (!isEditing) {
                            IconButton(onClick = { isEditing = true }) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit Profile",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
            ) {
                Image(
                    painter = if (imageUri != null) rememberAsyncImagePainter(imageUri)
                    else if (profileImageUrl.isNotEmpty()) rememberAsyncImagePainter(profileImageUrl)
                    else placeholderImagePainter,
                    contentDescription = "Profile Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isEditing) {
                Button(
                    onClick = { imagePicker.launch("image/*") },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFFA500)),
                    modifier = Modifier.width(200.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Camera,
                        contentDescription = "Change Photo",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Change Photo", color = Color.White)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            ProfileDetailItem(
                label = "Name",
                value = if (isEditing) editedName else name,
                isEditing = isEditing,
                onValueChange = { editedName = it }
            )

            ProfileDetailItem(
                label = "Email",
                value = if (isEditing) editedEmail else email,
                isEditing = isEditing,
                onValueChange = { editedEmail = it }
            )

            ProfileDetailItem(
                label = "Phone Number",
                value = if (isEditing) editedPhoneNumber else phoneNumber,
                isEditing = isEditing,
                onValueChange = { editedPhoneNumber = it }
            )

            ProfileDetailItem(
                label = "Birth Date",
                value = if (isEditing) editedBirthDate else birthDate,
                isEditing = isEditing,
                onValueChange = { editedBirthDate = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (isEditing) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            val userData = mutableMapOf<String, Any>(
                                "name" to editedName,
                                "email" to editedEmail,
                                "phoneNumber" to editedPhoneNumber,
                                "birthDate" to editedBirthDate
                            )

                            user?.uid?.let { uid ->
                                if (imageUri != null) {
                                    // Jika ada gambar baru, upload gambar dulu
                                    val imageRef = storage.reference.child("profile_images/${uid}/${UUID.randomUUID()}")
                                    imageRef.putFile(imageUri!!)
                                        .addOnSuccessListener { taskSnapshot ->
                                            taskSnapshot.storage.downloadUrl.addOnSuccessListener { downloadUrl ->
                                                userData["profileImage"] = downloadUrl.toString() // Tambahkan URL gambar ke data
                                                saveOrUpdateFirestore(userData, uid, context, navController)
                                            }
                                        }
                                        .addOnFailureListener { exception ->
                                            Toast.makeText(context, "Error uploading image: ${exception.message}", Toast.LENGTH_SHORT).show()
                                        }
                                } else {
                                    // Jika tidak ada gambar baru, langsung simpan atau update Firestore
                                    saveOrUpdateFirestore(userData, uid, context, navController)
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFFA500)),
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            editedName = name
                            editedEmail = email
                            editedPhoneNumber = phoneNumber
                            editedBirthDate = birthDate
                            imageUri = null // Reset image URI
                            isEditing = false
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray),
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    ) {
                        Text("Cancel", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

fun saveOrUpdateFirestore(
    userData: Map<String, Any>,
    uid: String,
    context: Context,
    navController: NavController
) {
    val firestore = FirebaseFirestore.getInstance()

    firestore.collection("users").document(uid)
        .get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                // Jika dokumen sudah ada, update data
                firestore.collection("users").document(uid)
                    .update(userData)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Profile updated successfully.", Toast.LENGTH_SHORT).show()
                        navController.navigateUp() // Kembali ke halaman sebelumnya
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(context, "Error updating profile: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                // Jika dokumen belum ada, buat dokumen baru
                firestore.collection("users").document(uid)
                    .set(userData)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Profile created successfully.", Toast.LENGTH_SHORT).show()
                        navController.navigateUp() // Kembali ke halaman sebelumnya
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(context, "Error creating profile: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
        .addOnFailureListener { exception ->
            Toast.makeText(context, "Error checking profile: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
}

@Composable
fun ProfileDetailItem(
    label: String,
    value: String,
    isEditing: Boolean,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, fontSize = 14.sp, color = Color(0xFFFFA500))
        if (isEditing) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
        } else {
            Text(
                text = value,
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}
