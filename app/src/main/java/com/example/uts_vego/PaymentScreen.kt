package com.example.uts_vego

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun PaymentScreen(
    viewModel: PaymentViewModel = viewModel()
) {
    val context = LocalContext.current
    val balance by viewModel.balance.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .background(Color(0xFFFFA500)) // Sama dengan warna top bar
                    .statusBarsPadding() // Pastikan padding ditambahkan di sini
            ) {
                TopAppBar(
                    title = { Text("Payment", color = Color.White) },
                    backgroundColor = Color(0xFFFFA500), // Warna yang sama agar menyatu
                    elevation = 0.dp
                )
            }
        }
    ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                BalanceCard(
                    balance = balance,
                    onTopUp = {
                        viewModel.topUp(
                            onSuccess = {
                                Toast.makeText(context, "Top up successful", Toast.LENGTH_SHORT).show()
                            },
                            onError = { error ->
                                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "History Pembayaran:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn {
                    items(transactions) { transaction ->
                        TransactionItem(transaction = transaction)
                    }
                }
            }
    }
}

@Composable
fun BalanceCard(balance: Double, onTopUp: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        backgroundColor = Color(0xFF4CAF50),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("VEGAN Cash", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Total Saldo", color = Color.White, fontSize = 14.sp)
            Text("Rp. ${"%,.2f".format(balance)}", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Basic", color = Color.White, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier.fillMaxWidth()
            ) {
                ActionButton("Top Up", Icons.Default.Add, onTopUp)
                ActionButton("Transfer", Icons.Default.ArrowForward) { /* Handle transfer */ }
                ActionButton("ATM", Icons.Default.ShoppingCart) { /* Handle ATM */ }
            }
        }
    }
}

fun isToday(timestamp: Long): Boolean {
    val today = java.util.Calendar.getInstance()
    val date = java.util.Calendar.getInstance().apply { timeInMillis = timestamp }

    return today.get(java.util.Calendar.YEAR) == date.get(java.util.Calendar.YEAR) &&
            today.get(java.util.Calendar.DAY_OF_YEAR) == date.get(java.util.Calendar.DAY_OF_YEAR)
}


@Composable
fun TransactionItem(transaction: Transaction) {
    val formattedDate = if (isToday(transaction.dateCreated)) {
        "Today"
    } else {
        java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault())
            .format(java.util.Date(transaction.dateCreated))
    }

    val isTopUp = transaction.title == "Top Up" // Tentukan apakah ini top-up atau order
    val amountColor = if (isTopUp) Color(0xFF4CAF50) else Color.Red // Hijau untuk top-up, merah untuk order
    val formattedAmount = if (isTopUp) {
        "+Rp. ${"%,.2f".format(transaction.amount)}"
    } else {
        "-Rp. ${"%,.2f".format(transaction.amount)}"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.ShoppingCart,
            contentDescription = "Transaction",
            tint = Color(0xFFFFA500)
        )
        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = transaction.location,
                fontSize = 14.sp,
                color = Color.Gray
            )
            Text(
                text = formattedDate,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        Text(
            text = formattedAmount,
            fontSize = 14.sp,
            color = amountColor, // Warna ditentukan berdasarkan tipe transaksi
            fontWeight = FontWeight.Bold
        )
    }
    Divider(color = Color.Gray, thickness = 0.5.dp)
}



@Composable
fun ActionButton(label: String, icon: ImageVector, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}