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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import coil.compose.AsyncImage
import com.example.uts_vego.font.MyCustomFont
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

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
    val firestore = FirebaseFirestore.getInstance()
    var bestRecipes by remember { mutableStateOf<List<Recipe>>(emptyList()) }

    LaunchedEffect(Unit) {
        fetchBestRecipes(
            firestore,
            onSuccess = { recipes ->
                bestRecipes = recipes
            },
            onFailure = { e ->
                println("Failed to fetch recipes: ${e.message}")
            }
        )
    }

    Scaffold(
        topBar = {
            TopBarWithSearchBarHome(navController, showSearchBar = true)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            CarouselSection()
            ButtonGridSection(navController)
            Spacer(modifier = Modifier.height(16.dp))
            VeganRecommendationSection()
            Spacer(modifier = Modifier.height(16.dp))
            BestPicksSection(navController)
            Spacer(modifier = Modifier.height(16.dp))
            BestRecipePickSection(
                recipes = bestRecipes,
                onCardClick = { recipe ->
                    navController.navigate("recipeDetail/${recipe.timestamp}") // Navigasi ke layar detail resep
                }
            )
        }
    }
}


@Composable
fun TopBarWithSearchBarHome(
    navController: NavController,
    showSearchBar: Boolean = true
) {
    Column(
        modifier = Modifier
            .background(Color(0xFFFFA500))
            .statusBarsPadding()
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Vego",
                    color = Color.White,
                    fontFamily = MyCustomFont,
                    fontSize = 32.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            },
            backgroundColor = Color(0xFFFFA500),
            elevation = 0.dp
        )

        if (showSearchBar) {
            SearchBarHome(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth()
            )
        }
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
                onClick = { navController.navigate("nearby") }
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
                onClick = { navController.navigate("recipe") }
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
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                fontSize = 16.sp,
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
fun BestPicksSection(navController: NavController) {
    Column(modifier = Modifier.fillMaxWidth()) {
        BestPickCategory(
            title = "Best Restaurant Pick",
            backgroundColor = Color(0xFFFFA500),
            items = getBestPickRestaurants(),
            onCardClick = { restoItem ->
                navController.navigate("restoDetail/${restoItem.name}")
            }
        )
    }
}



@Composable
fun BestPickCategory(
    title: String,
    backgroundColor: Color,
    items: List<RestoItem>,
    onCardClick: (RestoItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { item ->
                PickCard(item = item, onClick = { onCardClick(item) })
            }
        }
    }
}


@Composable
fun PickCard(item: RestoItem, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = 4.dp,
        modifier = Modifier
            .width(160.dp)
            .height(220.dp)
            .clickable { onClick() }
            .padding(top = 16.dp)
    ) {
        Column {
            Image(
                painter = painterResource(id = item.imageRes),
                contentDescription = item.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.star),
                        contentDescription = "Rating",
                        tint = Color.Yellow,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(text = item.rating.toString())
                }
            }
        }
    }
}


data class PickItem(
    val imageRes: Int,
    val title: String,
    val rating: Double,
    val price: String? = null
)

data class VeganRecipe(
    val imageResId: Int,
    val title: String,
    val author: String
)

fun getBestPickRestaurants(): List<RestoItem> {
    return listOf(
        RestoItem(
            imageRes = R.drawable.resto_image,
            restaurantId = "1",
            name = "Resto Salad Sayur",
            rating = 4.9,
            time = "20 MINS",
            distance = "1.5 Km",
            tags = listOf("Top Pick", "Healthy"),
            menuItems = listOf(
                MenuItem("Salad Buah", 20000, R.drawable.vegan_food),
                MenuItem("Sup Sehat", 25000, R.drawable.vegan_food)
            ),
            location = Location(-6.248533599167057, 106.62296950636762)
        ),
        RestoItem(
            imageRes = R.drawable.vegan_food,
            restaurantId = "2",
            name = "Vegan Delight",
            rating = 4.8,
            time = "15 MINS",
            distance = "1.2 Km",
            tags = listOf("Recommended", "Popular"),
            menuItems = listOf(
                MenuItem("Pecel Vegan", 18000, R.drawable.vegan_food),
                MenuItem("Sate Vegan", 22000, R.drawable.vegan_food)
            ),
            location = Location(-6.248533599167057, 106.62296950636762)
        ),
        RestoItem(
            imageRes = R.drawable.resto_image,
            restaurantId = "3",
            name = "Fusion Vegan Resto",
            rating = 4.7,
            time = "25 MINS",
            distance = "2.0 Km",
            tags = listOf("Fusion", "Vegan"),
            menuItems = listOf(
                MenuItem("Ramen Vegan", 30000, R.drawable.vegan_food),
                MenuItem("Sushi Vegan", 28000, R.drawable.vegan_food)
            ),
            location = Location(-6.248533599167057, 106.62296950636762)
        )
    )
}

fun fetchBestRecipes(
    firestore: FirebaseFirestore,
    onSuccess: (List<Recipe>) -> Unit,
    onFailure: (Exception) -> Unit
) {
    firestore.collection("recipes")
        .orderBy("timestamp", Query.Direction.DESCENDING)
        .limit(5) // Ambil maksimal 5 resep terbaru
        .get()
        .addOnSuccessListener { snapshot ->
            val recipes = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Recipe::class.java)
            }
            onSuccess(recipes)
        }
        .addOnFailureListener { e ->
            onFailure(e)
        }
}

@Composable
fun BestRecipePickSection(
    recipes: List<Recipe>,
    onCardClick: (Recipe) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Best Recipe Pick",
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

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(recipes) { recipe ->
                RecipeCard(recipe = recipe, onClick = { onCardClick(recipe) })
            }
        }
    }
}

@Composable
fun RecipeCard(recipe: Recipe, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = 4.dp,
        modifier = Modifier
            .width(160.dp)
            .height(220.dp)
            .clickable { onClick() }
            .padding(top = 16.dp)
    ) {
        Column {
            // Gambar Resep
            AsyncImage(
                model = recipe.imageUrl,
                contentDescription = recipe.content,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
            // Informasi Resep
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = recipe.content,
                    style = MaterialTheme.typography.subtitle1.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "By: ${recipe.username}",
                    style = MaterialTheme.typography.body2.copy(
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                )
            }
        }
    }
}


@Preview
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreenContent(navController = rememberNavController())
    }
}
