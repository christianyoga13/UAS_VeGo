    package com.example.uts_vego

    import android.content.Intent
    import android.os.Bundle
    import androidx.activity.compose.setContent
    import androidx.appcompat.app.AppCompatActivity
    import androidx.compose.foundation.background
    import androidx.compose.foundation.layout.Box
    import androidx.compose.foundation.layout.fillMaxSize
    import androidx.compose.foundation.layout.navigationBarsPadding
    import androidx.compose.foundation.layout.padding
    import androidx.compose.foundation.layout.systemBarsPadding
    import androidx.compose.foundation.shape.RoundedCornerShape
    import androidx.compose.material.*
    import androidx.compose.runtime.Composable
    import androidx.compose.runtime.collectAsState
    import androidx.compose.runtime.getValue
    import androidx.compose.runtime.remember
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.draw.clip
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.platform.LocalContext
    import androidx.compose.ui.unit.dp
    import androidx.core.content.ContextCompat
    import androidx.lifecycle.viewmodel.compose.viewModel
    import androidx.navigation.NavType
    import androidx.navigation.compose.*
    import androidx.navigation.navArgument
    import com.google.firebase.auth.FirebaseAuth
    import com.google.firebase.firestore.FirebaseFirestore
    import com.google.firebase.storage.FirebaseStorage

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
                window.navigationBarColor = ContextCompat.getColor(this, R.color.orange)
                setContent {
                    MaterialTheme {
                        MainAppNavigation()
                    }
                }
            }
        }

        private fun checkAdminRole() {
            val user = FirebaseAuth.getInstance().currentUser
            user?.getIdToken(true)?.addOnSuccessListener { result ->
                val claims = result.claims
                val isAdmin = claims["admin"] as? Boolean ?: false

                if (isAdmin) {
                    setContent {
                        MainAppNavigation()
                    }
                } else {
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

        val restoViewModel: RestoViewModel = remember { RestoViewModel() }
        restoViewModel.fetchRestosFromFirestore()
        val allItems = restoViewModel.restoList + getRestoItems() + getFastServeItems() + getBigDiscountItems() + getBestSellerItems() + getBestPickRestaurants()

        val cartViewModel: CartViewModel = remember { CartViewModel() }
        val paymentViewModel: PaymentViewModel = remember { PaymentViewModel() }
        val context = LocalContext.current

        Scaffold(
            backgroundColor = Color.White
        ) { innerPadding ->
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)) {
                NavHost(
                    navController = navController,
                    startDestination = "home",
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable("home") {
                        HomeScreenContent(navController)
                    }
                    composable("payment") {
                        PaymentScreen()
                    }
                    composable("promo") {
                        PromoScreen(navController)
                    }
                    composable("profile_home") {
                        ProfileScreen(navController)
                    }
                    composable("yourProfile") {
                        YourProfileScreen(navController)
                    }
                    composable("helpCenter") {
                        HelpCenterScreen(navController)
                    }
                    composable("payment_method") {
                        PaymentMethodScreen(navController)
                    }
                    composable("notifications") {
                        NotificationScreen(navController)
                    }
                    composable("onlineOrder") {
                        OnlineOrderScreen(navController, viewModel = restoViewModel)
                    }
                    composable("address") {
                        AddressScreen(navController)
                    }
                    composable("nearby"){
                        RestaurantNearByScreen(navController, restoViewModel)
                    }
                    composable(
                        route = "restoDetail/{name}",
                        arguments = listOf(navArgument("name") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val restoName = backStackEntry.arguments?.getString("name") ?: ""
                        val restoItem = allItems.find { it.name == restoName }
                        if (restoItem != null) {
                            RestoDetailScreen(
                                navController = navController,
                                restoItem = restoItem,
                                cartViewModel = cartViewModel
                            )
                        } else {
                            Text("Resto not found")
                        }
                    }
                    composable("AdminScreen") {
                        AdminScreen(navController = navController, viewModel = restoViewModel)
                    }
                    composable("forum") {
                        ForumScreen(navController)
                    }
                    composable("post") {
                        PostForumScreen(navController)
                    }
                    composable(
                        route = "recipeDetail/{recipeId}",
                        arguments = listOf(navArgument("recipeId") { type = NavType.LongType }) // Sesuaikan dengan tipe ID
                    ) { backStackEntry ->
                        val recipeId = backStackEntry.arguments?.getLong("recipeId") ?: 0L
                        RecipeDetailScreen(recipeId = recipeId)
                    }
                    composable(
                        route = "checkout_page/{restaurantId}",
                        arguments = listOf(
                            navArgument("restaurantId") {
                                type = NavType.StringType
                                nullable = true
                            }
                        )
                    ) { backStackEntry ->
                        val restaurantId = backStackEntry.arguments?.getString("restaurantId")
                        CheckoutPage(
                            navController = navController,
                            cartViewModel = cartViewModel,
                            restaurantId = restaurantId
                        )
                    }

                    composable("checkout_page") {
                        CheckoutPage(
                            navController = navController,
                            cartViewModel = cartViewModel
                        )
                    }

                    composable("recipe") {
                        RecipeListScreen(onAddRecipeClick = {
                            navController.navigate("addRecipe")
                        })
                    }
                    composable("addRecipe") {
                        Recipe2Screen(auth = FirebaseAuth.getInstance(), context = context, firestore = FirebaseFirestore.getInstance(), storage = FirebaseStorage.getInstance())
                    }

                    composable(
                        route = "map/{restaurantId}/{total}",
                        arguments = listOf(
                            navArgument("restaurantId") { type = NavType.StringType },
                            navArgument("total") { type = NavType.FloatType })
                    ) { backStackEntry ->
                        val restaurantId = backStackEntry.arguments?.getString("restaurantId")
                        val restoItems = getRestoItems() + getFastServeItems() + getBigDiscountItems() + getBestSellerItems() + restoViewModel.restoList
                        val total = backStackEntry.arguments?.getFloat("total")?.toDouble() ?: 0.0

                        MapPage(
                            navController = navController,
                            restaurantId = restaurantId,
                            total = total,
                            restoItems = restoItems,
                            cartViewModel = cartViewModel,
                            paymentViewModel = paymentViewModel
                        )
                    }
                    composable(
                        "confirmation/{totalPrice}/{deliveryName}/{deliveryPrice}/{total}",
                        arguments = listOf(
                            navArgument("totalPrice") { type = NavType.FloatType },
                            navArgument("deliveryName") { type = NavType.StringType },
                            navArgument("deliveryPrice") { type = NavType.IntType },
                            navArgument("total") { type = NavType.FloatType }
                        )
                    ) { backStackEntry ->
                        val totalPrice = backStackEntry.arguments?.getFloat("totalPrice")?.toDouble() ?: 0.0
                        val deliveryName = backStackEntry.arguments?.getString("deliveryName") ?: ""
                        val deliveryPrice = backStackEntry.arguments?.getInt("deliveryPrice") ?: 0
                        val total = backStackEntry.arguments?.getFloat("total")?.toDouble() ?: 0.0

                        ConfirmationPage(
                            navController = navController,
                            cartViewModel = cartViewModel,
                            promoViewModel = PromoViewModel(), // Pastikan PromoViewModel disertakan
                            paymentViewModel = paymentViewModel,
                            totalPrice = totalPrice,
                            deliveryName = deliveryName,
                            deliveryPrice = deliveryPrice,
                            total = total
                        )
                    }
                }

                if (currentRoute in listOf("home", "payment", "promo", "profile_home")) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(24.dp)
                    ) {
                        BottomNavigationBar(
                            navController = navController,
                            modifier = Modifier.clip(RoundedCornerShape(24.dp))
                        )
                    }
                }
            }
        }
    }




