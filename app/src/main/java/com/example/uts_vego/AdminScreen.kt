package com.example.uts_vego

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

@Composable
fun AdminScreen(navController: NavController, viewModel: RestoViewModel) {
    var showAddRestoDialog by remember { mutableStateOf(false) }
    var selectedResto by remember { mutableStateOf<RestoItem?>(null) }
    var showAddMenuDialog by remember { mutableStateOf(false) }
    var currentLocation by remember { mutableStateOf<Location?>(null) }
    var locationPermissionGranted by remember { mutableStateOf(false) }
    var restoImageUri by remember { mutableStateOf<Uri?>(null) }
    var menuImageUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.initLocationClient(context)
    }

    val userId = FirebaseAuth.getInstance().currentUser?.uid

    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        restoImageUri = uri
    }

    val menuImagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        menuImageUri = uri
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .background(Color(0xFFFFA500))
                    .statusBarsPadding()
            ) {
                TopAppBar(
                    title = { Text("Admin Dashboard") },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showAddRestoDialog = true }) {
                            Icon(Icons.Default.Add, "Add Restaurant")
                        }
                    }
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            items(viewModel.restoList) { resto ->
                RestoAdminCard(
                    resto = resto,
                    onAddMenu = {
                        selectedResto = resto
                        showAddMenuDialog = true
                    },
                    onDelete = { viewModel.deleteResto(resto) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        if (showAddRestoDialog) {
            AddRestoDialog(
                onDismiss = { showAddRestoDialog = false },
                onAdd = { name ->
                    if (userId != null && currentLocation != null) {
                        restoImageUri?.let { uri ->
                            viewModel.uploadImageToFirebaseStorage(uri, { imageUrl ->
                                viewModel.addResto(name, userId, currentLocation!!, imageUrl)
                                showAddRestoDialog = false
                            }, { error ->
                                Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
                            })
                        } ?: run {
                            Toast.makeText(
                                context,
                                "Please select an image for the restaurant",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            context,
                            "Please enable location services to add a restaurant",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                },
                onImageSelect = { imagePickerLauncher.launch("image/*") }
            )
        }

        if (showAddMenuDialog && selectedResto != null) {
            AddMenuDialog(
                onDismiss = { showAddMenuDialog = false },
                onAdd = { menuName, price ->
                    menuImageUri?.let { uri ->
                        viewModel.uploadImageToFirebaseStorage(uri, { imageUrl ->
                            viewModel.addMenu(selectedResto!!, menuName, price, imageUrl)
                            showAddMenuDialog = false
                        }, { error ->
                            Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
                        })
                    } ?: run {
                        Toast.makeText(
                            context,
                            "Please select an image for the menu item",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                onImageSelect = { menuImagePickerLauncher.launch("image/*") }
            )
        }
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        locationPermissionGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] ?: locationPermissionGranted
        if (locationPermissionGranted) {
            viewModel.getCurrentLocation(context) { location ->
                currentLocation = location
            }
        } else {
            Log.e("PermissionError", "Location permission denied")
        }
    }

    val permissions = arrayOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )

    LaunchedEffect(key1 = true) {
        requestPermissionLauncher.launch(permissions)
    }
}

@Composable
fun RestoAdminCard(
    resto: RestoItem,
    onAddMenu: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = resto.name, style = MaterialTheme.typography.h6)
                Row {
                    IconButton(onClick = onAddMenu) {
                        Icon(Icons.Default.Add, "Add Menu")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, "Delete Restaurant")
                    }
                }
            }

            resto.menuItems.forEach { menu ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(menu.name)
                    Text("Rp ${menu.price}")
                }
            }
        }
    }
}

@Composable
fun AddRestoDialog(
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit,
    onImageSelect: () -> Unit
) {
    var restoName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Restaurant") },
        text = {
            Column {
                TextField(
                    value = restoName,
                    onValueChange = { restoName = it },
                    label = { Text("Restaurant Name") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onImageSelect) {
                    Text("Select Image")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onAdd(restoName) }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddMenuDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Int) -> Unit,
    onImageSelect: () -> Unit
) {
    var menuName by remember { mutableStateOf("") }
    var menuPrice by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Menu") },
        text = {
            Column {
                TextField(
                    value = menuName,
                    onValueChange = { menuName = it },
                    label = { Text("Menu Name") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = menuPrice,
                    onValueChange = { menuPrice = it },
                    label = { Text("Price") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onImageSelect) {
                    Text("Select Image")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    menuPrice.toIntOrNull()?.let { price ->
                        onAdd(menuName, price)
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
