package com.example.uts_vego

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import android.location.Location as AndroidLocation
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import kotlinx.coroutines.delay
import kotlin.math.pow
import kotlin.math.sqrt

// Convert between AndroidLocation and the app's Location class
fun AndroidLocation.toAppLocation(): Location {
    return Location(this.latitude, this.longitude)
}

fun Location.toAndroidLocation(): AndroidLocation {
    return AndroidLocation("").apply {
        latitude = this@toAndroidLocation.latitude
        longitude = this@toAndroidLocation.longitude
    }
}

@Composable
fun RestaurantNearByScreen(
    navController: NavController,
    restoViewModel: RestoViewModel = viewModel()
) {
    val context = LocalContext.current
    var userLocation by remember { mutableStateOf<Location?>(null) }

    // Hardcoded data integration
    val fastServeItems = getFastServeItems()
    val bigDiscountItems = getBigDiscountItems()
    val bestSellerItems = getBestSellerItems()
    val restoItems = getRestoItems()

    // Combine all the data into one list for display
    val restoList = remember {
        fastServeItems + bigDiscountItems + bestSellerItems + restoItems + restoViewModel.restoList
    }

    // Initialize location client first
    LaunchedEffect(Unit) {
        restoViewModel.initLocationClient(context)
    }

    // Get user location after initialization
    LaunchedEffect(Unit) {
        restoViewModel.getCurrentLocation(context) { location ->
            userLocation = location
        }
    }

    // Sort restaurants by distance
    val sortedRestoList = remember(userLocation) {
        restoList.sortedBy { resto ->
            val restoLocation = resto.location
            if (restoLocation != null && userLocation != null) {
                val androidUserLocation = AndroidLocation("").apply {
                    latitude = userLocation!!.latitude
                    longitude = userLocation!!.longitude
                }
                val androidRestoLocation = AndroidLocation("").apply {
                    latitude = restoLocation.latitude
                    longitude = restoLocation.longitude
                }
                calculateDistance(androidUserLocation, androidRestoLocation)
            } else {
                Double.MAX_VALUE
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nearby Restaurants") },
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
            Text("Restaurants Near You", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))

            if (userLocation != null) {
                LazyColumn {
                    items(sortedRestoList) { resto ->
                        RestaurantCard(
                            resto = resto,
                            onClick = {
                                navController.navigate("restoDetail/${resto.name}")
                            }
                        )
                    }
                }
            } else {
                Text("Loading your location...", fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}


@Composable
fun RestaurantCard(
    resto: RestoItem,
    onClick: () -> Unit
) {
    var animated by remember { mutableStateOf(false) }
    val animatedScale by animateFloatAsState(
        targetValue = if (animated) 0.95f else 1f,
        animationSpec = tween(durationMillis = 100), label = ""
    )

    MaterialTheme { // Pastikan di dalam MaterialTheme
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .scale(animatedScale)
                .clickable(
                    onClick = {
                        animated = !animated
                        onClick()
                    },
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(bounded = true)
                ),
            elevation = 4.dp, // Langsung berikan nilai Dp
            shape = RoundedCornerShape(12.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colors.surface // Akses colors (bukan colorScheme)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .padding(end = 16.dp)
                    ) {
                        Image(
                            painter = rememberImagePainter(data = resto.imageUrl ?: resto.imageRes),
                            contentDescription = "Restaurant Image",
                            modifier = Modifier
                                .matchParentSize()
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.6f)
                                        ),
                                        startY = 0.5f
                                    )
                                )
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterVertically)
                    ) {
                        Text(
                            text = resto.name,
                            style = MaterialTheme.typography.h6, // Gunakan style dari Material 2
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Rating: ${resto.rating}",
                            style = MaterialTheme.typography.body2, // Gunakan style dari Material 2
                            color = Color.Gray
                        )
                        Text(
                            text = "Distance: ${resto.distance}",
                            style = MaterialTheme.typography.body2, // Gunakan style dari Material 2
                            color = Color.Gray
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.ArrowForward,
                        contentDescription = "Go to Detail",
                        modifier = Modifier.align(Alignment.CenterVertically),
                        tint = MaterialTheme.colors.primary // Akses colors (bukan colorScheme)
                    )
                }
                LaunchedEffect(animated) {
                    if (animated) {
                        delay(100)
                        animated = false
                    }
                }
            }
        }
    }
}

fun calculateDistance(userLocation: AndroidLocation, restoLocation: AndroidLocation): Double {
    val latDiff = userLocation.latitude - restoLocation.latitude
    val lonDiff = userLocation.longitude - restoLocation.longitude
    return sqrt(latDiff.pow(2) + lonDiff.pow(2)) * 111.32 // Converting to km
}