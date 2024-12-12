package com.example.uts_vego

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AdminScreen(navController: NavController, viewModel: RestoViewModel) {
    var showAddRestoDialog by remember { mutableStateOf(false) }
    var selectedResto by remember { mutableStateOf<RestoItem?>(null) }
    var showAddMenuDialog by remember { mutableStateOf(false) }

    val userId = FirebaseAuth.getInstance().currentUser?.uid

    Scaffold(
        topBar = {
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
                    if (userId != null) {
                        viewModel.addResto(name, userId) // Pass userId to addResto
                        showAddRestoDialog = false
                    } else {
                        // Handle case where user is not authenticated
                        Log.e("AuthError", "User not authenticated")
                    }
                }
            )
        }

        if (showAddMenuDialog && selectedResto != null) {
            AddMenuDialog(
                onDismiss = { showAddMenuDialog = false },
                onAdd = { menuName, price ->
                    viewModel.addMenu(selectedResto!!, menuName, price)
                    showAddMenuDialog = false
                }
            )
        }
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

            // Display menus
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
    onAdd: (String) -> Unit
) {
    var restoName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Restaurant") },
        text = {
            TextField(
                value = restoName,
                onValueChange = { restoName = it },
                label = { Text("Restaurant Name") }
            )
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
    onAdd: (String, Int) -> Unit
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