package com.example.uts_vego

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AddressScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val user = auth.currentUser

    // Address data state variables
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }

    // Edit mode state
    var isEditing by remember { mutableStateOf(false) }

    // Temporary variables to hold edited data
    var editedAddress by remember { mutableStateOf(address) }
    var editedCity by remember { mutableStateOf(city) }
    var editedState by remember { mutableStateOf(state) }
    var editedPostalCode by remember { mutableStateOf(postalCode) }

    // Load address data from Firestore
    LaunchedEffect(user) {
        user?.uid?.let { uid ->
            firestore.collection("addresses").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        address = document.getString("address") ?: ""
                        city = document.getString("city") ?: ""
                        state = document.getString("state") ?: ""
                        postalCode = document.getString("postalCode") ?: ""

                        // Initialize edited fields
                        editedAddress = address
                        editedCity = city
                        editedState = state
                        editedPostalCode = postalCode
                    } else {
                        Toast.makeText(context, "No address found. Please add your address.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(context, "Error loading address: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } ?: Toast.makeText(context, "User not logged in.", Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Address", color = Color(0xFFFFA500)) },
                backgroundColor = Color.White,
                contentColor = Color.White,
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFFFFA500))
                    }
                },
                actions = {
                    if (!isEditing) {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit Address", tint = Color(0xFFFFA500))
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Display or Edit Address
            AddressDetailItem(
                label = "Address",
                value = if (isEditing) editedAddress else address,
                isEditing = isEditing,
                onValueChange = { editedAddress = it }
            )

            // Display or Edit City
            AddressDetailItem(
                label = "City",
                value = if (isEditing) editedCity else city,
                isEditing = isEditing,
                onValueChange = { editedCity = it }
            )

            // Display or Edit State
            AddressDetailItem(
                label = "State",
                value = if (isEditing) editedState else state,
                isEditing = isEditing,
                onValueChange = { editedState = it }
            )

            // Display or Edit Postal Code
            AddressDetailItem(
                label = "Postal Code",
                value = if (isEditing) editedPostalCode else postalCode,
                isEditing = isEditing,
                onValueChange = { editedPostalCode = it }
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
                            user?.uid?.let { uid ->
                                val addressData = mapOf(
                                    "address" to editedAddress,
                                    "city" to editedCity,
                                    "state" to editedState,
                                    "postalCode" to editedPostalCode
                                )
                                firestore.collection("addresses").document(uid)
                                    .set(addressData)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Address updated successfully.", Toast.LENGTH_SHORT).show()
                                        // Update displayed data
                                        address = editedAddress
                                        city = editedCity
                                        state = editedState
                                        postalCode = editedPostalCode
                                        isEditing = false
                                    }
                                    .addOnFailureListener { exception ->
                                        Toast.makeText(context, "Error updating address: ${exception.message}", Toast.LENGTH_SHORT).show()
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
                            editedAddress = address
                            editedCity = city
                            editedState = state
                            editedPostalCode = postalCode
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
fun AddressDetailItem(
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
