// Promo.kt
package com.example.uts_vego

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlin.math.abs
import kotlin.random.Random

@Composable
fun PromoScreen(navController: NavController) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var promoCode by remember { mutableStateOf("") }

    // Initialize ShakeDetector
    val shakeDetector = remember {
        ShakeDetector(
            onShake = {
                promoCode = generatePromoCode()
                showDialog = true
            }
        )
    }

    // Obtain SensorManager from the context
    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    // Register and unregister the shake detector
    DisposableEffect(sensorManager) {
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(shakeDetector, accelerometer, SensorManager.SENSOR_DELAY_UI)

        onDispose {
            sensorManager.unregisterListener(shakeDetector)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Promo", color = Color.White, fontWeight = FontWeight.Bold) },
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
            // Voucher Section
            Text("Voucher", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(3) { // Replace '3' with the actual number of vouchers you have
                    PromoVoucherCard()
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Big Discount Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Big Discount", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                TextButton(onClick = { /* Handle See More click */ }) {
                    Text("See More", color = Color(0xFF4CAF50))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(listOf("Dharma Kitchen", "Fedwell")) { item -> // Replace with actual list data
                    BigDiscountItem(name = item)
                }
            }
        }

        // Promo Code Dialog
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(text = "Your Promo Code") },
                text = { Text(text = "Here is your exclusive promo code: $promoCode") },
                confirmButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
fun PromoVoucherCard() {
    Card(
        modifier = Modifier
            .width(200.dp)
            .height(100.dp),
        backgroundColor = Color(0xFF4CAF50),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Text(
                "Promo Cashback Tomorrow...",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "• Tomorrow Coffee - Cashback 45% Min. 50000",
                color = Color.White,
                fontSize = 10.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Rp5.000", color = Color.White, fontSize = 12.sp)
                Button(
                    onClick = { /* Handle Claim button click */ },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.White)
                ) {
                    Text("Claim", color = Color(0xFF4CAF50), fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun BigDiscountItem(name: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(8.dp)
    ) {
        // Placeholder for the restaurant image
        Image(
            painter = painterResource(id = R.drawable.ic_restaurant), // Replace with actual image resource
            contentDescription = "Restaurant Image",
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Chip(text = "Flexitarian")
                Chip(text = "Cheap")
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("⭐ 4.7k", fontSize = 12.sp)
                Text("40 MINS", fontSize = 12.sp)
                Text("3.5 km", fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun Chip(text: String) {
    Surface(
        color = Color(0xFFE0E0E0),
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.padding(2.dp)
    ) {
        Text(
            text = text,
            color = Color.Black,
            fontSize = 10.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

private fun generatePromoCode(): String {
    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    return (1..8)
        .map { chars[Random.nextInt(chars.length)] }
        .joinToString("")
}

/**
 * ShakeDetector class to detect shake gestures using the accelerometer sensor.
 */
class ShakeDetector(
    private val onShake: () -> Unit,
    private val shakeThreshold: Float = 15.0f, // Adjust this threshold as needed
    private val shakeInterval: Long = 5000 // Minimum interval between shakes in milliseconds
) : SensorEventListener {

    private var lastShakeTime: Long = 0
    private var lastAccelX = 0f
    private var lastAccelY = 0f
    private var lastAccelZ = 0f

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return

        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // Calculate the change in acceleration since the last reading
            val deltaX = abs(x - lastAccelX)
            val deltaY = abs(y - lastAccelY)
            val deltaZ = abs(z - lastAccelZ)

            val gForce = deltaX + deltaY + deltaZ // Sum of deltas

            // Debugging log (optional)
            // Log.d("ShakeDetector", "gForce: $gForce")

            if (gForce > shakeThreshold) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastShakeTime > shakeInterval) {
                    lastShakeTime = currentTime
                    onShake()
                }
            }

            // Save the current accelerometer readings for the next event
            lastAccelX = x
            lastAccelY = y
            lastAccelZ = z
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used in this implementation
    }
}
