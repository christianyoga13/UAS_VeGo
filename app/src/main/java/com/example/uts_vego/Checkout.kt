package com.example.uts_vego

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CheckoutPage(cartViewModel: CartViewModel) {
    val cartItems by cartViewModel.cartItemsState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout") },
                backgroundColor = MaterialTheme.colors.primary
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (cartItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Cart is empty.")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(cartItems) { item ->
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

                val total = cartItems.sumOf { it.price * it.quantity }

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Total: Rp $total", style = MaterialTheme.typography.h6)
                    Button(onClick = {
                        cartViewModel.clearCart()
                    }) {
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
                Text(item.name, style = MaterialTheme.typography.subtitle1, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                Text("Rp ${item.price} x ${item.quantity} = Rp ${item.price * item.quantity}", style = MaterialTheme.typography.body2)
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
