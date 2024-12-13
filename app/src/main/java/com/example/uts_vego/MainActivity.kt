package com.example.uts_vego

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            checkAdminRole()
            setContent {
                MainAppNavigation()
            }
        }
    }

    private fun checkAdminRole() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.getIdToken(true)?.addOnSuccessListener { result ->
            val claims = result.claims
            val isAdmin = claims["admin"] as? Boolean ?: false

            if (isAdmin) {
                // Jika pengguna admin, tampilkan tampilan admin
                setContent {
                    MainAppNavigation()
                }
            } else {
                // Jika bukan admin, arahkan ke tampilan normal
                setContent {
                    MainAppNavigation()
                }
            }
        }
    }
}

@Composable
fun MainAppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Buat instance RestoViewModel
    val restoViewModel: RestoViewModel = remember { RestoViewModel() }
    restoViewModel.fetchRestosFromFirestore()
    val allItems = restoViewModel.restoList + getRestoItems() + getFastServeItems() + getBigDiscountItems()

    // Buat instance CartViewModel
    val cartViewModel: CartViewModel = remember { CartViewModel() }

    Scaffold(
        bottomBar = {
            // Hanya tampilkan BottomNavigationBar pada rute tertentu
            if (currentRoute in listOf("home", "payment", "promo", "profile_home")) {
                BottomNavigationBar(navController)
            }
        }
    ) { paddingValues ->
        // Navigasi Utama
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            // Halaman Utama
            composable("home") {
                HomeScreenContent(navController)
            }
            composable("payment") {
                PaymentScreen()
            }
            composable("promo") {
                PromoScreen(navController)
            }

            // Halaman Profil
            composable("profile_home") {
                ProfileScreen(navController)
            }
            composable("yourProfile") {
                YourProfileScreen(navController)
            }

            // Online Order Screen
            composable("onlineOrder") {
                OnlineOrderScreen(navController, viewModel = restoViewModel)
            }

            // Detail Restoran
            composable(
                route = "restoDetail/{name}",
                arguments = listOf(navArgument("name") { type = NavType.StringType })
            ) { backStackEntry ->
                val restoName = backStackEntry.arguments?.getString("name") ?: ""
                val restoItem = allItems.find { it.name == restoName }

                if (restoItem != null) {
                    // Pass cartViewModel di sini jika RestoDetailScreen butuh cartViewModel.
                    // Jika tidak butuh, bisa dihapus. Namun sebaiknya sama seperti sebelumnya.
                    RestoDetailScreen(navController = navController, restoItem = restoItem, cartViewModel = cartViewModel)
                } else {
                    Text("Resto not found")
                }
            }

            // Admin Screen
            composable("AdminScreen") {
                AdminScreen(navController = navController, viewModel = restoViewModel)
            }
            composable("forum") { ForumScreen(navController) }
            composable("post") { PostForumScreen(navController) }

            composable("checkout_page") {
                CheckoutPage(cartViewModel = cartViewModel)
            }
        }
    }
}



