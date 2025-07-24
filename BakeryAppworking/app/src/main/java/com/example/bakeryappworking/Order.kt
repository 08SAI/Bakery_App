package com.example.bakeryappworking

import java.util.Date

data class Order(
    val id: String = "",
    val userId: String = "",
    val items: List<CartItem> = listOf(),
    val totalAmount: Double = 0.0,
    val status: String = "Pending",
    val orderDate: Long = Date().time
) {
    constructor() : this("", "", listOf(), 0.0, "Pending", Date().time) // No-arg constructor for Firebase
} 