package com.example.uts_vego

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
    val image: String = "",
    var quantity: Int = 1,
    val userId: String = "" // pastikan ada field userId
)

class CartViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val cartCollection = db.collection("cartItems")

    private val _cartItemsState = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItemsState: StateFlow<List<CartItem>> get() = _cartItemsState

    init {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null) {
            // Hanya listen ke cart milik user saat ini
            cartCollection
                .whereEqualTo("userId", currentUserId)
                .addSnapshotListener { snapshot, e ->
                    if (e == null && snapshot != null) {
                        val items = snapshot.toObjects(CartItem::class.java)
                        _cartItemsState.value = items
                    } else {
                        _cartItemsState.value = emptyList()
                    }
                }
        } else {
            _cartItemsState.value = emptyList()
        }
    }

    fun addToCart(item: MenuItem) {
        viewModelScope.launch {
            val auth = FirebaseAuth.getInstance()
            val currentUserId = auth.currentUser?.uid ?: return@launch

            // Cek item yang sudah ada
            // Karena kita sudah memfilter dengan whereEqualTo("userId", currentUserId),
            // _cartItemsState cuma milik user ini saja.
            val existingItem = _cartItemsState.value.find { it.name == item.name }

            if (existingItem != null) {
                val newQty = existingItem.quantity + 1
                cartCollection
                    .whereEqualTo("name", existingItem.name)
                    .whereEqualTo("userId", currentUserId)
                    .get()
                    .addOnSuccessListener { query ->
                        updateItemQuantity(query, newQty)
                    }
            } else {
                val newCartItem = CartItem(
                    name = item.name,
                    price = item.price,
                    image = item.image,
                    quantity = 1,
                    userId = currentUserId
                )
                cartCollection.add(newCartItem)
            }
        }
    }

    private fun updateItemQuantity(query: QuerySnapshot, newQty: Int) {
        for (doc in query.documents) {
            doc.reference.update("quantity", newQty)
        }
    }

    fun removeFromCart(item: CartItem) {
        viewModelScope.launch {
            cartCollection
                .whereEqualTo("name", item.name)
                .whereEqualTo("userId", item.userId)
                .get()
                .addOnSuccessListener { documents ->
                    for (doc in documents.documents) {
                        doc.reference.delete()
                    }
                }
        }
    }

    fun updateItemQuantityInCart(item: CartItem, newQty: Int) {
        if (newQty <= 0) {
            removeFromCart(item)
        } else {
            viewModelScope.launch {
                cartCollection
                    .whereEqualTo("name", item.name)
                    .whereEqualTo("userId", item.userId)
                    .get()
                    .addOnSuccessListener { query ->
                        updateItemQuantity(query, newQty)
                    }
            }
        }
    }

    fun clearCart() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
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
}
