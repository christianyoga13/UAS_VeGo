package com.example.uts_vego

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class CarouselItem(
    val imageResId: Int,
    val title: String
)


class OnlineOrder : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MainAppNavigation()
        }
    }
}

@Composable
fun OnlineOrderScreen(navController: NavController, viewModel: RestoViewModel) {
    val items = listOf(
        CarouselItem(R.drawable.heavy_meal, "Heavy Meal"),
        CarouselItem(R.drawable.snack, "Snack"),
        CarouselItem(R.drawable.ic_cart, "Groceries"),
        CarouselItem(R.drawable.drink, "Drink")
    )

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var isAdmin by remember { mutableStateOf(false) }

    val user = auth.currentUser
    if (user != null) {
        // Ambil role pengguna dari Firestore
        LaunchedEffect(user.uid) {
            db.collection("users").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val role = document.getString("role")
                        if (role == "admin") {
                            isAdmin = true  // Jika role admin, set isAdmin = true
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Gagal mendapatkan data role", Toast.LENGTH_SHORT).show()
                }
        }
    }


    // Memuat data restoran saat screen dibuka
    LaunchedEffect(Unit) {
        viewModel.fetchRestosFromFirestore()  // Memanggil untuk mengambil data restoran
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Online Order") },
                actions = {
                    if (isAdmin) {
                        TextButton(onClick = { navController.navigate("AdminScreen") }) {
                            Text("Admin", color = MaterialTheme.colors.onPrimary)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(0.dp))
                    }

                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Text(text = "Kategori", modifier = Modifier.padding(16.dp), fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Carousel(items = items)
            Spacer(modifier = Modifier.height(24.dp))
            ButtonGroup()
            Spacer(modifier = Modifier.height(24.dp))
            OrderNowCarousel()
            Spacer(modifier = Modifier.height(24.dp))

            // Restoran yang Ditambahkan
            if (viewModel.restoList.isNotEmpty()) {
                ReusableRestoSection(
                    title = "Your Added Restaurants",
                    items = viewModel.restoList.map { resto ->
                        RestoItem(
                            imageRes = R.drawable.resto_image, // Gambar default
                            name = resto.name,
                            rating = 4.5, // Default rating
                            time = "20 MINS", // Default time
                            distance = "1.5 Km", // Default distance
                            tags = listOf("User Added"),
                            menuItems = resto.menuItems
                        )
                    },
                    onSeeAllClick = { /* Handle See All */ },
                    onCardClick = { restoItem ->
                        navController.navigate("restoDetail/${restoItem.name}")
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            ReusableRestoSection(
                title = "24 Hours",
                items = getRestoItems(),
                onSeeAllClick = { /* Handle See All */ },
                onCardClick = { restoItem ->
                    navController.navigate("restoDetail/${restoItem.name}")
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            ReusableRestoSection(
                title = "Fast Serve",
                items = getFastServeItems(),
                onSeeAllClick = { /* Handle See All */ },
                onCardClick = { restoItem ->
                    navController.navigate("restoDetail/${restoItem.name}")
                }
            )
        }
    }
}


@Composable
fun TopBarWithSearchBar(navController: NavController) {
    Column(modifier = Modifier.background(Color(0xFFFFA500))) {
        TopAppBar(
            title = {
                Text(text = "Online Order", color = Color.White)
            },
            navigationIcon = {
                IconButton(onClick = { navController.navigate("home") }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            },
            backgroundColor = Color(0xFFFFA500),
            elevation = 0.dp
        )

        SearchBar(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
        )
    }
}

@Composable
fun SearchBar(modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = "",
        onValueChange = {},
        leadingIcon = {
            Icon(Icons.Filled.Search, contentDescription = "Search Icon")
        },
        placeholder = { Text("Let's order something!") },
        modifier = modifier
            .background(Color.White, shape = RoundedCornerShape(16.dp)),
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            backgroundColor = Color.White,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent
        )
    )
}

@Composable
fun Carousel(items: List<CarouselItem>) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items) { item ->
            CarouselCard(item)
        }
    }
}

@Composable
fun CarouselCard(item: CarouselItem) {
    Column(
        modifier = Modifier
            .width(150.dp)
            .padding(8.dp)
            .background(MaterialTheme.colors.surface, shape = RoundedCornerShape(16.dp))
            .clickable { /* Handle click */ }
    ) {
        Image(
            painter = painterResource(id = item.imageResId),
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .height(100.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = item.title,
            style = MaterialTheme.typography.subtitle1,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

@Composable
fun ButtonGroup() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ButtonOneByOne(
                title = "Open 24 Hours",
                description = "Ready anytime",
                icon = Icons.Default.AccessTime,
                iconColor = Color.Green
            )
            ButtonOneByOne(
                title = "Fast Serve",
                description = "Serve for u",
                icon = Icons.Default.Restaurant,
                iconColor = Color.Red

            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ButtonOneByOne(
                title = "Big Discount",
                description = "Discount up to 50%",
                icon = Icons.Default.Percent,
                iconColor =  Color.Blue
            )
            ButtonOneByOne(
                title = "Best Seller",
                description = "Recommended",
                icon = Icons.Default.Star,
                iconColor = Color.Yellow
            )
        }
    }
}

@Composable
fun ButtonOneByOne(
    title: String,
    description: String,
    icon: ImageVector,
    iconColor: Color
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = 4.dp,
        modifier = Modifier
            .width(160.dp)
            .height(80.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.subtitle2,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.body2,
                    color = Color.Gray
                )
            }

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier
                    .size(30.dp)
                    .align(Alignment.Bottom)
            )
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun OrderNowCarousel() {
    val pagerState = rememberPagerState()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFECECEC))
            .padding(vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            // Header Order Now
            Text(
                text = "Order Now",
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalPager(
                count = getOrderItems().size, // Jumlah item
                state = pagerState,
                contentPadding = PaddingValues(horizontal = 8.dp), // Padding untuk snap
            ) { page ->
                OrderCard(orderItem = getOrderItems()[page])
            }
        }
    }
}

@Composable
fun OrderCard(orderItem: OrderItem) {
    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(horizontal = 32.dp)
    ) {
        Column {
            // Gambar di atas
            Image(
                painter = painterResource(id = orderItem.imageRes),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )
            // Teks di bawah
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = orderItem.title,
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = orderItem.subtitle,
                    style = MaterialTheme.typography.body2,
                    color = Color.Gray
                )
            }
        }
    }
}

