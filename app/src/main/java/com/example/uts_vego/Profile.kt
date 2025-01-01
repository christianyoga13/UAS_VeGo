package com.example.uts_vego

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Profile : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            ProfileNavigation(navController = navController)
        }
    }
}

@Composable
fun ProfileNavigation(navController: NavController) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute == "profile_home") {
                BottomNavigationBar(navController)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "profile_home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("profile_home") { ProfileScreen(navController) }
            composable("yourProfile") { YourProfileScreen(navController) }
            composable("address") { AddressScreen(navController) }
            composable("helpCenter") { HelpCenterScreen(navController) }
        }
    }
}

@Composable
fun ProfileScreen(navController: NavController, viewModel: PaymentViewModel = viewModel()) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val user = auth.currentUser

    var profileName by remember { mutableStateOf("No Name") }
    var profileImageUrl by remember { mutableStateOf("") }
    val balance by viewModel.balance.collectAsState()

    // Fetch user data from Firestore
    LaunchedEffect(user) {
        user?.uid?.let { uid ->
            firestore.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        profileName = document.getString("name") ?: "No Name"
                        profileImageUrl = document.getString("profileImage") ?: ""
                    } else {
                        Toast.makeText(context, "No profile found.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(context, "Error loading profile: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", color = Color.White) },
                backgroundColor = Color(0xFFFFA500),
                elevation = 0.dp
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Profile Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Profile Image
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.Gray),
                        contentAlignment = Alignment.Center
                    ) {
                        if (profileImageUrl.isNotEmpty()) {
                            Image(
                                painter = rememberAsyncImagePainter(profileImageUrl),
                                contentDescription = "Profile Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = profileName.take(1), // First letter of name
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Profile Name and Balance
                    Column {
                        Text(
                            text = profileName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Rp. ${"%,.2f".format(balance)}", // Display the user's balance
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(onClick = { navController.navigate("yourProfile") }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit Profile")
                    }
                }
            }

            // Profile Menu Items
            Column(modifier = Modifier.fillMaxWidth()) {
                ProfileMenuItem(
                    text = "Your Profile",
                    icon = Icons.Filled.Person,
                    onClick = { navController.navigate("yourProfile") }
                )
                ProfileMenuItem(
                    text = "Address",
                    icon = Icons.Filled.LocationOn,
                    onClick = { navController.navigate("address") }
                )
                ProfileMenuItem(
                    text = "Payment Method",
                    icon = Icons.Filled.Payment,
                    onClick = { navController.navigate("payment_method") }
                )
                ProfileMenuItem(
                    text = "Order",
                    icon = Icons.Filled.ShoppingCart,
                    onClick = { navController.navigate("checkout_page") }
                )
                ProfileMenuItem(
                    text = "Notification",
                    icon = Icons.Filled.Notifications,
                    onClick = { navController.navigate("notifications") }
                )
                ProfileMenuItem(
                    text = "Setting",
                    icon = Icons.Filled.Settings,
                    onClick = { navController.navigate("setting") }
                )
                ProfileMenuItem(
                    text = "Help Center",
                    icon = Icons.Filled.Help,
                    onClick = { navController.navigate("helpCenter") }
                )
                ProfileMenuItem(
                    text = "Log Out",
                    icon = Icons.Filled.ExitToApp,
                    textColor = Color.Red,
                    onClick = {
                        FirebaseAuth.getInstance().signOut()
                        val intent = Intent(context, LoginActivity::class.java)
                        context.startActivity(intent)
                        (context as? MainActivity)?.finish()
                    }
                )
            }
        }
    }
}

@Composable
fun ProfileMenuItem(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    textColor: Color = Color(0xFFFFA500)
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = text,
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                color = textColor,
                modifier = Modifier.weight(1f)
            )
        }
        Divider(modifier = Modifier.padding(top = 12.dp), color = textColor)
    }
}
