package com.example.uts_vego

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth

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
            if(currentRoute in listOf("profile_home")) {
                BottomNavigationBar(navController)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "profile_home",
            modifier = Modifier.padding(paddingValues)
        ) {
            // Define your main profile routes
            composable("profile_home") { ProfileScreen(navController) }
            composable("yourProfile") { YourProfileScreen(navController) } // No bottom nav here
//        composable("address") { AddressScreen(navController) }
//        composable("paymentMethod") { PaymentMethodScreen(navController) }
//        composable("order") { OrderScreen(navController) }
//        composable("notification") { NotificationScreen(navController) }
//        composable("setting") { SettingScreen(navController) }
//        composable("helpCenter") { HelpCenterScreen(navController) }
        }
    }
}

@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Profile", color = Color.White, fontWeight = FontWeight.Bold) },
                backgroundColor = Color(0xFFFFA500)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.Gray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "P",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "Christian Yoga",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Rp 100,000",
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(onClick = {
                        navController.navigate("yourProfile")
                    }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit Profile")
                    }
                }
            }

            // Menu Section
            Column(modifier = Modifier.fillMaxWidth()) {
                ProfileMenuItem("Your Profile") {
                    navController.navigate("yourProfile")
                }
                ProfileMenuItem("Payment Method") {
                    navController.navigate("payment")
                }
                ProfileMenuItem("Order") {
                    navController.navigate("checkout_page")
                }
                ProfileMenuItem("Notification") {
                    navController.navigate("notification")
                }
                ProfileMenuItem("Setting") {
                    navController.navigate("setting")
                }
                ProfileMenuItem("Help Center") {
                    navController.navigate("helpCenter")
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(context, LoginActivity::class.java)
                    context.startActivity(intent)
                    (context as? MainActivity)?.finish()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
            ) {
                Text("Log out", color = Color.White)
            }
        }
    }
}

@Composable
fun ProfileMenuItem(text: String, onClick: () -> Unit) {
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
            Text(
                text = text,
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFFFFA500),
                modifier = Modifier.weight(1f)
            )
        }
        Divider(modifier = Modifier.padding(top = 12.dp), color = Color(0xFFFFA500))
    }
}
