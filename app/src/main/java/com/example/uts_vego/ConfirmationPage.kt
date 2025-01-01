package com.example.uts_vego

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun ConfirmationPage(
    navController: NavController,
    cartViewModel: CartViewModel,
    paymentViewModel: PaymentViewModel,
    promoViewModel: PromoViewModel,
    totalPrice: Double,
    deliveryName: String,
    deliveryPrice: Int,
    total: Double
) {
    val cartItems by cartViewModel.cartItemsState.collectAsState()
    val balance by paymentViewModel.balance.collectAsState()
    val vouchers by remember { derivedStateOf { promoViewModel.userVouchers.toList() } }

    var selectedVoucher by remember { mutableStateOf<Voucher?>(null) }
    var discountAmount by remember { mutableStateOf(0.0) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    val tax = (total + deliveryPrice) * 0.11
    val grandTotal = total + deliveryPrice + tax
    val discountedTotal = grandTotal - discountAmount

    // Fetch vouchers only once when composable is first composed
    LaunchedEffect(Unit) {
        try {
            promoViewModel.fetchUserVouchers()
            Log.d("ConfirmationPage", "Fetching vouchers...")
        } catch (e: Exception) {
            Log.e("ConfirmationPage", "Error fetching vouchers", e)
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .background(Color(0xFFFFA500))
                    .statusBarsPadding()
            ) {
                TopAppBar(
                    title = { Text("Order Confirmation") },
                    backgroundColor = Color(0xFFFFA500),
                    contentColor = Color.White,
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
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
                .verticalScroll(rememberScrollState())
        ) {
            // Order Details Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                elevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Order Details",
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    cartItems.forEach { item ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(item.name, fontWeight = FontWeight.Bold)
                                Text("Rp ${item.price}")
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Quantity: ${item.quantity}")
                                Text("Subtotal: Rp ${item.price * item.quantity}")
                            }
                        }
                    }
                }
            }

            // Voucher Section
            Text(
                "Apply Voucher",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.h6
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box {
                Button(
                    onClick = { isDropdownExpanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFF6200EE)
                    )
                ) {
                    Text(
                        text = selectedVoucher?.code ?: "Select Voucher",
                        color = Color.White
                    )
                }
                DropdownMenu(
                    expanded = isDropdownExpanded,
                    onDismissRequest = { isDropdownExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    if (vouchers.isEmpty()) {
                        DropdownMenuItem(onClick = {}) {
                            Text("No Vouchers Available")
                        }
                    } else {
                        vouchers.forEach { voucher ->
                            DropdownMenuItem(
                                onClick = {
                                    selectedVoucher = voucher
                                    discountAmount = calculateDiscount(grandTotal, voucher)
                                    isDropdownExpanded = false
                                }
                            ) {
                                Column {
                                    Text(
                                        "${voucher.code}",
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "Discount ${voucher.discountPercentage}% OFF",
                                        style = MaterialTheme.typography.body2
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Price Details Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                elevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Price Details",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.h6
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal")
                        Text("Rp $total")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(deliveryName)
                        Text("Rp $deliveryPrice")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tax (11%)")
                        Text("Rp ${"%.2f".format(tax)}")
                    }

                    if (selectedVoucher != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Discount (${selectedVoucher?.code})")
                            Text("- Rp ${"%.2f".format(discountAmount)}")
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Total After Discount",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Rp ${"%.2f".format(discountedTotal)}",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Balance and Confirm Button
            Text(
                text = "Your Balance: Rp ${"%.2f".format(balance)}",
                style = MaterialTheme.typography.subtitle1,
                color = if (balance >= discountedTotal) Color.Black else Color.Red,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Button(
                onClick = {
                    if (balance >= discountedTotal) {
                        paymentViewModel.updateBalance(
                            newBalance = balance - discountedTotal,
                            onSuccess = {
                                // Delete the used voucher if one was selected
                                selectedVoucher?.let { voucher ->
                                    promoViewModel.deleteVoucher(voucher)
                                }

                                paymentViewModel.addOrderTransaction(
                                    amount = discountedTotal,
                                    date = "Today",
                                    onSuccess = {
                                        cartViewModel.clearCart()
                                        navController.navigate("home") {
                                            popUpTo("home") { inclusive = true }
                                        }
                                    },
                                    onError = { error ->
                                        Log.e("ConfirmationPage", "Error adding transaction: $error")
                                    }
                                )
                            },
                            onError = { error ->
                                Log.e("ConfirmationPage", "Error updating balance: $error")
                            }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (balance >= discountedTotal)
                        Color(0xFF00C853) else Color.Gray,
                    contentColor = Color.White
                ),
                enabled = balance >= discountedTotal
            ) {
                Text("Confirm Order")
            }
        }
    }
}

// Helper function to calculate discount
private fun calculateDiscount(grandTotal: Double, voucher: Voucher): Double {
    return if (voucher.discountPercentage > 0) {
        grandTotal * (voucher.discountPercentage / 100.0)
    } else {
        0.0
    }
}
