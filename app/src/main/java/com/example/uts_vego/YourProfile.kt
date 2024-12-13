package com.example.uts_vego

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class YourProfile : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Assuming you have a NavHost setup elsewhere
            val navController = rememberNavController()
            YourProfileScreen(navController = navController)
        }
    }
}

@Composable
fun YourProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val user = auth.currentUser

    // Profile data state variables
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }

    // Edit mode state
    var isEditing by remember { mutableStateOf(false) }

    // Temporary variables to hold edited data
    var editedName by remember { mutableStateOf(name) }
    var editedEmail by remember { mutableStateOf(email) }
    var editedPhoneNumber by remember { mutableStateOf(phoneNumber) }
    var editedBirthDate by remember { mutableStateOf(birthDate) }

    // Load user data from Firestore
    LaunchedEffect(user) {
        user?.uid?.let { uid ->
            firestore.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        name = document.getString("name") ?: "N/A"
                        email = document.getString("email") ?: "N/A"
                        phoneNumber = document.getString("phoneNumber") ?: "N/A"
                        birthDate = document.getString("birthDate") ?: "N/A"
                        // Initialize edited fields
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
            TopAppBar(
                title = { Text("My Profile", color = Color.White, fontWeight = FontWeight.Bold) },
                backgroundColor = Color(0xFFFFA500),
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    if (!isEditing) {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = Color.White)
                        }
                    }
                }
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Image
            Image(
                painter = painterResource(id = R.drawable.logo), // Replace with actual profile image resource
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFA500))
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Display or Edit Name
            ProfileDetailItem(
                label = "Name",
                value = if (isEditing) editedName else name,
                isEditing = isEditing,
                onValueChange = { editedName = it }
            )

            // Display or Edit Email
            ProfileDetailItem(
                label = "Email",
                value = if (isEditing) editedEmail else email,
                isEditing = isEditing,
                onValueChange = { editedEmail = it }
            )

            // Display or Edit Phone Number
            ProfileDetailItem(
                label = "Phone Number",
                value = if (isEditing) editedPhoneNumber else phoneNumber,
                isEditing = isEditing,
                onValueChange = { editedPhoneNumber = it }
            )

            // Display or Edit Birth Date
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
                    // Save Button
                    Button(
                        onClick = {
                            // Validate inputs
                            if (validateInputs(context, editedName, editedEmail, editedPhoneNumber, editedBirthDate)) {
                                user?.uid?.let { uid ->
                                    val userData = mapOf(
                                        "name" to editedName,
                                        "email" to editedEmail,
                                        "phoneNumber" to editedPhoneNumber,
                                        "birthDate" to editedBirthDate
                                    )
                                    firestore.collection("users").document(uid)
                                        .set(userData)
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "Profile updated successfully.", Toast.LENGTH_SHORT).show()
                                            // Update displayed data
                                            name = editedName
                                            email = editedEmail
                                            phoneNumber = editedPhoneNumber
                                            birthDate = editedBirthDate
                                            isEditing = false
                                        }
                                        .addOnFailureListener { exception ->
                                            Toast.makeText(context, "Error updating profile: ${exception.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFFA500)),
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    ) {
                        Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    // Cancel Button
                    Button(
                        onClick = {
                            // Reset edited fields to original values
                            editedName = name
                            editedEmail = email
                            editedPhoneNumber = phoneNumber
                            editedBirthDate = birthDate
                            isEditing = false
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray),
                        modifier = Modifier.weight(1f).padding(start = 8.dp)
                    ) {
                        Text("Cancel", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
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

fun validateInputs(
    context: Context,
    name: String,
    email: String,
    phoneNumber: String,
    birthDate: String
): Boolean {
    // Basic validation. You can enhance this as needed.
    if (name.isBlank()) {
        Toast.makeText(context, "Name cannot be empty.", Toast.LENGTH_SHORT).show()
        return false
    }
    if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        Toast.makeText(context, "Please enter a valid email.", Toast.LENGTH_SHORT).show()
        return false
    }
    if (phoneNumber.isBlank() || phoneNumber.length < 10) {
        Toast.makeText(context, "Please enter a valid phone number.", Toast.LENGTH_SHORT).show()
        return false
    }
    if (birthDate.isBlank()) {
        Toast.makeText(context, "Birth date cannot be empty.", Toast.LENGTH_SHORT).show()
        return false
    }
    return true
}
