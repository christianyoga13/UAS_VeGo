package com.example.uts_vego

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage

@Composable
fun RestoDetailScreen(navController: NavController, restoItem: RestoItem) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = restoItem.name, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                backgroundColor = Color.White
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Display Image (either from URL or drawable resource)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                if (restoItem.imageRes is Int) {
                    Image(
                        painter = painterResource(id = restoItem.imageRes),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = restoItem.imageRes as Int),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "${restoItem.name} - Gading Serpong", // You can adjust the location
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color.Yellow
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${restoItem.rating} • ${restoItem.time} • ${restoItem.distance}",
                            style = MaterialTheme.typography.body2,
                            color = Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Menu", // Updated to "Menu"
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxHeight()
            ) {
                items(restoItem.menuItems) { menu ->
                    MenuCard(menu = menu)
                }
            }
        }
    }
}



@Composable
fun MenuCard(menu: MenuItem) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp) // Adjust card height as needed
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {

                if (menu.image.isNotEmpty()){
                    AsyncImage(
                        model = menu.image,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop,
                        error = painterResource(id = R.drawable.ic_launcher_foreground) // Placeholder
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = menu.name,
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Rp ${menu.price}",
                    style = MaterialTheme.typography.body2,
                    color = Color.Gray
                )
            }

            IconButton(
                onClick = { /* Handle add to cart */ },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(36.dp)
                    .background(Color(0xFFFFA500), shape = RoundedCornerShape(50))
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_cart), // Assuming you have an add to cart icon
                    contentDescription = "Add to cart",
                    tint = Color.White
                )
            }
        }

    }
}

fun getRecommendedItems(): List<MenuItem> {
    return listOf(
        MenuItem("Drumstick Vegetarian", 20000, "vegan_food"),
        MenuItem("Gyoza Vegetarian", 25000, "vegan_food"),
        MenuItem("Perkedel Vegetarian", 20000, "vegan_food"),
        MenuItem("Daging Vegetarian", 25000, "vegan_food")
        // Add more menu items as needed
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewRestoDetailScreen() {
    RestoDetailScreen(
        navController = rememberNavController(),
        restoItem = RestoItem(
            imageRes = R.drawable.resto_image, // Example drawable resource
            name = "Vegetarian Restaurant",
            rating = 4.5,
            time = "10 min",
            distance = "1 km",
            tags = listOf("Vegetarian", "Healthy", "Vegan"),
            menuItems = getRecommendedItems()
        )
    )
}