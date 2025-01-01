package com.example.uts_vego

data class Transaction(
    val title: String = "",
    val location: String = "",
    val date: String = "", // Untuk keperluan tampilan seperti "Today"
    val dateCreated: Long = System.currentTimeMillis(), // Timestamp sesungguhnya
    val amount: Double = 0.0,
    val isRefunded: Boolean = false,
    val userId: String = ""
)