fun getOrderItems(): List<OrderItem> {
    return listOf(
        OrderItem(R.drawable.carousel_image1, "Salad Buah Murah, Cuma 10k", "Ad - Salad Buah Eni"),
        OrderItem(R.drawable.carousel_image2, "Fresh Juice, Mulai 15k", "Ad - Juice Segar"),
        OrderItem(R.drawable.carousel_image1, "Roti Panggang Enak", "Ad - Toast House")
    )
}

data class OrderItem(
    val imageRes: Int,
    val title: String,
    val subtitle: String
)

@Composable
fun ReusableRestoSection(
    title: String,
    items: List<RestoItem>,
    onSeeAllClick: () -> Unit,
    onCardClick: (RestoItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onSeeAllClick) {
                Text(text = "See All", color = Color.Green)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // LazyRow untuk menampilkan daftar restoran
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { item ->
                RestoCard(restoItem = item, onClick = { onCardClick(item) })
            }
        }
    }
}

@Composable
fun RestoCard(restoItem: RestoItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(180.dp)
            .wrapContentHeight()
            .clickable { onClick() },
        elevation = 4.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            // Gambar
            Image(
                painter = painterResource(id = restoItem.imageRes),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Nama restoran
            Text(
                text = restoItem.name,
                style = MaterialTheme.typography.subtitle1,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Rating dan waktu/jarak
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = Color.Yellow,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = restoItem.rating.toString(),
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(start = 4.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${restoItem.time} • ${restoItem.distance}",
                    style = MaterialTheme.typography.body2,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Tags
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                restoItem.tags.forEach { tag ->
                    Text(
                        text = tag,
                        style = MaterialTheme.typography.caption,
                        color = Color.White,
                        modifier = Modifier
                            .background(
                                color = Color.Green,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

fun getFastServeItems(): List<RestoItem> {
    return listOf(
        RestoItem(
            imageRes = R.drawable.vegan_food,
            name = "Vegetarian Mix",
            rating = 4.8,
            time = "15 MINS",
            distance = "0.8 Km",
            tags = listOf("Healthy", "Cheap"),
            menuItems = listOf(
                MenuItem("Fruit + Vegetable Salad", 20000, "vegan_food"),
                MenuItem("Vegan Rendang", 25000, "vegan_food")
            )
        ),
        RestoItem(
            imageRes = R.drawable.resto_image,
            name = "Asian Delight",
            rating = 4.7,
            time = "10 MINS",
            distance = "1.0 Km",
            tags = listOf("Quick Serve", "Popular"),
            menuItems = listOf(
                MenuItem("Mie Kangkung", 20000, "vegan_food"),
                MenuItem("Satay for Vegan", 25000, "vegan_food")
            )
        )
    )
}

fun getBigDiscountItems(): List<RestoItem> {
    return listOf(
        RestoItem(
            imageRes = R.drawable.vegan_smoothie,
            name = "Losing Hut",
            rating = 4.5,
            time = "30 MINS",
            distance = "2.0 Km",
            tags = listOf("Cheap", "Discount"),
            menuItems = listOf(
                MenuItem("Vegetable Pizza", 20000, "vegan_food"),
                MenuItem("Cream Soup", 25000, "vegan_food")
            )
        ),
        RestoItem(
            imageRes = R.drawable.vegan_food,
            name = "Festival",
            rating = 4.3,
            time = "25 MINS",
            distance = "1.5 Km",
            tags = listOf("Deal", "Limited"),
            menuItems = listOf(
                MenuItem("Candy", 20000, "vegan_food"),
                MenuItem("Cookies", 25000, "vegan_food")
            )
        )
    )
}

fun getBestSellerItems(): List<RestoItem> {
    return listOf(
        RestoItem(
            imageRes = R.drawable.vegan_food,
            name = "Resto Salad Sayur",
            rating = 4.9,
            time = "20 MINS",
            distance = "1.2 Km",
            tags = listOf("Best Resto", "Top Rated"),
            menuItems = listOf(
                MenuItem("Salad Buah", 20000, "vegan_food"),
                MenuItem("Sup Sehat", 25000, "vegan_food")
            )
        ),
        RestoItem(
            imageRes = R.drawable.resto_image,
            name = "RM. Vegan Indo",
            rating = 4.8,
            time = "15 MINS",
            distance = "1.0 Km",
            tags = listOf("Recommended", "Vegan"),
            menuItems = listOf(
                MenuItem("Salad Buah", 20000, "vegan_food"),
                MenuItem("Sup Sehat", 25000, "vegan_food")
            )
        )
    )
}

fun getRestoItems(): List<RestoItem> {
    return listOf(
        RestoItem(
            imageRes = R.drawable.resto_image,
            name = "Resto Salad Sayur",
            rating = 4.5,
            time = "20 MINS",
            distance = "1.5 Km",
            tags = listOf("Best Resto", "Cheap"),
            menuItems = listOf(
                MenuItem("Salad Buah", 20000, "vegan_food"),
                MenuItem("Sup Sehat", 25000, "vegan_food")
            )
        ),
        RestoItem(
            imageRes = R.drawable.vegan_food,
            name = "RM. Vegan Indo",
            rating = 4.7,
            time = "20 MINS",
            distance = "1.5 Km",
            tags = listOf("Near You", "Recommend"),
            menuItems = listOf(
                MenuItem("Pecel Vegan", 18000, "vegan_food"),
                MenuItem("Sate Vegan", 22000, "vegan_food")
            )
        ),
        RestoItem(
            imageRes = R.drawable.resto_image,
            name = "Resto Jepang Fusion",
            rating = 4.8,
            time = "25 MINS",
            distance = "2.0 Km",
            tags = listOf("Flexitarian", "Popular"),
            menuItems = listOf(
                MenuItem("Ramen Vegan", 30000, "vegan_food"),
                MenuItem("Sushi Vegan", 28000, "vegan_food")
            )
        )
    )
}

@Preview(showBackground = true)
@Composable
fun OnlineOrderPreview() {
    MaterialTheme {
        OnlineOrderScreen(navController = rememberNavController(), viewModel = RestoViewModel())
    }
}
