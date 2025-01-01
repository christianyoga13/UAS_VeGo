package com.example.uts_vego

import android.util.Log
import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CartItem(
    val name: String = "",
    val price: Int = 0,
    @DrawableRes val imageres: Int = 0,
    var quantity: Int = 1,
    val userId: String = "",
    val restaurantId: String = "",    // Document ID dari restoran
    val restaurantName: String = ""   // Nama display dari restoran
)

class CartViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val cartCollection = db.collection("cartItems")

    private val _cartItemsState = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItemsState: StateFlow<List<CartItem>> get() = _cartItemsState

    /**
     * Fetch cart items based on the current user and restaurantId.
     * Ensures only items from the same restaurant are displayed in the cart.
     */
    fun fetchCartItemsByRestaurant(restaurantId: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        cartCollection
            .whereEqualTo("userId", currentUserId)
            .whereEqualTo("restaurantId", restaurantId) // Query menggunakan Document ID
            .addSnapshotListener { snapshot, e ->
                if (e == null && snapshot != null) {
                    val items = snapshot.toObjects(CartItem::class.java)
                    _cartItemsState.value = items
                    Log.d("FirestoreData", "Items: $items")
                } else {
                    _cartItemsState.value = emptyList()
                    Log.e("FirestoreError", "Error fetching data: ${e?.message}")
                }
            }
    }

    /**
     * Add an item to the cart. If the item already exists, increase the quantity.
     */
    fun addToCart(item: MenuItem, restaurantId: String, restaurantName: String) {
        viewModelScope.launch {
            val auth = FirebaseAuth.getInstance()
            val currentUserId = auth.currentUser?.uid ?: return@launch

            val existingItem = _cartItemsState.value.find {
                it.name == item.name && it.restaurantId == restaurantId
            }

            if (existingItem != null) {
                // Item exists, increase quantity
                val newQty = existingItem.quantity + 1
                cartCollection
                    .whereEqualTo("name", existingItem.name)
                    .whereEqualTo("userId", currentUserId)
                    .whereEqualTo("restaurantId", restaurantId)
                    .get()
                    .addOnSuccessListener { query ->
                        updateItemQuantity(query, newQty)
                    }
            } else {
                // Add new item to the cart
                val newCartItem = CartItem(
                    name = item.name,
                    price = item.price,
                    imageres = item.imageres,
                    quantity = 1,
                    userId = currentUserId,
                    restaurantId = restaurantId, // Gunakan Document ID
                    restaurantName = restaurantName // Gunakan nama restoran untuk UI
                )
                cartCollection.add(newCartItem)
            }
        }
    }

    /**
     * Update the quantity of an item in the cart.
     */
    private fun updateItemQuantity(query: QuerySnapshot, newQty: Int) {
        for (doc in query.documents) {
            doc.reference.update("quantity", newQty)
        }
    }

    /**
     * Update the quantity of an item directly from the cart.
     */
    fun updateItemQuantityInCart(item: CartItem, newQty: Int) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        cartCollection
            .whereEqualTo("name", item.name)
            .whereEqualTo("userId", currentUserId)
            .whereEqualTo("restaurantId", item.restaurantId) // Query dengan Document ID
            .get()
            .addOnSuccessListener { query ->
                for (doc in query.documents) {
                    doc.reference.update("quantity", newQty)
                }
            }
    }

    /**
     * Remove an item from the cart.
     */
    fun removeFromCart(item: CartItem) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        cartCollection
            .whereEqualTo("name", item.name)
            .whereEqualTo("userId", currentUserId)
            .whereEqualTo("restaurantId", item.restaurantId) // Query dengan Document ID
            .get()
            .addOnSuccessListener { query ->
                for (doc in query.documents) {
                    doc.reference.delete()
                }
            }
    }

    /**
     * Clear all items in the cart for the current user.
     */
    fun clearCart() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        cartCollection
            .whereEqualTo("userId", currentUserId)
            .get()
            .addOnSuccessListener { query ->
                for (doc in query.documents) {
                    doc.reference.delete()
                }
            }
    }
}
