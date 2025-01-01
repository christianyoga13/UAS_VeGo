package com.example.uts_vego

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import android.net.Uri
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

data class RestoItem(
    @DrawableRes val imageRes: Int,
    val restaurantId: String,
    val name: String,
    val rating: Double,
    val time: String,
    val distance: String,
    val tags: List<String>,
    val menuItems: List<MenuItem>,
    val location: Location? = null,
    val imageUrl: String? = null
)

data class MenuItem(
    val name: String,
    val price: Int,
    @DrawableRes val imageres: Int,
    val imageUrl: String? = null
)

data class Location(
    val latitude: Double,
    val longitude: Double
)

class RestoViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _restoList = mutableStateListOf<RestoItem>()
    val restoList: List<RestoItem> get() = _restoList

    fun fetchRestosFromFirestore() {
        db.collection("restaurants")
            .get()
            .addOnSuccessListener { documents ->
                _restoList.clear()
                for (document in documents) {
                    val name = document.getString("name") ?: continue
                    val imageUrl = document.getString("imageUrl") ?: ""
                    val latitude = document.getDouble("latitude") ?: 0.0
                    val longitude = document.getDouble("longitude") ?: 0.0
                    val menus = document.get("menus") as? List<Map<String, Any>> ?: listOf()

                    val menuItems = menus.map { menuMap ->
                        MenuItem(
                            name = menuMap["name"] as? String ?: "",
                            price = (menuMap["price"] as? Number)?.toInt() ?: 0,
                            imageres = 0,
                            imageUrl = menuMap["image"] as? String ?: ""
                        )
                    }

                    _restoList.add(
                        RestoItem(
                            imageRes = 0,
                            restaurantId = document.id,
                            name = name,
                            rating = 4.5,
                            time = "20 MINS",
                            distance = "1.5 Km",
                            tags = listOf("New"),
                            menuItems = menuItems,
                            location = Location(latitude, longitude),
                            imageUrl = imageUrl
                        )
                    )
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Error fetching restaurants: ${e.message}")
            }
    }


    fun addResto(name: String, userId: String, location: Location, imageUrl: String) {
        val resto = hashMapOf(
            "name" to name,
            "menus" to listOf<Map<String, Any>>(),
            "ownerId" to userId,
            "latitude" to location.latitude,
            "longitude" to location.longitude,
            "imageUrl" to imageUrl
        )

        db.collection("restaurants")
            .add(resto)
            .addOnSuccessListener { document ->
                _restoList.add(
                    RestoItem(
                        imageRes = R.drawable.resto_image,
                        restaurantId = document.id,
                        name = name,
                        rating = 4.5,
                        time = "20 MINS",
                        distance = "1.5 Km",
                        tags = listOf("New"),
                        menuItems = listOf(),
                        location = location,
                        imageUrl = imageUrl
                    )
                )
            }
    }

    fun addMenu(resto: RestoItem, menuName: String, price: Int, imageUrl: String) {
        val menuItem = hashMapOf(
            "name" to menuName,
            "price" to price,
            "image" to imageUrl
        )

        db.collection("restaurants")
            .document(resto.restaurantId)
            .get()
            .addOnSuccessListener { document ->
                val currentMenus = document.get("menus") as? List<Map<String, Any>> ?: listOf()
                val updatedMenus = currentMenus + menuItem

                document.reference.update("menus", updatedMenus)
                    .addOnSuccessListener {
                        val index = _restoList.indexOf(resto)
                        if (index != -1) {
                            _restoList[index] = resto.copy(
                                menuItems = resto.menuItems + MenuItem(
                                    name = menuName,
                                    price = price,
                                    imageres = 0,  // Atau simpan URL di model MenuItem jika ingin
                                    imageUrl = imageUrl
                                )
                            )
                        }
                    }
            }
    }

    fun deleteResto(resto: RestoItem) {
        db.collection("restaurants")
            .document(resto.restaurantId) // Gunakan restaurantId untuk menghapus restoran
            .delete()
            .addOnSuccessListener {
                _restoList.remove(resto)
            }
    }

    fun uploadImageToFirebaseStorage(uri: Uri, onSuccess: (String) -> Unit, onFailure: (String) -> Unit) {
        val storageReference = FirebaseStorage.getInstance().reference
        val imageRef = storageReference.child("restos/${UUID.randomUUID()}.jpg")  // Menyimpan dengan nama unik

        imageRef.putFile(uri)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    onSuccess(downloadUrl.toString())
                }
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Upload failed")
            }
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    fun initLocationClient(context: Context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    }

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(context: Context, callback: (Location?) -> Unit) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.e("LocationError", "GPS is not enabled")
            callback(null)
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: android.location.Location? ->
                if (location != null) {
                    callback(Location(location.latitude, location.longitude))
                } else {
                    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                        .addOnSuccessListener { loc: android.location.Location? ->
                            if (loc != null) {
                                callback(Location(loc.latitude, loc.longitude))
                            } else {
                                Log.e("LocationError", "Failed to get current location")
                                callback(null)
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("LocationError", "Error getting current location: ${e.message}")
                            callback(null)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("LocationError", "Error getting last location: ${e.message}")
                callback(null)
            }
    }
}
