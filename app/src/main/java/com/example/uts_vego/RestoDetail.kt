package com.example.uts_vego

import android.util.Log
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage

@Composable
fun RestoDetailScreen(
    navController: NavController,
    restoItem: RestoItem,
    cartViewModel: CartViewModel
) {
    val cartItems by cartViewModel.cartItemsState.collectAsState()

    LaunchedEffect(restoItem.restaurantId) {
        cartViewModel.fetchCartItemsByRestaurant(restoItem.restaurantId)
    }

    val restaurantCartItems = cartItems

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .background(Color(0xFFFFA500))
                    .statusBarsPadding()
            ) {
                TopAppBar(
                    title = { Text(text = restoItem.name, fontSize = 20.sp, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    backgroundColor = Color(0xFFFFA500)
                )
            }
        },
        bottomBar = {
            if (restaurantCartItems.isNotEmpty()) {
                CustomBottomCartBar(
                    itemCount = restaurantCartItems.sumOf { it.quantity },
                    totalPrice = restaurantCartItems.sumOf { it.price * it.quantity },
                    restaurantName = restoItem.name,
                    onCheckoutClicked = {
                        navController.navigate("checkout_page/${restoItem.restaurantId}")
                    }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Gambar restoran menggunakan AsyncImage
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                if (!restoItem.imageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = restoItem.imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.resto_image),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxHeight()
            ) {
                items(restoItem.menuItems) { menu ->
                    MenuCard(
                        menu = menu,
                        onAddToCart = {
                            cartViewModel.addToCart(menu, restoItem.restaurantId, restoItem.name)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MenuCard(menu: MenuItem, onAddToCart: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                if (!menu.imageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = menu.imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.vegan_food),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(16.dp))
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
                onClick = { onAddToCart() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(36.dp)
                    .background(Color(0xFFFFA500), shape = RoundedCornerShape(50))
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_cart),
                    contentDescription = "Add to cart",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun CustomBottomCartBar(
    itemCount: Int,
    totalPrice: Int,
    restaurantName: String,
    onCheckoutClicked: () -> Unit
) {
    Log.d("CustomBottomCartBar", "ItemCount: $itemCount, TotalPrice: $totalPrice, Restaurant: $restaurantName")

    if (itemCount > 0) {
        Card(
            backgroundColor = Color(0xFFFFA500),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            elevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "$itemCount Menu",
                        color = Color.White,
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Pesan antar dari $restaurantName",
                        color = Color.White,
                        style = MaterialTheme.typography.caption
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Rp $totalPrice",
                        color = Color.White,
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Button(
                        onClick = { onCheckoutClicked() },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_cart),
                                contentDescription = "Cart",
                                tint = Color(0xFFFFA500)
                            )
                        }
                    }
                }
            }
        }
    }
}