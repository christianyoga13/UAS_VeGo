package com.example.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            MaterialTheme {
                SettingsScreenWithScaffold(navController = navController)
            }
        }
    }
}

@Composable
fun SettingsScreenWithScaffold(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFFFFA500)
                        )
                    }
                },
                backgroundColor = Color.White,
                elevation = 0.dp
            )
        },
        backgroundColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            SettingsSection(
                title = "Privacy",
                options = listOf("Communications", "Ads", "Location Sharing")
            )
            Divider(
                color = Color(0xFFFFA500),
                thickness = 1.dp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            SettingsSection(
                title = "Security",
                options = listOf("Account Security")
            )
        }
    }
}

@Composable
fun SettingsSection(title: String, options: List<String>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFFA500),
            fontSize = 16.sp,
            modifier = Modifier.padding(16.dp)
        )
        options.forEach { option ->
            SettingOption(option)
        }
    }
}

@Composable
fun SettingOption(option: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Handle option click */ }
    ) {
        Text(
            text = option,
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
        Divider(
            color = Color(0xFFFFA500),
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}
