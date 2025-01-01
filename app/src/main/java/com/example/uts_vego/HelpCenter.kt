package com.example.uts_vego

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class HelpCenterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "help_center") {
                composable("help_center") {
                    HelpCenterScreen(navController = navController)
                }
            }
        }
    }
}



@Composable
fun HelpCenterScreen(navController: NavController) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Help Center",
                        color = Color(0xFFFFA500),
                        fontWeight = FontWeight.Bold
                    )
                },
                backgroundColor = Color.White,
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFFFFA500)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Popular Topics",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFA500),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // List of popular topics
            val topics = listOf(
                "What if the Vcash balance hasn't arrived yet?",
                "I can't use Vcash",
                "Balance transfer to bank has not been received",
                "Claim balance refund"
            )

            topics.forEach { topic ->
                TopicItem(topic = topic, onClick = {
                    // Handle navigation or action here
                    Toast.makeText(context, "Selected: $topic", Toast.LENGTH_SHORT).show()
                })
            }
        }
    }
}

@Composable
fun TopicItem(topic: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = topic,
            fontSize = 16.sp,
            color = Color.Black
        )
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            color = Color(0xFFFFA500),
            thickness = 0.5.dp
        )
    }
}
