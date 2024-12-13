package com.example.uts_vego

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.example.uts_vego.font.MyCustomFont
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState

class HomeScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainAppNavigation()
        }
    }
}

@Composable
fun HomeScreenContent(navController: NavController) {
    Scaffold(
        topBar = {
            TopBarWithSearchBarHome(navController)
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            CarouselSection()
            ButtonGridSection(navController)
            Spacer(modifier = Modifier.height(16.dp))
            VeganRecommendationSection()
            Spacer(modifier = Modifier.height(16.dp))
            BestPicksSection()
        }
    }
}

@Composable
fun TopBarWithSearchBarHome(navController: NavController) {
    Column(modifier = Modifier.background(Color(0xFFFFA500))) {
        TopAppBar(
            title = {
                Text(text = "Vego", color = Color.White, fontFamily = MyCustomFont, fontSize = 32.sp, modifier = Modifier.padding(top = 8.dp))
            },
            backgroundColor = Color(0xFFFFA500),
            elevation = 0.dp
        )

        SearchBarHome(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
        )
    }
}

@Composable
fun SearchBarHome(modifier: Modifier = Modifier) {
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

@OptIn(ExperimentalPagerApi::class)
@Composable
fun CarouselSection() {
    val images = listOf(
        R.drawable.carousel_image1,
        R.drawable.carousel_image2
    )

    val pagerState = rememberPagerState()

    LaunchedEffect(pagerState) {
        while (true) {
            kotlinx.coroutines.delay(5000) // Tunggu selama 5 detik
            val nextPage = (pagerState.currentPage + 1) % images.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    HorizontalPager(
        count = images.size,
        state = pagerState,
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp), // Perbesar tinggi carousel
        contentPadding = PaddingValues(horizontal = 16.dp), // Mengurangi padding agar gambar lebih besar
        itemSpacing = 16.dp // Jarak antar gambar
    ) { page ->
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f) // Proporsi kartu lebih besar (lebar dibanding tinggi)
        ) {
            Image(
                painter = painterResource(id = images[page]),
                contentDescription = "Carousel Image",
                modifier = Modifier
                    .fillMaxSize() // Pastikan gambar mengisi kartu sepenuhnya
                    .clip(RoundedCornerShape(16.dp)), // Rounded corner pada gambar
                contentScale = ContentScale.Crop // Gambar dipotong untuk memenuhi area
            )
        }
    }
}


@Composable
fun ButtonGridSection(navController: NavController) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp), // Tambahkan jarak antar baris
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FeatureButton(
                iconRes = R.drawable.motorcycle,
                label = "Online Order",
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate("onlineOrder") }
            )
            Spacer(modifier = Modifier.width(16.dp)) // Jarak antar tombol
            FeatureButton(
                iconRes = R.drawable.restaurant_logo,
                label = "Restaurant Near You",
                modifier = Modifier.weight(1f),
                onClick = { /*navController.navigate("restaurant")*/ }
            )
        }
        Spacer(modifier = Modifier.height(16.dp)) // Jarak antar baris
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FeatureButton(
                iconRes = R.drawable.recipe,
                label = "Recipe Book",
                modifier = Modifier.weight(1f),
                onClick = { /*navController.navigate("recipe")*/ }
            )
            Spacer(modifier = Modifier.width(16.dp))
            FeatureButton(
                iconRes = R.drawable.forum_chat,
                label = "Forum Chat",
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate("forum") }
            )
        }
    }
}

@Composable
fun FeatureButton(
    iconRes: Int,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = 6.dp, // Perbaikan shadow lebih tegas
        modifier = modifier
            .fillMaxWidth()
            .height(90.dp) // Tinggi tombol agar proporsional
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp), // Padding internal untuk jarak
            verticalAlignment = Alignment.CenterVertically // Logo dan teks sejajar vertikal
        ) {
            // Logo di sisi kiri
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp) // Ukuran logo lebih besar
            )
            Spacer(modifier = Modifier.width(16.dp)) // Jarak antara logo dan teks
            // Teks di sisi kanan
            Text(
                text = label,
                fontSize = 16.sp, // Ukuran teks lebih besar
                color = Color.Black,
                textAlign = TextAlign.Start, // Teks rata kiri
                modifier = Modifier.weight(1f) // Memastikan teks tidak keluar area
            )
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun VeganRecommendationSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp) // Padding luar untuk keseluruhan section
    ) {
        // Header Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "From the Vegans",
                style = MaterialTheme.typography.h6.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFA500) // Warna oranye header
                )
            )
            Text(
                text = "View All",
                style = MaterialTheme.typography.body2.copy(
                    fontSize = 14.sp,
                    color = Color.Green, // Warna teks "View All"
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.clickable { /* Tambahkan aksi untuk View All */ }
            )
        }

        Spacer(modifier = Modifier.height(16.dp)) // Jarak antara header dan carousel

        // Carousel dengan HorizontalPager
        val recipes = listOf(
            VeganRecipe(
                imageResId = R.drawable.carousel_image1, // Ganti dengan resource gambar Anda
                title = "10 Recommendation Vegan Recipe",
                author = "by Doc Seung Kwon"
            ),
            VeganRecipe(
                imageResId = R.drawable.carousel_image2,
                title = "5 Easy Vegan Desserts",
                author = "by Chef Emily Green"
            ),
            VeganRecipe(
                imageResId = R.drawable.carousel_image1,
                title = "Vegan Breakfast Ideas",
                author = "by John Doe"
            )
        )

        val pagerState = rememberPagerState()

        HorizontalPager(
            count = recipes.size,
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp), // Tinggi card carousel
            contentPadding = PaddingValues(horizontal = 0.dp) // Padding kiri dan kanan
        ) { page ->
            VeganRecipeCard(
                recipe = recipes[page],
                modifier = Modifier.padding(horizontal = 16.dp) // Jarak antar card
            )
        }
    }
}

