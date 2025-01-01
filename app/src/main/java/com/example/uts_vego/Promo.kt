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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import kotlin.math.abs

@Composable
fun PromoScreen(
    navController: NavController,
    restoViewModel: RestoViewModel = viewModel(),
    promoViewModel: PromoViewModel = viewModel()
) {
    val context = LocalContext.current

    // Data from RestoViewModel
    val restoList = restoViewModel.restoList

    // Static data (ensure these functions return List<RestoItem>)
    val fastServeItems = getFastServeItems()
    val bigDiscountItems = getBigDiscountItems()
    val bestSellerItems = getBestSellerItems()
    val additionalRestoItems = getRestoItems()

    // Combine all restaurant data
    val combinedList: List<RestoItem> = remember {
        mutableStateListOf<RestoItem>().apply {
            addAll(restoList)
            addAll(fastServeItems)
            addAll(bigDiscountItems)
            addAll(bestSellerItems)
            addAll(additionalRestoItems)
        }
    }

    // Vouchers from PromoViewModel
    val availableVouchers: SnapshotStateList<Voucher> = promoViewModel.availableVouchers
    val userVouchers: SnapshotStateList<Voucher> = promoViewModel.userVouchers

    // Shake-related state
    var showDialog by remember { mutableStateOf(false) }
    var promoCode by remember { mutableStateOf("") }
    var promoDiscount by remember { mutableStateOf(0) } // New state for discount percentage

    // Select a random big restaurant for promo
    var bigRestaurant by remember { mutableStateOf<RestoItem?>(null) }

    // Shake detector setup
    val shakeDetector = remember {
        ShakeDetector(
            onShake = {
                val newVoucher = promoViewModel.generateAndAddPromoCode()
                promoCode = newVoucher.code
                promoDiscount = newVoucher.discountPercentage
                promoViewModel.addVoucher(newVoucher)
                showDialog = true
            }
        )
    }

    // Sensor Manager setup
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

    // Fetch a random big restaurant when the combined list changes
    LaunchedEffect(combinedList) {
        if (combinedList.isNotEmpty()) {
            bigRestaurant = combinedList.randomOrNull()
        } else {
            restoViewModel.fetchRestosFromFirestore()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Promo") },
                backgroundColor = Color(0xFFFFA500),
                contentColor = Color.White
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Big Restaurant Promo Section
            Text("Big Restaurant Promo", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            if (bigRestaurant != null) {
                BigRestaurantCard(restaurant = bigRestaurant!!)
            } else {
                Text("Loading Big Restaurant Promo...", fontSize = 14.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Available Vouchers Section
            Text("Available Vouchers", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(availableVouchers) { voucher ->
                    PromoVoucherCard(
                        voucher = voucher,
                        onUse = {
                            promoViewModel.useVoucher(voucher)
                        },
                        isClaimed = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Your Vouchers Section
            Text("Your Vouchers", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(userVouchers) { userVoucher ->
                    PromoVoucherCard(
                        voucher = userVoucher,
                        onUse = {}, // No action for already claimed vouchers
                        isClaimed = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // All Restaurants Section
            Text("All Restaurants", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(combinedList) { item ->
                    RestoCard(item)
                }
            }
        }

        // Dialog to show the new promo code
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(text = "Your Promo Code") },
                text = { Text(text = "Here is your exclusive promo code: $promoCode\nDiscount: $promoDiscount% OFF") },
                confirmButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

/**
 * Composable representing a big restaurant promo card.
 *
 * @param restaurant The restaurant to display.
 */
@Composable
fun BigRestaurantCard(restaurant: RestoItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            if (restaurant.imageUrl != null && restaurant.imageUrl.isNotEmpty()) {
                Image(
                    painter = rememberImagePainter(data = restaurant.imageUrl),
                    contentDescription = restaurant.name,
                    modifier = Modifier
                        .size(120.dp)
                        .background(Color.Gray)
                )
            } else {
                Image(
                    painter = rememberImagePainter(data = restaurant.imageRes),
                    contentDescription = restaurant.name,
                    modifier = Modifier
                        .size(120.dp)
                        .background(Color.Gray)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(restaurant.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Rating: ${restaurant.rating}", fontSize = 12.sp, color = Color.Gray)
                Text("Distance: ${restaurant.distance}", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

/**
 * Composable representing a restaurant card.
 *
 * @param restaurant The restaurant to display.
 */
@Composable
fun RestoCard(restaurant: RestoItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            if (restaurant.imageUrl != null && restaurant.imageUrl.isNotEmpty()) {
                Image(
                    painter = rememberImagePainter(data = restaurant.imageUrl),
                    contentDescription = restaurant.name,
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color.Gray)
                )
            } else {
                Image(
                    painter = rememberImagePainter(data = restaurant.imageRes),
                    contentDescription = restaurant.name,
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color.Gray)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(restaurant.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Rating: ${restaurant.rating}", fontSize = 12.sp, color = Color.Gray)
                Text("Distance: ${restaurant.distance}", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

/**
 * Composable representing a promo voucher card.
 *
 * @param voucher The Voucher object to display.
 * @param onUse The action to perform when the "Use" button is clicked.
 * @param isClaimed Indicates if the voucher has already been claimed.
 */
@Composable
fun PromoVoucherCard(voucher: Voucher, onUse: () -> Unit, isClaimed: Boolean = false) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .height(120.dp),
        backgroundColor = if (isClaimed) Color.Gray else Color(0xFF4CAF50),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Text(
                "Promo Code:",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                voucher.code,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                if (voucher.discountPercentage > 0)
                    "Discount: ${voucher.discountPercentage}% OFF"
                else
                    "Free Shipping",
                color = Color.White,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            if (!isClaimed) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Special Discount", color = Color.White, fontSize = 12.sp)
                    Button(
                        onClick = onUse,
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("Use", color = Color(0xFF4CAF50), fontSize = 12.sp)
                    }
                }
            } else {
                // Indicate that the voucher is already claimed
                Text(
                    "Claimed",
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

/**
 * SensorEventListener implementation to detect shake gestures.
 *
 * @param onShake Lambda to execute when a shake is detected.
 * @param shakeThreshold Threshold to determine a shake.
 * @param shakeInterval Minimum interval between shake detections.
 */
class ShakeDetector(
    private val onShake: () -> Unit,
    private val shakeThreshold: Float = 15.0f,
    private val shakeInterval: Long = 5000
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

            val deltaX = abs(x - lastAccelX)
            val deltaY = abs(y - lastAccelY)
            val deltaZ = abs(z - lastAccelZ)

            val gForce = deltaX + deltaY + deltaZ

            if (gForce > shakeThreshold) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastShakeTime > shakeInterval) {
                    lastShakeTime = currentTime
                    onShake()
                }
            }

            lastAccelX = x
            lastAccelY = y
            lastAccelZ = z
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No implementation needed
    }
}