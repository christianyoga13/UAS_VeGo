package com.example.uts_vego

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*

data class DeliveryOption(
    val name: String,
    val price: Int
)

@Composable
fun MapPage(
    navController: NavController,
    restaurantId: String?,
    total: Double,
    restoItems: List<RestoItem>,
    cartViewModel: CartViewModel,
    paymentViewModel: PaymentViewModel
) {
    var restaurantLocation by remember { mutableStateOf<LatLng?>(null) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val cameraPositionState = rememberCameraPositionState()

    val deliveryOptions = listOf(
        DeliveryOption("Sharing Delivery", 5000),
        DeliveryOption("Regular Delivery", 10000),
        DeliveryOption("Premium Delivery", 20000)
    )

    var selectedDeliveryOption by remember { mutableStateOf<DeliveryOption?>(null) }
    var expanded by remember { mutableStateOf(false) }

    val balance by paymentViewModel.balance.collectAsState(initial = 0.0)

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                fetchUserLocation(fusedLocationClient) { location ->
                    userLocation = location
                }
            } else {
                errorMessage = "Izin lokasi ditolak."
            }
        }
    )

    Log.d("MapPage", "restaurantId: $restaurantId")
    Log.d("MapPage", "restoItems: $restoItems")


    LaunchedEffect(Unit) {
        when (ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )) {
            PackageManager.PERMISSION_GRANTED -> {
                fetchUserLocation(fusedLocationClient) { location ->
                    userLocation = location
                }
            }
            else -> locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    LaunchedEffect(restaurantId) {
        val resto = restoItems.find { it.restaurantId == restaurantId }

        if (resto != null) {
            resto.location?.let { location ->
                restaurantLocation = LatLng(location.latitude, location.longitude)
            }
            isLoading = false
        } else {
            errorMessage = "Restoran tidak ditemukan!"
            isLoading = false
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
                    title = { Text("Restaurant Navigation", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    backgroundColor = Color(0xFFFFA500)
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                errorMessage != null -> {
                    Text(
                        text = errorMessage ?: "Terjadi kesalahan.",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colors.error
                    )
                }
                else -> {
                    LaunchedEffect(restaurantLocation, userLocation) {
                        val currentRestaurantLocation = restaurantLocation
                        val currentUserLocation = userLocation

                        if (currentRestaurantLocation != null && currentUserLocation != null) {
                            val boundsBuilder = LatLngBounds.Builder()
                            boundsBuilder.include(currentRestaurantLocation)
                            boundsBuilder.include(currentUserLocation)
                            val bounds = boundsBuilder.build()

                            cameraPositionState.move(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                        } else {
                            restaurantLocation?.let { location ->
                                cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(location, 15f))
                            }
                        }
                    }

                    GoogleMap(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 180.dp),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(isMyLocationEnabled = true)
                    ) {
                        restaurantLocation?.let { location ->
                            Marker(
                                state = MarkerState(position = location),
                                title = "Restaurant",
                                snippet = "Lokasi Restoran"
                            )
                        }

                        userLocation?.let { userLoc ->
                            Marker(
                                state = MarkerState(position = userLoc),
                                title = "You",
                                snippet = "Lokasi Anda"
                            )

                            restaurantLocation?.let { restoLoc ->
                                Polyline(
                                    points = listOf(userLoc, restoLoc),
                                    color = MaterialTheme.colors.primary,
                                    width = 5f
                                )
                            }
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                        elevation = 8.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .align(Alignment.BottomCenter)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFFFFA500))
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Online Order",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingBag,
                                    contentDescription = "Shopping Bag Icon",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { expanded = !expanded },
                                    colors = ButtonDefaults.buttonColors(
                                        backgroundColor = Color.White,
                                        contentColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(40.dp)
                                ) {
                                    Text(text = selectedDeliveryOption?.name ?: "Choose Order")
                                }
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    deliveryOptions.forEach { option ->
                                        DropdownMenuItem(
                                            onClick = {
                                                selectedDeliveryOption = option
                                                expanded = false
                                            }
                                        ) {
                                            Text(text = "${option.name} (+Rp ${option.price})")
                                        }
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 64.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Rp ${"%,.0f".format(balance)}",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                                Icon(
                                    imageVector = Icons.Filled.MoreVert,
                                    contentDescription = "More Options",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Button(
                                onClick = {
                                    val deliveryFee = selectedDeliveryOption?.price ?: 0
                                    val tax = (total + deliveryFee) * 0.11
                                    val totalPrice = total + deliveryFee + tax

                                    navController.navigate(
                                        "confirmation/${totalPrice}/${selectedDeliveryOption?.name ?: "None"}/${selectedDeliveryOption?.price ?: 0}/${total}"
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = Color(0xFF00C853),
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(text = "Order")
                            }
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
private fun fetchUserLocation(
    fusedLocationClient: FusedLocationProviderClient,
    onLocationFetched: (LatLng?) -> Unit
) {
    fusedLocationClient.lastLocation
        .addOnSuccessListener { location ->
            if (location != null) {
                onLocationFetched(LatLng(location.latitude, location.longitude))
            } else {
                fusedLocationClient.getCurrentLocation(
                    com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                    null
                ).addOnSuccessListener { currentLocation ->
                    if (currentLocation != null) {
                        onLocationFetched(LatLng(currentLocation.latitude, currentLocation.longitude))
                    } else {
                        onLocationFetched(null)
                    }
                }.addOnFailureListener {
                    Log.e("LocationError", "Gagal mendapatkan lokasi terkini: ${it.message}")
                    onLocationFetched(null)
                }
            }
        }
        .addOnFailureListener {
            Log.e("LocationError", "Gagal mendapatkan lokasi terakhir: ${it.message}")
            onLocationFetched(null)
        }
}