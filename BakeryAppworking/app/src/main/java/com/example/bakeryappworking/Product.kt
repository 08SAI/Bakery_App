package com.example.bakeryappworking

data class Product(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val category: String = "",
    val inStock: Boolean = true,
    val rating: Double = 0.0
) {
    constructor() : this("", "", "", 0.0, "", "", true, 0.0) // No-arg constructor for Firebase
} 