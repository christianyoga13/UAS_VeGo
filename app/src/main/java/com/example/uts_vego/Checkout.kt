package com.example.uts_vego

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun CheckoutPage(
    navController: NavController,
    cartViewModel: CartViewModel,
    restaurantId: String? = null
) {
    val cartItems by cartViewModel.cartItemsState.collectAsState()

    val displayedItems = if (restaurantId != null) {
        cartItems.filter { it.restaurantId == restaurantId }
    } else {
        cartItems
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
                        Text(if (restaurantId != null) "Checkout - $restaurantId" else "Checkout", color = Color(0xFFFFA500))
                    },
                    backgroundColor = Color.White,
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFFFFA500))
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (displayedItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Cart is empty.")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(displayedItems) { item ->
                        CheckoutItemRow(
                            item = item,
                            onIncreaseQty = {
                                cartViewModel.updateItemQuantityInCart(item, item.quantity + 1)
                            },
                            onDecreaseQty = {
                                cartViewModel.updateItemQuantityInCart(item, item.quantity - 1)
                            },
                            onRemove = {
                                cartViewModel.removeFromCart(item)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val total = displayedItems.sumOf { it.price * it.quantity }

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Total: Rp $total",
                        style = MaterialTheme.typography.h6
                    )
                    Button(
                        onClick = {
                            if (restaurantId != null) {
                                navController.navigate("map/$restaurantId/$total")
                                Log.d("Navigation", "Navigating to map/$restaurantId")
                                Log.d("CheckoutPage", "Restaurant ID: $restaurantId")

                            } else {
                                navController.navigateUp()
                            }
                        }
                    ) {
                        Text("Confirm Purchase")
                    }
                }
            }
        }
    }
}

@Composable
fun CheckoutItemRow(
    item: CartItem,
    onIncreaseQty: () -> Unit,
    onDecreaseQty: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        elevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    item.name,
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Rp ${item.price} x ${item.quantity} = Rp ${item.price * item.quantity}",
                    style = MaterialTheme.typography.body2
                )
                Text(
                    "Restoran: ${item.restaurantId}", // Tambahkan informasi restoran
                    style = MaterialTheme.typography.caption,
                    color = Color.Gray
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onDecreaseQty() }) {
                    Text("-")
                }
                Text(item.quantity.toString())
                IconButton(onClick = { onIncreaseQty() }) {
                    Text("+")
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { onRemove() }) {
                    Text("🗑")
                }
            }
        }
    }
}

