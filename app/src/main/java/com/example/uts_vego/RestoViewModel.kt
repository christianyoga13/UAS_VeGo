package com.example.uts_vego

import androidx.annotation.DrawableRes
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class RestoItem(
    @DrawableRes val imageRes: Int,
    val name: String,
    val rating: Double,
    val time: String,
    val distance: String,
    val tags: List<String>,
    val menuItems: List<MenuItem>
)

data class MenuItem(
    val name: String,
    val price: Int,
    val image: String // Bisa diubah menjadi gambar atau URL jika diperlukan
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
                    val menus = document.get("menus") as? List<Map<String, Any>> ?: listOf()

                    val menuItems = menus.map { menuMap ->
                        MenuItem(
                            name = menuMap["name"] as? String ?: "",
                            price = (menuMap["price"] as? Number)?.toInt() ?: 0,
                            image = menuMap["image"] as? String ?: ""
                        )
                    }

                    _restoList.add(
                        RestoItem(
                            imageRes = R.drawable.resto_image,
                            name = name,
                            rating = 4.5,
                            time = "20 MINS",
                            distance = "1.5 Km",
                            tags = listOf("New"),
                            menuItems = menuItems
                        )
                    )
                }
            }
    }

    fun addResto(name: String, userId: String) {
        val resto = hashMapOf(
            "name" to name,
            "menus" to listOf<Map<String, Any>>(),
            "ownerId" to userId // Menambahkan ID pengguna sebagai pemilik restoran
        )

        db.collection("restaurants")
            .add(resto)
            .addOnSuccessListener { documentReference ->
                _restoList.add(
                    RestoItem(
                        imageRes = R.drawable.resto_image,
                        name = name,
                        rating = 4.5,
                        time = "20 MINS",
                        distance = "1.5 Km",
                        tags = listOf("New"),
                        menuItems = listOf()
                    )
                )
            }
    }

    fun addMenu(resto: RestoItem, menuName: String, price: Int) {
        val menuItem = hashMapOf(
            "name" to menuName,
            "price" to price,
            "image" to ""
        )

        db.collection("restaurants")
            .whereEqualTo("name", resto.name)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
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
                                        image = ""
                                    )
                                )
                            }
                        }
                }
            }
    }

    fun deleteResto(resto: RestoItem) {
        db.collection("restaurants")
            .whereEqualTo("name", resto.name)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    document.reference.delete()
                        .addOnSuccessListener {
                            _restoList.remove(resto)
                        }
                }
            }
    }
}