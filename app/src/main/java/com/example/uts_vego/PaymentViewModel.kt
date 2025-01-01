package com.example.uts_vego

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PaymentViewModel : ViewModel() {
    private val _balance = MutableStateFlow(0.0)
    val balance: StateFlow<Double> = _balance

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions

    private val firestore = FirebaseFirestore.getInstance()
    private val realtimeDb = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()

    init {
        loadUserData()
        listenToBalanceUpdates()
        listenToTransactionsUpdates()
    }

    private fun listenToTransactionsUpdates() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("transactions")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("PaymentViewModel", "Transaction listener error: ${e.message}")
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val transactionsList = snapshots.documents.mapNotNull { it.toObject(Transaction::class.java) }

                    // Pastikan data diurutkan berdasarkan tanggal terbaru
                    _transactions.value = transactionsList.sortedByDescending { it.dateCreated }
                }
            }
    }

    private fun listenToBalanceUpdates() {
        val userId = auth.currentUser?.uid ?: return

        realtimeDb.getReference("users")
            .child(userId)
            .child("balance")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val newBalance = snapshot.getValue(Double::class.java) ?: 0.0
                    _balance.value = newBalance
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("PaymentViewModel", "Balance listener cancelled: ${error.message}")
                }
            })
    }


    private fun loadUserData() {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid
                if (userId == null) return@launch

                // Load balance
                realtimeDb.getReference("users")
                    .child(userId)
                    .child("balance")
                    .get()
                    .addOnSuccessListener { snapshot ->
                        _balance.value = snapshot.getValue(Double::class.java) ?: 0.0
                    }
                    .addOnFailureListener { e ->
                        Log.e("PaymentViewModel", "Error loading balance", e)
                    }

                // Load transactions
                firestore.collection("transactions")
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnSuccessListener { documents ->
                        val transactionsList = documents.map { doc -> doc.toObject(Transaction::class.java) }
                        _transactions.value = transactionsList
                    }
                    .addOnFailureListener { e ->
                        Log.e("PaymentViewModel", "Error loading transactions", e)
                    }

            } catch (e: Exception) {
                Log.e("PaymentViewModel", "Error in loadUserData", e)
            }
        }
    }

    fun updateBalance(newBalance: Double, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onError("User not authenticated")
            return
        }

        realtimeDb.getReference("users")
            .child(userId)
            .child("balance")
            .setValue(newBalance)
            .addOnSuccessListener {
                _balance.value = newBalance
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to update balance")
            }
    }

    fun addOrderTransaction(amount: Double, date: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onError("User not authenticated")
            return
        }

        val transaction = Transaction(
            title = "Order",
            location = "",
            date = "Today", // Atau logika lain untuk menentukan string tampilan
            dateCreated = System.currentTimeMillis(), // Timestamp untuk pencatatan
            amount = amount,
            isRefunded = false,
            userId = userId
        )


        firestore.collection("transactions")
            .add(transaction)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Failed to add transaction") }
    }


    // Function to handle top-up
    fun topUp(amount: Double = 50000.0, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onError("User not authenticated")
            return
        }

        viewModelScope.launch {
            try {
                val newBalance = _balance.value + amount
                realtimeDb.getReference("users")
                    .child(userId)
                    .child("balance")
                    .setValue(newBalance)
                    .addOnSuccessListener {
                        _balance.value = newBalance

                        // Add transaction
                        val transaction = Transaction(
                            title = "Top Up",
                            location = "",
                            date = "Today", // Atau logika lain untuk menentukan string tampilan
                            dateCreated = System.currentTimeMillis(), // Timestamp untuk pencatatan
                            amount = amount,
                            isRefunded = false,
                            userId = userId
                        )


                        firestore.collection("transactions")
                            .add(transaction)
                            .addOnSuccessListener {
                                onSuccess()
                                loadUserData() // Reload data
                            }
                            .addOnFailureListener { e ->
                                onError(e.message ?: "Failed to add transaction")
                            }
                    }
                    .addOnFailureListener { e ->
                        onError(e.message ?: "Failed to update balance")
                    }

            } catch (e: Exception) {
                onError(e.message ?: "Unknown error occurred")
            }
        }
    }
}