// Card untuk VeganRecipe
@Composable
fun VeganRecipeCard(recipe: VeganRecipe, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = 4.dp,
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp) // Tinggi card
    ) {
        Column {
            // Gambar di bagian atas
            Image(
                painter = painterResource(id = recipe.imageResId),
                contentDescription = recipe.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2f) // Proporsi gambar lebih besar
            )
            // Informasi di bagian bawah
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F5F5)) // Warna abu-abu untuk latar teks
                    .padding(16.dp)
            ) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.subtitle1.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = recipe.author,
                    style = MaterialTheme.typography.body2.copy(
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                )
            }
        }
    }
}

@Composable
fun BestPicksSection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Bagian "Best Restaurant Pick"
        BestPickCategory(
            title = "Best Restaurant Pick",
            backgroundColor = Color(0xFFFFA500), // Warna latar header oranye
            items = listOf(
                PickItem(
                    imageRes = R.drawable.vegan_food, // Ganti dengan resource gambar restoran
                    title = "Selero Kito Vegetarian",
                    rating = 4.9
                ),
                PickItem(
                    imageRes = R.drawable.vegan_smoothie,
                    title = "Vegan & Vegetarians",
                    rating = 4.8
                ),
                PickItem(
                    imageRes = R.drawable.resto_image,
                    title = "Suka Cita Vegan",
                    rating = 4.7
                )
            )
        )

        Spacer(modifier = Modifier.height(16.dp)) // Jarak antar kategori

        // Bagian "Best Recipe Pick"
        BestPickCategory(
            title = "Best Recipe Pick",
            backgroundColor = Color.White, // Latar header putih
            items = listOf(
                PickItem(
                    imageRes = R.drawable.vegan_food, // Ganti dengan resource gambar resep
                    title = "Nasi Padang Vegetarian",
                    rating = 5.0,
                    price = "23.000"
                ),
                PickItem(
                    imageRes = R.drawable.vegan_food,
                    title = "Pecel Sayur",
                    rating = 4.8,
                    price = "25.000"
                )
            )
        )
    }
}

@Composable
fun BestPickCategory(
    title: String,
    backgroundColor: Color,
    items: List<PickItem>
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor) // Background mencakup seluruh area
            .padding(vertical = 8.dp) // Padding di luar area konten
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.h6.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                )
                Text(
                    text = "See More",
                    style = MaterialTheme.typography.body2.copy(
                        fontSize = 14.sp,
                        color = Color.Green,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.clickable { /* Tambahkan aksi untuk See More */ }
                )
            }

            // Horizontal List
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp), // Jarak antara header dan LazyRow
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items) { item ->
                    PickCard(item = item)
                }
            }
        }
    }
}

@Composable
fun PickCard(item: PickItem) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = 4.dp,
        modifier = Modifier
            .width(160.dp) // Tetapkan lebar yang sama untuk semua card
            .height(220.dp) // Tetapkan tinggi yang sama untuk semua card
    ) {
        Column {
            // Gambar bagian atas
            Image(
                painter = painterResource(id = item.imageRes),
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp) // Pastikan tinggi gambar seragam
            )
            // Informasi bagian bawah
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.subtitle1.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.star), // Ikon bintang
                        contentDescription = "Rating",
                        tint = Color.Yellow,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = item.rating.toString(),
                        style = MaterialTheme.typography.body2.copy(
                            fontSize = 12.sp,
                            color = Color.Gray
                        ),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                item.price?.let { price ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = price,
                        style = MaterialTheme.typography.body2.copy(
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    )
                }
            }
        }
    }
}

// Data Model untuk item pick
data class PickItem(
    val imageRes: Int,
    val title: String,
    val rating: Double,
    val price: String? = null // Harga opsional, hanya untuk "Best Recipe Pick"
)

// Model Data untuk VeganRecipe
data class VeganRecipe(
    val imageResId: Int,
    val title: String,
    val author: String
)

@Preview
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreenContent(navController = rememberNavController())
    }
}
